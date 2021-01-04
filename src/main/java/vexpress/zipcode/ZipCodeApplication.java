package vexpress.zipcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class ZipCodeApplication {
  public static void main(final String[] args) {
    SpringApplication.run(ZipCodeApplication.class, args);
  }
}
