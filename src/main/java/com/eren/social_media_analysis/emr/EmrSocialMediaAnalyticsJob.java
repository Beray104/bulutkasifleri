package com.eren.social_media_analysis.emr;

import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.spark.sql.functions.array_intersect;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.concat_ws;
import static org.apache.spark.sql.functions.current_timestamp;
import static org.apache.spark.sql.functions.expr;
import static org.apache.spark.sql.functions.from_json;
import static org.apache.spark.sql.functions.lit;
import static org.apache.spark.sql.functions.lower;
import static org.apache.spark.sql.functions.regexp_extract_all;
import static org.apache.spark.sql.functions.regexp_replace;
import static org.apache.spark.sql.functions.size;
import static org.apache.spark.sql.functions.split;
import static org.apache.spark.sql.functions.struct;
import static org.apache.spark.sql.functions.to_json;
import static org.apache.spark.sql.functions.to_timestamp;
import static org.apache.spark.sql.functions.when;

public class EmrSocialMediaAnalyticsJob {

	private static final String OPENSEARCH_SERVICE_NAME = "es";
	private static final String BULK_CONTENT_TYPE = "application/x-ndjson";

	public static void main(String[] args) throws Exception {
		EmrJobSettings settings = EmrJobSettings.fromEnvironment();

		SparkSession spark = SparkSession.builder()
				.appName("emr-social-media-analytics-streaming")
				.config("spark.sql.shuffle.partitions", settings.shufflePartitions())
				.getOrCreate();

		Dataset<Row> kafkaStream = readFromMsk(spark, settings);
		Dataset<Row> analyticsStream = transform(kafkaStream);

		StreamingQuery query = writeToOpenSearch(analyticsStream, settings);
		query.awaitTermination();
	}

	// Reads the social media event stream from Amazon MSK or any Kafka-compatible AWS endpoint.
	private static Dataset<Row> readFromMsk(SparkSession spark, EmrJobSettings settings) {
		org.apache.spark.sql.streaming.DataStreamReader reader = spark.readStream()
				.format("kafka")
				.option("kafka.bootstrap.servers", settings.kafkaBootstrapServers())
				.option("subscribe", settings.kafkaTopic())
				.option("startingOffsets", settings.kafkaStartingOffsets())
				.option("failOnDataLoss", "false");

		settings.kafkaSecurityProtocol().ifPresent(value -> reader.option("kafka.security.protocol", value));
		settings.kafkaSaslMechanism().ifPresent(value -> reader.option("kafka.sasl.mechanism", value));
		settings.kafkaSaslJaasConfig().ifPresent(value -> reader.option("kafka.sasl.jaas.config", value));

		return reader.load();
	}

	// Parses JSON, tokenizes text, extracts hashtags, and computes dictionary-based sentiment fields.
	private static Dataset<Row> transform(Dataset<Row> kafkaStream) {
		StructType messageSchema = new StructType()
				.add("externalId", DataTypes.StringType)
				.add("platform", DataTypes.StringType)
				.add("authorUsername", DataTypes.StringType)
				.add("content", DataTypes.StringType)
				.add("language", DataTypes.StringType)
				.add("publishedAt", DataTypes.StringType);

		Dataset<Row> parsedMessages = kafkaStream
				.selectExpr("CAST(value AS STRING) AS json")
				.select(from_json(col("json"), messageSchema).alias("message"))
				.select("message.*")
				.where(col("content").isNotNull());

		Column cleanText = lower(regexp_replace(col("content"), "[^\\p{L}\\p{N}#@ ]", " "));
		Column tokens = split(regexp_replace(cleanText, "#", ""), "\\s+");
		Column positiveWords = expr("array('iyi','harika','mukemmel','pozitif','basarili','sevildi','great','good','happy')");
		Column negativeWords = expr("array('kotu','berbat','negatif','basarisiz','uzgun','kizgin','bad','sad','angry')");
		Column positiveCount = size(array_intersect(tokens, positiveWords));
		Column negativeCount = size(array_intersect(tokens, negativeWords));

		return parsedMessages
				.withColumn("tokens", tokens)
				.withColumn("hashtags", regexp_extract_all(col("content"), lit("#[\\p{L}\\p{N}_]+"), lit(0)))
				.withColumn("positive_count", positiveCount)
				.withColumn("negative_count", negativeCount)
				.withColumn("sentiment_score", positiveCount.minus(negativeCount))
				.withColumn("sentiment_label", when(col("sentiment_score").gt(0), "POSITIVE")
						.when(col("sentiment_score").lt(0), "NEGATIVE")
						.otherwise("NEUTRAL"))
				.withColumn("trend_keywords", concat_ws(",", col("hashtags")))
				.withColumn("published_at", to_timestamp(col("publishedAt")))
				.withColumn("processed_at", current_timestamp())
				.select(
						col("externalId").alias("external_id"),
						col("platform"),
						col("authorUsername").alias("author_username"),
						col("content"),
						col("language"),
						col("tokens"),
						col("hashtags"),
						col("trend_keywords"),
						col("positive_count"),
						col("negative_count"),
						col("sentiment_score"),
						col("sentiment_label"),
						col("published_at"),
						col("processed_at")
				);
	}

	// Writes each micro-batch to AWS OpenSearch using IAM credentials from the default provider chain.
	private static StreamingQuery writeToOpenSearch(Dataset<Row> analyticsStream, EmrJobSettings settings) throws Exception {
		return analyticsStream.writeStream()
				.outputMode("append")
				.option("checkpointLocation", settings.checkpointLocation())
				.foreachBatch((batch, batchId) -> {
					Dataset<Row> documents = batch.select(
							col("external_id"),
							to_json(struct(col("*"))).alias("document_json")
					);

					documents.foreachPartition(new OpenSearchBulkPartitionWriter(
							settings.openSearchEndpoint(),
							settings.openSearchIndex(),
							settings.awsRegion(),
							settings.bulkSize()
					));
				})
				.start();
	}

	private record EmrJobSettings(
			String kafkaBootstrapServers,
			String kafkaTopic,
			String kafkaStartingOffsets,
			java.util.Optional<String> kafkaSecurityProtocol,
			java.util.Optional<String> kafkaSaslMechanism,
			java.util.Optional<String> kafkaSaslJaasConfig,
			String openSearchEndpoint,
			String openSearchIndex,
			String awsRegion,
			String checkpointLocation,
			String shufflePartitions,
			int bulkSize
	) {

		static EmrJobSettings fromEnvironment() {
			return new EmrJobSettings(
					requiredEnv("MSK_BOOTSTRAP_SERVERS"),
					envOrDefault("KAFKA_TOPIC", "social-media-topic"),
					envOrDefault("KAFKA_STARTING_OFFSETS", "latest"),
					optionalEnv("KAFKA_SECURITY_PROTOCOL"),
					optionalEnv("KAFKA_SASL_MECHANISM"),
					optionalEnv("KAFKA_SASL_JAAS_CONFIG"),
					trimTrailingSlash(requiredEnv("OPENSEARCH_ENDPOINT")),
					envOrDefault("OPENSEARCH_INDEX", "social_media_analytics"),
					requiredRegion(),
					requiredEnv("CHECKPOINT_LOCATION"),
					envOrDefault("SPARK_SHUFFLE_PARTITIONS", "24"),
					Integer.parseInt(envOrDefault("OPENSEARCH_BULK_SIZE", "500"))
			);
		}
	}

	private static final class OpenSearchBulkPartitionWriter implements ForeachPartitionFunction<Row>, Serializable {

		private final String endpoint;
		private final String index;
		private final String region;
		private final int bulkSize;

		private OpenSearchBulkPartitionWriter(String endpoint, String index, String region, int bulkSize) {
			this.endpoint = endpoint;
			this.index = index;
			this.region = region;
			this.bulkSize = bulkSize;
		}

		@Override
		public void call(Iterator<Row> rows) throws Exception {
			OpenSearchBulkClient client = new OpenSearchBulkClient(endpoint, region);
			StringBuilder bulkPayload = new StringBuilder();
			int itemCount = 0;

			while (rows.hasNext()) {
				Row row = rows.next();
				String documentId = valueOrDefault(row.getAs("external_id"), UUID.randomUUID().toString());
				String documentJson = row.getAs("document_json");

				bulkPayload.append("{\"index\":{\"_index\":\"")
						.append(index)
						.append("\",\"_id\":\"")
						.append(escapeJson(documentId))
						.append("\"}}\n")
						.append(documentJson)
						.append('\n');

				itemCount++;
				if (itemCount >= bulkSize) {
					client.sendBulk(bulkPayload.toString());
					bulkPayload.setLength(0);
					itemCount = 0;
				}
			}

			if (!bulkPayload.isEmpty()) {
				client.sendBulk(bulkPayload.toString());
			}
		}
	}

	private static final class OpenSearchBulkClient {

		private final URI bulkUri;
		private final String region;
		private final HttpClient httpClient;
		private final Aws4Signer signer;
		private final DefaultCredentialsProvider credentialsProvider;

		private OpenSearchBulkClient(String endpoint, String region) {
			this.bulkUri = URI.create(endpoint + "/_bulk");
			this.region = region;
			this.httpClient = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(10))
					.build();
			this.signer = Aws4Signer.create();
			this.credentialsProvider = DefaultCredentialsProvider.create();
		}

		private void sendBulk(String payload) throws IOException, InterruptedException {
			byte[] body = payload.getBytes(StandardCharsets.UTF_8);
			SdkHttpFullRequest signedRequest = sign(body);

			HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(bulkUri)
					.timeout(Duration.ofSeconds(30))
					.POST(HttpRequest.BodyPublishers.ofByteArray(body));

			for (Map.Entry<String, List<String>> header : signedRequest.headers().entrySet()) {
				if (!"host".equalsIgnoreCase(header.getKey()) && !"content-length".equalsIgnoreCase(header.getKey())) {
					for (String value : header.getValue()) {
						requestBuilder.header(header.getKey(), value);
					}
				}
			}

			HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 300 || response.body().contains("\"errors\":true")) {
				throw new IOException("OpenSearch bulk write failed. status=" + response.statusCode() + ", body=" + response.body());
			}
		}

		private SdkHttpFullRequest sign(byte[] body) {
			AwsCredentials credentials = credentialsProvider.resolveCredentials();
			SdkHttpFullRequest unsignedRequest = SdkHttpFullRequest.builder()
					.method(SdkHttpMethod.POST)
					.protocol(bulkUri.getScheme())
					.host(bulkUri.getHost())
					.port(bulkUri.getPort())
					.encodedPath("/_bulk")
					.putHeader("Content-Type", BULK_CONTENT_TYPE)
					.contentStreamProvider(() -> new ByteArrayInputStream(body))
					.build();

			Aws4SignerParams signerParams = Aws4SignerParams.builder()
					.awsCredentials(credentials)
					.signingName(OPENSEARCH_SERVICE_NAME)
					.signingRegion(Region.of(region))
					.build();

			return signer.sign(unsignedRequest, signerParams);
		}
	}

	private static String requiredEnv(String name) {
		String value = System.getenv(name);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Missing required environment variable: " + name);
		}
		return value;
	}

	private static String requiredRegion() {
		String region = System.getenv("AWS_REGION");
		if (region == null || region.isBlank()) {
			region = System.getenv("AWS_DEFAULT_REGION");
		}
		if (region == null || region.isBlank()) {
			throw new IllegalArgumentException("Missing required environment variable: AWS_REGION or AWS_DEFAULT_REGION");
		}
		return region;
	}

	private static java.util.Optional<String> optionalEnv(String name) {
		String value = System.getenv(name);
		return value == null || value.isBlank() ? java.util.Optional.empty() : java.util.Optional.of(value);
	}

	private static String envOrDefault(String name, String defaultValue) {
		String value = System.getenv(name);
		return value == null || value.isBlank() ? defaultValue : value;
	}

	private static String trimTrailingSlash(String value) {
		while (value.endsWith("/")) {
			value = value.substring(0, value.length() - 1);
		}
		return value;
	}

	private static String valueOrDefault(String value, String defaultValue) {
		return value == null || value.isBlank() ? defaultValue : value;
	}

	private static String escapeJson(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
