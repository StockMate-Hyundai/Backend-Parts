package com.stockmate.parts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PartsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PartsApplication.class, args);
	}

}
