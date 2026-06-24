package com.robtech.fraud_detection.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI fraudDetectionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fraud Detection API")
                        .description("🛡️ API für Betrugserkennung mit Kafka")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Robert P.")
                                .email("robert@example.com")));
    }
}