package com.innowise.aikufarbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AiKufarBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiKufarBotApplication.class, args);
    }
}
