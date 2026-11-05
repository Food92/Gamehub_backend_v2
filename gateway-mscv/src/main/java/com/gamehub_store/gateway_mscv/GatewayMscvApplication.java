package com.gamehub_store.gateway_mscv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient // <-- Obligatorio para que use Eureka y resuelva los "lb://"
@SpringBootApplication
public class GatewayMscvApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayMscvApplication.class, args);
	}

}
