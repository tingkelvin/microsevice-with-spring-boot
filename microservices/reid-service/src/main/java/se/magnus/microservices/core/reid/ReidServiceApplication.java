package se.magnus.microservices.core.reid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("se.magnus")
public class ReidServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReidServiceApplication.class, args);
  }
}


