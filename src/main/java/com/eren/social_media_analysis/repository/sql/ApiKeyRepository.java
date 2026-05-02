package com.eren.social_media_analysis.repository.sql;

import com.eren.social_media_analysis.domain.sql.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

	List<ApiKey> findByOwnerIdAndActiveTrue(Long ownerId);

	Optional<ApiKey> findByKeyHash(String keyHash);
}
