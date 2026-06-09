package com.eren.social_media_analysis.config;

import com.eren.social_media_analysis.service.SentimentAnalysisUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.KafkaException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Merkezi hata yakalama ve raporlama mekanizmasÄ±.
 *
 * TÃ¼m controller'lardan fÄ±rlayan hatalar burada yakalanÄ±r,
 * yapÄ±sal olarak loglanÄ±r ve standart hata response'u dÃ¶ndÃ¼rÃ¼lÃ¼r.
 * Her hata iÃ§in benzersiz bir correlationId Ã¼retilir.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Validation hatalarÄ± (DTO @NotBlank, @Valid vb.)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String correlationId = UUID.randomUUID().toString();
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("GeÃ§ersiz istek verisi");

        log.warn("[HATA-RAPOR] correlationId={}, tip=VALIDATION, mesaj={}",
                correlationId, errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(correlationId, "VALIDATION_ERROR", errorMessage, 400));
    }

    /**
     * Kafka baÄŸlantÄ± ve gÃ¶nderim hatalarÄ±
     */
    @ExceptionHandler(KafkaException.class)
    public ResponseEntity<Map<String, Object>> handleKafkaError(KafkaException ex) {
        String correlationId = UUID.randomUUID().toString();

        log.error("[HATA-RAPOR] correlationId={}, tip=KAFKA_ERROR, mesaj={}",
                correlationId, ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildErrorResponse(correlationId, "KAFKA_ERROR",
                        "MesajlaÅŸma servisi geÃ§ici olarak kullanÄ±lamÄ±yor.", 503));
    }

    @ExceptionHandler(SentimentAnalysisUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleSentimentAnalysisUnavailable(
            SentimentAnalysisUnavailableException ex
    ) {
        String correlationId = UUID.randomUUID().toString();

        log.warn("[HATA-RAPOR] correlationId={}, tip=SENTIMENT_ANALYSIS_UNAVAILABLE, mesaj={}",
                correlationId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildErrorResponse(
                        correlationId,
                        "SENTIMENT_ANALYSIS_UNAVAILABLE",
                        ex.getMessage(),
                        503
                ));
    }

    /**
     * IllegalArgument hatalarÄ± (iÅŸ mantÄ±ÄŸÄ± validasyonlarÄ±)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        String correlationId = UUID.randomUUID().toString();

        log.warn("[HATA-RAPOR] correlationId={}, tip=BAD_REQUEST, mesaj={}",
                correlationId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(correlationId, "BAD_REQUEST", ex.getMessage(), 400));
    }

    /**
     * Beklenmeyen tÃ¼m hatalar (catch-all)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        String correlationId = UUID.randomUUID().toString();

        log.error("[HATA-RAPOR] correlationId={}, tip=INTERNAL_ERROR, sinif={}, mesaj={}",
                correlationId, ex.getClass().getSimpleName(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(correlationId, "INTERNAL_ERROR",
                        "Beklenmeyen bir hata oluÅŸtu. LÃ¼tfen daha sonra tekrar deneyin.", 500));
    }

    /**
     * Standart hata response'u oluÅŸturur.
     */
    private Map<String, Object> buildErrorResponse(String correlationId, String errorType,
                                                    String message, int status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status);
        body.put("error", errorType);
        body.put("message", message);
        body.put("correlationId", correlationId);
        return body;
    }
}
