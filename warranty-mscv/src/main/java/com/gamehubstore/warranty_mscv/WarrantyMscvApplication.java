package com.gamehubstore.warranty_mscv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class WarrantyMscvApplication {

	public static void main(String[] args) {
		SpringApplication.run(WarrantyMscvApplication.class, args);
	}

}
