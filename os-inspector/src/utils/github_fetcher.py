"""GitHub repository content fetcher."""

from __future__ import annotations

import base64
import os
import re
from dataclasses import dataclass, field
from typing import Optional

from github import Github, GithubException
from github.Repository import Repository


# File extensions worth analysing
CODE_EXTENSIONS = {
    ".py", ".js", ".ts", ".jsx", ".tsx", ".java", ".go", ".rs",
    ".c", ".cpp", ".h", ".hpp", ".cs", ".rb", ".php", ".swift",
    ".kt", ".scala", ".r", ".sh", ".bash",
}

DOC_FILENAMES = {
    "readme.md", "readme.rst", "readme.txt", "readme",
    "contributing.md", "contributing.rst",
    "changelog.md", "changelog.rst", "history.md",
    "code_of_conduct.md", "security.md",
    "license", "licence",
}

CI_FILENAMES = {
    ".github/workflows", ".travis.yml", ".circleci/config.yml",
    "jenkinsfile", ".gitlab-ci.yml", "azure-pipelines.yml",
    "bitbucket-pipelines.yml",
}


@dataclass
class RepoFile:
    path: str
    content: str
    size: int


@dataclass
class RepoSnapshot:
    owner: str
    name: str
    full_name: str
    description: Optional[str]
    stars: int
    forks: int
    open_issues: int
    language: Optional[str]
    license_name: Optional[str]
    topics: list[str]
    has_wiki: bool
    default_branch: str

    code_files: list[RepoFile] = field(default_factory=list)
    doc_files: list[RepoFile] = field(default_factory=list)
    has_ci: bool = False
    has_tests: bool = False
    test_files: list[RepoFile] = field(default_factory=list)
    has_contributing: bool = False
    has_changelog: bool = False
    has_code_of_conduct: bool = False
    has_security_policy: bool = False
    total_files: int = 0


def parse_github_url(url: str) -> tuple[str, str]:
    """Extract owner and repo name from a GitHub URL."""
    url = url.strip().rstrip("/")
    patterns = [
        r"github\.com[:/]([^/]+)/([^/\s]+?)(?:\.git)?$",
    ]
    for pattern in patterns:
        m = re.search(pattern, url)
        if m:
            return m.group(1), m.group(2)
    raise ValueError(f"Geçersiz GitHub URL'si: {url}")


def _decode_content(content_obj) -> str:
    try:
        return base64.b64decode(content_obj.content).decode("utf-8", errors="replace")
    except Exception:
        return ""


def _is_test_file(path: str) -> bool:
    lower = path.lower()
    return (
        "/test" in lower
        or lower.startswith("test")
        or "_test." in lower
        or ".test." in lower
        or ".spec." in lower
        or "/tests/" in lower
        or "/spec/" in lower
    )


def _has_ci(repo: Repository) -> bool:
    for ci_path in CI_FILENAMES:
        try:
            repo.get_contents(ci_path)
            return True
        except GithubException:
            continue
    return False


def fetch_repo(
    repo_url: str,
    token: Optional[str] = None,
    max_code_files: int = 30,
    max_file_size_kb: int = 100,
) -> RepoSnapshot:
    """Fetch repository metadata and a representative sample of source files."""

    token = token or os.getenv("GITHUB_TOKEN")
    g = Github(token) if token else Github()

    owner, repo_name = parse_github_url(repo_url)
    repo = g.get_repo(f"{owner}/{repo_name}")

    license_name = None
    try:
        license_name = repo.get_license().license.name
    except GithubException:
        pass

    snapshot = RepoSnapshot(
        owner=owner,
        name=repo_name,
        full_name=repo.full_name,
        description=repo.description,
        stars=repo.stargazers_count,
        forks=repo.forks_count,
        open_issues=repo.open_issues_count,
        language=repo.language,
        license_name=license_name,
        topics=repo.get_topics(),
        has_wiki=repo.has_wiki,
        default_branch=repo.default_branch,
    )

    # Walk tree
    try:
        tree = repo.get_git_tree(repo.default_branch, recursive=True)
    except GithubException as e:
        raise RuntimeError(f"Repo ağacı alınamadı: {e}") from e

    code_paths: list[str] = []
    max_size = max_file_size_kb * 1024

    for item in tree.tree:
        if item.type != "blob":
            continue

        snapshot.total_files += 1
        path_lower = item.path.lower()
        filename_lower = os.path.basename(path_lower)
        ext = os.path.splitext(filename_lower)[1]

        # Documentation / meta files
        if filename_lower in DOC_FILENAMES or filename_lower.startswith("license"):
            snapshot.doc_files.append(RepoFile(path=item.path, content="", size=item.size or 0))
            if "contributing" in filename_lower:
                snapshot.has_contributing = True
            if "changelog" in filename_lower or "history" in filename_lower:
                snapshot.has_changelog = True
            if "code_of_conduct" in filename_lower:
                snapshot.has_code_of_conduct = True
            if "security" in filename_lower:
                snapshot.has_security_policy = True

        # Code files
        if ext in CODE_EXTENSIONS and (item.size or 0) <= max_size:
            if _is_test_file(item.path):
                snapshot.has_tests = True
                if len(snapshot.test_files) < 5:
                    code_paths.append(item.path)
                    snapshot.test_files.append(RepoFile(path=item.path, content="", size=item.size or 0))
            else:
                code_paths.append(item.path)

    # CI detection
    snapshot.has_ci = _has_ci(repo)

    # Fetch actual content for a sample of code files (skip test files from quota)
    non_test_paths = [p for p in code_paths if not _is_test_file(p)]
    selected_paths = non_test_paths[:max_code_files]

    for path in selected_paths:
        try:
            file_obj = repo.get_contents(path)
            content = _decode_content(file_obj)
            snapshot.code_files.append(RepoFile(path=path, content=content, size=len(content)))
        except GithubException:
            continue

    # Fetch doc file contents
    for doc in snapshot.doc_files:
        try:
            file_obj = repo.get_contents(doc.path)
            doc.content = _decode_content(file_obj)
        except GithubException:
            continue

    return snapshot
