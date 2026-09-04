package com.projetointegrador.natysync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NatySyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(NatySyncApplication.class, args);
    }
}
