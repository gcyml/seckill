package com.secskill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 秒杀系统主应用类
 * 
 * @author seckill
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,  // 排除数据源自动配置
        RedisAutoConfiguration.class          // 排除Redis自动配置（如果Redis未启动）
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

