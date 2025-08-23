package com.c2.ai.SpringAIIntro.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    // This class can be used to configure OpenAPI settings if needed.
    // Currently, it is empty, but you can add configurations for OpenAPI here.
    // For example, you can define API info, security schemes, etc.
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring AI Service API")
                        .version("1.0.0")
                        .description("""
                                API for demonstrating Spring AI features. \
                                This API provides endpoints for various AI-related functionalities, \
                                including chat responses, image processing, and audio conversion,\
                                RAG, Vector Stores Embeddings...
                                
                                Also used Caching with Redis,\s""")
                        .contact(new Contact()
                                .name("Chandan Kalita")
                                .email("c2kalita85@gmail.com")
                                .url("https://your-company.com"))
                );

    }
    // You can also configure security schemes, servers, and other OpenAPI features here.
    // For more details, refer to the Springdoc OpenAPI documentation.
    // https://springdoc.org/
}
