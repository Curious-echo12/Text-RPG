package com.textrpg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.textrpg.mapper")
@EnableScheduling
public class TextRpgApplication {
    public static void main(String[] args) {
        SpringApplication.run(TextRpgApplication.class, args);
    }
}
