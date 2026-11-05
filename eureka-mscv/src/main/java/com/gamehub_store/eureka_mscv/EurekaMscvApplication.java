package com.gamehub_store.eureka_mscv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;


// @EnableEurekaServer: convierte esta aplicacion en el servidor de descubrimiento.
// Los demas microservicios se registran aqui y se buscan entre si por su nombre.
@EnableEurekaServer
@SpringBootApplication
public class EurekaMscvApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaMscvApplication.class, args);
	}

}
