package com.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 秒杀系统主应用类
 *
 * @author seckill
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class  // 排除数据源自动配置
        // RedisAutoConfiguration.class 已移除，启用Redis自动配置
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

