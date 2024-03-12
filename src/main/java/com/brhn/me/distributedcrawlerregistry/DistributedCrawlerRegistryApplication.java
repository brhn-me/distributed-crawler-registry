package com.brhn.me.distributedcrawlerregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DistributedCrawlerRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedCrawlerRegistryApplication.class, args);
    }

}
