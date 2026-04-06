package com.medicalstore.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter userLoginCounter(MeterRegistry meterRegistry) {
        return Counter.builder("user.login.attempts")
                .description("Number of user login attempts")
                .register(meterRegistry);
    }

    @Bean
    public Counter medicineCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("medicine.created")
                .description("Number of medicines created")
                .register(meterRegistry);
    }

    @Bean
    public Counter orderProcessedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("order.processed")
                .description("Number of orders processed")
                .register(meterRegistry);
    }
}