package com.estatex.adapter.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com.estatex.adapter.persistence",
        "com.estatex.application"
})
public class ModulesConfig {
}
