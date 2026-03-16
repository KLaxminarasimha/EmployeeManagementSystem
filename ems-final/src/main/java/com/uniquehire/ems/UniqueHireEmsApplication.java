package com.uniquehire.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UniqueHireEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(UniqueHireEmsApplication.class, args);
    }
}
