package com.pindou.timer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 豆屿温柔集管理系统 - 主启动类
 *
 * @author pindou
 * @since 1.0.0
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.pindou.timer.mapper")
public class PindouTimerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PindouTimerApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("豆屿温柔集管理系统启动成功！");
        System.out.println("接口文档: http://localhost:9026/api/doc.html");
        System.out.println("Druid监控: http://localhost:9026/api/druid/");
        System.out.println("客户端地址: http://localhost:9026/");
        System.out.println("========================================\n");
    }
}
