package com.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 秒杀系统主应用类
 *
 * @author seckill
 */
@SpringBootApplication
// 已启用数据源自动配置
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

