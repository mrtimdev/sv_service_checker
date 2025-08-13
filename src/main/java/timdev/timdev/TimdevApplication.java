package timdev.timdev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// // @SpringBootApplication(scanBasePackages = "timdev.timdev")
// @EnableJpaRepositories(basePackages = "timdev.timdev.repository")
// @SpringBootApplication(scanBasePackages = "timdev.timdev.controller")
// @EntityScan(basePackages = "timdev.timdev.entity")
public class TimdevApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimdevApplication.class, args);
	}

}
