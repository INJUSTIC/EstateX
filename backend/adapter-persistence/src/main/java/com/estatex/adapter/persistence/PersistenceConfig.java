package com.estatex.adapter.persistence;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.estatex.adapter.persistence")
@EnableJpaRepositories(basePackages = "com.estatex.adapter.persistence")
public class PersistenceConfig {
}
