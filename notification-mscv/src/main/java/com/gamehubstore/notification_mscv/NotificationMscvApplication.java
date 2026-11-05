package com.gamehubstore.notification_mscv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class NotificationMscvApplication {
    public static void main(String[] args) { SpringApplication.run(NotificationMscvApplication.class, args); }
}
