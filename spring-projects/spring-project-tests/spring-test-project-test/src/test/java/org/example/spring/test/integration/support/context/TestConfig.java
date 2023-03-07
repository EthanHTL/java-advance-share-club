package org.example.spring.test.integration.support.context;

import org.example.spring.test.entity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

    @Bean
    public User user() {
        return new User();
    }
}
