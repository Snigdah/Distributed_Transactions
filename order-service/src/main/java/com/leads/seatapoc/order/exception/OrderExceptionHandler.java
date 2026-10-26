package com.leads.seatapoc.order.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// When inventory or payment fails, Seata has already rolled back everything.
// Here we only turn the error into a clear response.
@RestControllerAdvice
public class OrderExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> handle(FeignException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", "ROLLED_BACK",
                "reason", readMessage(e)));
    }

    // Take the "message" field from the other service's error response
    private String readMessage(FeignException e) {
        try {
            return objectMapper.readTree(e.contentUTF8()).path("message").asText(e.getMessage());
        } catch (Exception ignored) {
            return e.getMessage();
        }
    }
}
