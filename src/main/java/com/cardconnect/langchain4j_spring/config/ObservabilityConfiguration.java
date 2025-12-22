package com.cardconnect.langchain4j_spring.config;

import com.cardconnect.langchain4j_spring.observability.TracingInterceptor;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for observability, monitoring, and distributed tracing.
 * Registers interceptors and aspects for automatic instrumentation.
 */
@Configuration
@RequiredArgsConstructor
public class ObservabilityConfiguration implements WebMvcConfigurer {

    private final TracingInterceptor tracingInterceptor;

    /**
     * Register tracing interceptor for all HTTP requests
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tracingInterceptor)
                .addPathPatterns("/api/**")
                .addPathPatterns("/agent/**");
    }

    /**
     * Enable @Observed annotation support
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}

