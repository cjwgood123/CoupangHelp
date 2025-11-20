package com.coupang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CouHtmlApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouHtmlApplication.class, args);
    }

}







