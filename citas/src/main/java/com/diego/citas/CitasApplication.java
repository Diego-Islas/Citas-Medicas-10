package com.diego.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "com.diego.commons",
                "com.diego.citas"
        }
)
public class CitasApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitasApplication.class, args);
    }
}