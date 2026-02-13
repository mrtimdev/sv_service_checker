package timdev.timdev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "timdev.timdev.controller")
@EntityScan(basePackages = "timdev.timdev.entity")
@EnableJpaRepositories(basePackages = "timdev.timdev.repository")
@ComponentScan(basePackages = "timdev.timdev")
@EnableJpaAuditing
public class TimdevApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimdevApplication.class, args);
	}

}
