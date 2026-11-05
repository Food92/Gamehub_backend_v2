package com.gamehubstore.category_mscv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CategoryMscvApplication {

	public static void main(String[] args) {
		SpringApplication.run(CategoryMscvApplication.class, args);
	}

}
