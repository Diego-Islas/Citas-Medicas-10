package com.diego.medicos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "com.diego.commons",
                "com.diego.medicos"
        }
)
public class MedicosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicosApplication.class, args);
    }

}
