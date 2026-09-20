package org.example.lfmnacional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class LfmNacionalApplication {

    public static void main(String[] args) {
        String zona = System.getenv().getOrDefault("APP_TIMEZONE", "America/Argentina/Buenos_Aires");
        TimeZone.setDefault(TimeZone.getTimeZone(zona));
        System.setProperty("user.timezone", zona);
        SpringApplication.run(LfmNacionalApplication.class, args);
    }

}
