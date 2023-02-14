package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableR2dbcRepositories
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class,args);
    }

    @Bean
    PeopleService peopleService() {
        return new DefaultPeopleService();
    }
}