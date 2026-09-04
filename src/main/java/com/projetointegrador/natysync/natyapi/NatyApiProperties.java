package com.projetointegrador.natysync.natyapi;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naty.api")
public record NatyApiProperties(String url, String token, Duration timeout) {}
