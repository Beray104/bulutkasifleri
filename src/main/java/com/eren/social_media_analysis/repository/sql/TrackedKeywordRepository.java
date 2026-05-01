package com.eren.social_media_analysis.repository.sql;

import com.eren.social_media_analysis.domain.sql.TrackedKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackedKeywordRepository extends JpaRepository<TrackedKeyword, Long> {

	List<TrackedKeyword> findByOwnerIdAndActiveTrue(Long ownerId);

	Optional<TrackedKeyword> findByOwnerIdAndKeyword(Long ownerId, String keyword);
}
