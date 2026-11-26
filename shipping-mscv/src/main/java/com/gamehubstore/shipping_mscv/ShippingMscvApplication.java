package com.gamehubstore.shipping_mscv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ShippingMscvApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShippingMscvApplication.class, args);
	}

}
