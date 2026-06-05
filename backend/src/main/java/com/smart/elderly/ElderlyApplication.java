package com.smart.elderly;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.smart.elderly.mapper")
public class ElderlyApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElderlyApplication.class, args);
    }
}
