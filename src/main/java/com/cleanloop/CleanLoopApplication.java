package com.cleanloop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for CleanLoop Backend application.
 */
@SpringBootApplication
@EnableScheduling
public class CleanLoopApplication {

    public static void main(String[] args) {
        SpringApplication.run(CleanLoopApplication.class, args);
    }
}
