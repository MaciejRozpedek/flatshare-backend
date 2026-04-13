package com.flatshareteam.flatsharebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlatshareBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlatshareBackendApplication.class, args);
    }

}
