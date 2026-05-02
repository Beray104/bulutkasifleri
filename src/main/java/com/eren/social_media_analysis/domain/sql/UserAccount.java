package com.eren.social_media_analysis.domain.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(nullable = false, unique = true, length = 80)
	private String username;

	@NotBlank
	@Email
	@Column(nullable = false, unique = true, length = 160)
	private String email;

	@NotBlank
	@Column(nullable = false)
	private String passwordHash;

	@Column(nullable = false)
	private Instant createdAt = Instant.now();

	@Column(nullable = false)
	private boolean active = true;

	@OneToMany(mappedBy = "owner")
	private Set<ApiKey> apiKeys = new HashSet<>();

	@OneToMany(mappedBy = "owner")
	private Set<TrackedKeyword> trackedKeywords = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Set<ApiKey> getApiKeys() {
		return apiKeys;
	}

	public void setApiKeys(Set<ApiKey> apiKeys) {
		this.apiKeys = apiKeys;
	}

	public Set<TrackedKeyword> getTrackedKeywords() {
		return trackedKeywords;
	}

	public void setTrackedKeywords(Set<TrackedKeyword> trackedKeywords) {
		this.trackedKeywords = trackedKeywords;
	}
}
