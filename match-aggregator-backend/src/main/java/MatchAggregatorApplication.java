package ru.mirea.matchaggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MatchAggregatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(MatchAggregatorApplication.class, args);
    }
}
