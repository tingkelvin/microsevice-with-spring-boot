package se.magnus.microservices.core.lpr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("se.magnus")
public class LprServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(LprServiceApplication.class, args);
  }
}

