package cl.robermotos.delivery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class DeliveryExceptionLoggingAdvice extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(DeliveryExceptionLoggingAdvice.class);

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception exception,
        Object body,
        HttpHeaders headers,
        HttpStatusCode statusCode,
        WebRequest request
    ) {
        if (statusCode.is4xxClientError()) {
            log.warn("Request failed with status {}: {}", statusCode.value(), exception.getMessage(), exception);
        } else {
            log.error("Request failed with status {}: {}", statusCode.value(), exception.getMessage(), exception);
        }
        return super.handleExceptionInternal(exception, body, headers, statusCode, request);
    }
}
