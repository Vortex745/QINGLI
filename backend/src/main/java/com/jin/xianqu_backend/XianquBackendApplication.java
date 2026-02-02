package com.jin.xianqu_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.jin.xianqu_backend.mapper")
public class XianquBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(XianquBackendApplication.class, args);
    }

}
