package com.eren.social_media_analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = { org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class })
@EntityScan("com.eren.social_media_analysis.domain.sql")
@EnableJpaRepositories("com.eren.social_media_analysis.repository.sql")
public class SocialMediaAnalysisApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialMediaAnalysisApplication.class, args);
	}

}
