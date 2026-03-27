package com.pindou.timer.controller;

import com.pindou.timer.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 系统健康检查控制器
 *
 * @author pindou
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "系统健康检查", description = "检查数据库、Redis等连接状态")
@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 检查数据库连接
     */
    @Operation(summary = "检查数据库连接")
    @GetMapping("/database")
    public Result<Boolean> checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(5);
            log.info("数据库连接检查: {}", valid ? "成功" : "失败");
            return Result.success(valid);
        } catch (Exception e) {
            log.error("数据库连接失败: {}", e.getMessage(), e);
            return Result.error(500, "数据库连接失败: " + e.getMessage());
        }
    }

    /**
     * 检查Redis连接
     */
    @Operation(summary = "检查Redis连接")
    @GetMapping("/redis")
    public Result<String> checkRedis() {
        if (redisTemplate == null) {
            return Result.error(500, "Redis未配置");
        }
        try {
            redisTemplate.opsForValue().set("health_check", "ok", 10, java.util.concurrent.TimeUnit.SECONDS);
            String value = (String) redisTemplate.opsForValue().get("health_check");
            log.info("Redis连接检查: {}", "ok".equals(value) ? "成功" : "失败");
            return Result.success("Redis连接成功");
        } catch (Exception e) {
            log.error("Redis连接失败: {}", e.getMessage(), e);
            return Result.error(500, "Redis连接失败: " + e.getMessage());
        }
    }

    /**
     * 检查所有服务
     */
    @Operation(summary = "检查所有服务")
    @GetMapping("/all")
    public Result<String> checkAll() {
        StringBuilder sb = new StringBuilder();

        // 检查数据库
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(5);
            sb.append("数据库: ").append(valid ? "✅ 正常" : "❌ 异常").append("\n");
        } catch (Exception e) {
            sb.append("数据库: ❌ 异常 - ").append(e.getMessage()).append("\n");
        }

        // 检查Redis
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set("health_check", "ok", 10, java.util.concurrent.TimeUnit.SECONDS);
                sb.append("Redis: ✅ 正常\n");
            } catch (Exception e) {
                sb.append("Redis: ❌ 异常 - ").append(e.getMessage()).append("\n");
            }
        } else {
            sb.append("Redis: ⚠️  未配置\n");
        }

        log.info("健康检查结果:\n{}", sb);
        return Result.success(sb.toString());
    }
}
