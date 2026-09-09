package com.acougue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AcougueApplication {
    public static void main(String[] args) {
        SpringApplication.run(AcougueApplication.class, args);
    }
}
