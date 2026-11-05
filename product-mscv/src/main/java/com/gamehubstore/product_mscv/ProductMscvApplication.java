package com.gamehubstore.product_mscv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ProductMscvApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductMscvApplication.class, args);
	}

}
