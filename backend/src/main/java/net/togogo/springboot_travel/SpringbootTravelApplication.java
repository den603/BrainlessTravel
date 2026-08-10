package net.togogo.springboot_travel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("net.togogo.springboot_travel.mapper")
public class SpringbootTravelApplication {


    public static void main(String[] args) {

        SpringApplication.run(SpringbootTravelApplication.class, args);
        System.out.println("景区后端服务启动成功！");
    }

}
