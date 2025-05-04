package com.sportsync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class GetActiveApplication {

  public static void main(String[] args) {
    SpringApplication.run(GetActiveApplication.class, args);
  }
}
