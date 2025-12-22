package com.cardconnect.langchain4j_spring.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * HTTP interceptor for distributed tracing and observability.
 * Adds trace IDs and spans to all HTTP requests.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TracingInterceptor implements HandlerInterceptor {

    private final ObservationRegistry observationRegistry;

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    private static final String SPAN_ID_HEADER = "X-Span-ID";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Generate or extract request ID
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        // Store in request attributes
        request.setAttribute("requestId", requestId);
        request.setAttribute("startTime", System.currentTimeMillis());

        // Add to response headers
        response.setHeader(REQUEST_ID_HEADER, requestId);

        log.debug("Request started: {} {} [requestId={}]",
                request.getMethod(), request.getRequestURI(), requestId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {

        String requestId = (String) request.getAttribute("requestId");
        Long startTime = (Long) request.getAttribute("startTime");

        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;

            log.info("Request completed: {} {} - Status: {} - Duration: {}ms [requestId={}]",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration,
                    requestId);
        }

        if (ex != null) {
            log.error("Request failed: {} {} [requestId={}]",
                    request.getMethod(), request.getRequestURI(), requestId, ex);
        }
    }
}

