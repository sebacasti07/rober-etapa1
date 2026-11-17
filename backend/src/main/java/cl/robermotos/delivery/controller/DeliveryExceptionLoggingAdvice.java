package cl.robermotos.delivery.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMessageNotWritableException;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class DeliveryExceptionLoggingAdvice extends ResponseEntityExceptionHandler
    implements ResponseBodyAdvice<Object> {
    private static final Logger log = LoggerFactory.getLogger(DeliveryExceptionLoggingAdvice.class);
    private final ObjectMapper objectMapper;

    public DeliveryExceptionLoggingAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(
        MethodParameter returnType,
        Class<? extends HttpMessageConverter<?>> converterType
    ) {
        Class<?> controller = returnType.getContainingClass();
        Method method = returnType.getMethod();
        return controller == DeliveryController.class
            && method != null
            && (method.getName().equals("debugQuotes") || method.getName().equals("quotes"));
    }

    @Override
    public Object beforeBodyWrite(
        Object body,
        MethodParameter returnType,
        MediaType selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType,
        ServerHttpRequest request,
        ServerHttpResponse response
    ) {
        try {
            log.info(
                "Serialized quote response for {}: {}",
                request.getURI().getPath(),
                objectMapper.writeValueAsString(body)
            );
            return body;
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize quote response for {}", request.getURI().getPath(), exception);
            throw new HttpMessageNotWritableException("Failed to serialize quote response", exception);
        }
    }

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
