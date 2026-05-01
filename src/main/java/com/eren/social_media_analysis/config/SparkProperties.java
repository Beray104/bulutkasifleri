package com.eren.social_media_analysis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.spark")
public class SparkProperties {

	private boolean enabled;
	private String appName;
	private String master;
	private String kafkaBootstrapServers;
	private String kafkaTopic;
	private String elasticsearchNodes;
	private String elasticsearchPort;
	private String elasticsearchIndex;
	private String checkpointLocation;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public String getKafkaBootstrapServers() {
		return kafkaBootstrapServers;
	}

	public void setKafkaBootstrapServers(String kafkaBootstrapServers) {
		this.kafkaBootstrapServers = kafkaBootstrapServers;
	}

	public String getKafkaTopic() {
		return kafkaTopic;
	}

	public void setKafkaTopic(String kafkaTopic) {
		this.kafkaTopic = kafkaTopic;
	}

	public String getElasticsearchNodes() {
		return elasticsearchNodes;
	}

	public void setElasticsearchNodes(String elasticsearchNodes) {
		this.elasticsearchNodes = elasticsearchNodes;
	}

	public String getElasticsearchPort() {
		return elasticsearchPort;
	}

	public void setElasticsearchPort(String elasticsearchPort) {
		this.elasticsearchPort = elasticsearchPort;
	}

	public String getElasticsearchIndex() {
		return elasticsearchIndex;
	}

	public void setElasticsearchIndex(String elasticsearchIndex) {
		this.elasticsearchIndex = elasticsearchIndex;
	}

	public String getCheckpointLocation() {
		return checkpointLocation;
	}

	public void setCheckpointLocation(String checkpointLocation) {
		this.checkpointLocation = checkpointLocation;
	}
}
