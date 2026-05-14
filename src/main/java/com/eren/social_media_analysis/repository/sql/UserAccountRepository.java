package com.eren.social_media_analysis.repository.sql;

import com.eren.social_media_analysis.domain.sql.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

	Optional<UserAccount> findByUsername(String username);

	Optional<UserAccount> findByEmail(String email);

	boolean existsByUsername(String username);
}
