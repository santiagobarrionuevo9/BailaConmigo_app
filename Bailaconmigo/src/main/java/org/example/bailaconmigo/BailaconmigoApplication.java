package org.example.bailaconmigo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BailaconmigoApplication {

	public static void main(String[] args) {
		SpringApplication.run(BailaconmigoApplication.class, args);
	}

}
