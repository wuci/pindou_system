package com.pindou.timer.aspect;

import cn.hutool.json.JSONUtil;
import com.pindou.timer.annotation.LogOperation;
import com.pindou.timer.entity.OperationLog;
import com.pindou.timer.mapper.OperationLogMapper;
import com.pindou.timer.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 操作日志AOP切面
 * 异步记录操作日志
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    /**
     * 异步线程池
     */
    private static final Executor executor = Executors.newFixedThreadPool(5);

    @Resource
    private OperationLogMapper operationLogMapper;

    /**
     * 定义切点
     */
    @Pointcut("@annotation(com.pindou.timer.annotation.LogOperation)")
    public void logPointcut() {
    }

    /**
     * 环绕通知
     */
    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            // 执行方法
            result = point.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - beginTime;
            // 异步保存日志
            saveLogAsync(point, result, exception, duration);
        }
    }

    /**
     * 异步保存日志
     */
    private void saveLogAsync(ProceedingJoinPoint point, Object result, Exception exception, long duration) {
        CompletableFuture.runAsync(() -> {
            try {
                saveLog(point, result, exception, duration);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }, executor);
    }

    /**
     * 保存日志
     */
    private void saveLog(ProceedingJoinPoint point, Object result, Exception exception, long duration) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        LogOperation logAnnotation = method.getAnnotation(LogOperation.class);

        if (logAnnotation == null) {
            return;
        }

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        // 构建日志对象
        OperationLog operationLog = new OperationLog();
        operationLog.setId(UUID.randomUUID().toString().replace("-", ""));
        operationLog.setModule(logAnnotation.module());
        operationLog.setOperation(logAnnotation.operation());
        operationLog.setDescription(logAnnotation.description());
        operationLog.setMethod(request.getMethod() + " " + request.getRequestURI());
        operationLog.setDuration(duration);

        // 获取IP
        String ip = IpUtil.getClientIp(request);
        operationLog.setIp(ip);

        // 获取User-Agent
        String userAgent = request.getHeader("User-Agent");
        operationLog.setUserAgent(userAgent);

        // 获取请求参数
        if (logAnnotation.saveParams()) {
            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                String params = JSONUtil.toJsonStr(args);
                // 限制参数长度
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "...";
                }
                operationLog.setParams(params);
            }
        }

        // 获取响应结果
        if (logAnnotation.saveResult() && result != null) {
            String resultJson = JSONUtil.toJsonStr(result);
            // 限制结果长度
            if (resultJson.length() > 2000) {
                resultJson = resultJson.substring(0, 2000) + "...";
            }
            operationLog.setResult(resultJson);
        }

        // 操作状态
        if (exception != null) {
            operationLog.setStatus(0);
            operationLog.setErrorMsg(exception.getMessage());
        } else {
            operationLog.setStatus(1);
        }

        // TODO: 从Token或Session中获取用户信息
        // operationLog.setUserId(...);
        // operationLog.setUsername(...);

        // 保存日志
        try {
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志到数据库失败", e);
        }
    }
}
