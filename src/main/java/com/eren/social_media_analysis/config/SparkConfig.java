package com.eren.social_media_analysis.config;

import org.apache.spark.sql.SparkSession;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SparkProperties.class)
@ConditionalOnProperty(prefix = "app.spark", name = "enabled", havingValue = "true")
public class SparkConfig {

	// Spring Boot içinde çalışan local Spark oturumunu oluşturur.
	@Bean(destroyMethod = "stop")
	public SparkSession sparkSession(SparkProperties properties) {
		return SparkSession.builder()
				.appName(properties.getAppName())
				.master(properties.getMaster())
				.config("spark.sql.shuffle.partitions", "2")
				.config("spark.es.nodes", properties.getElasticsearchNodes())
				.config("spark.es.port", properties.getElasticsearchPort())
				.config("spark.es.nodes.wan.only", "true")
				.getOrCreate();
	}
}
