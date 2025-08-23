package com.c2.ai.SpringAIIntro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringAiIntroApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiIntroApplication.class, args);
	}

}
