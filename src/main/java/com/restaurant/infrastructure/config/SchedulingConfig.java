package com.restaurant.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables {@code @Scheduled} jobs (e.g. the reservation reminder). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
