package com.gdschongik.gdsc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GdscApplication {

    public static void main(String[] args) {
        SpringApplication.run(GdscApplication.class, args);
    }
}
