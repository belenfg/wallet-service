package com.playtomic.tests.wallet.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles IllegalArgumentException thrown by use cases or controllers.
     * Maps "not found" messages to 404, others to 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        String msg = e.getMessage();
        if (msg != null && msg.toLowerCase().contains("not found")) {
            log.warn("Resource not found: {}", msg);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            log.error("Bad request: {}", msg);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(msg));
        }
    }
}
