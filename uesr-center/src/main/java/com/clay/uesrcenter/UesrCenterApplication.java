package com.clay.uesrcenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
@MapperScan("com.clay.uesrcenter.mapper")
public class UesrCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(UesrCenterApplication.class, args);
    }

}
