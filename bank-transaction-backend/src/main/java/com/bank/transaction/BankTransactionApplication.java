package com.bank.transaction;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableCaching
public class BankTransactionApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankTransactionApplication.class, args);
    }
    
    @Bean
    public OpenAPI bankTransactionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Transaction Management System API")
                        .description("RESTful API documentation for the Bank Transaction Management System")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Bank Technical Team")
                                .url("https://bank.example.com")
                                .email("support@bank.example.com")));
    }
} 