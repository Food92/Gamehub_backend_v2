package com.gamehubstore.review_mscv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ReviewMscvApplication {
    public static void main(String[] args) { SpringApplication.run(ReviewMscvApplication.class, args); }
}
