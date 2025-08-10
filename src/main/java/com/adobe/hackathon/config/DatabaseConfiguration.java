// DatabaseConfiguration.java - Database Config
package com.adobe.hackathon.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.adobe.hackathon.repository")
@EntityScan(basePackages = "com.adobe.hackathon.model.entity")
@EnableTransactionManagement
public class DatabaseConfiguration {
    // Database configuration is handled by Spring Boot auto-configuration
    // This class ensures proper package scanning
}