package com.pindou.timer.aspect;

import cn.hutool.json.JSONUtil;
import com.pindou.timer.annotation.LogOperation;
import com.pindou.timer.dto.UserInfo;
import com.pindou.timer.entity.OperationLog;
import com.pindou.timer.mapper.OperationLogMapper;
import com.pindou.timer.service.UserService;
import com.pindou.timer.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.UUID;

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
     * SpEL 表达式解析器
     */
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 参数名称发现器
     */
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Resource
    private OperationLogMapper operationLogMapper;

    @Resource
    private UserService userService;

    /**
     * 定义切点 - 使用 execution 匹配所有 Controller 方法
     */
    @Pointcut("execution(* com.pindou.timer.controller..*.*(..))")
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

        // 记录方法调用
        log.info("LogAspect: 拦截到方法调用 - {}", point.getSignature().toShortString());

        try {
            // 执行方法
            result = point.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            log.error("LogAspect: 方法执行异常 - {}", point.getSignature().toShortString(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - beginTime;
            log.info("LogAspect: 方法执行完成 - {}, 耗时{}ms", point.getSignature().toShortString(), duration);
            // 同步保存日志（避免异步导致 RequestContextHolder 丢失）
            saveLog(point, result, exception, duration);
        }
    }

    /**
     * 保存日志
     */
    private void saveLog(ProceedingJoinPoint point, Object result, Exception exception, long duration) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // 由于可能使用 CGLIB 代理，需要从目标类获取方法
        Class<?> targetClass = point.getTarget().getClass();
        LogOperation logAnnotation = null;

        try {
            // 从目标类获取方法（处理 CGLIB 代理）
            Method targetMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
            logAnnotation = targetMethod.getAnnotation(LogOperation.class);
        } catch (NoSuchMethodException e) {
            log.warn("无法找到目标方法：{}", method.getName());
        }

        if (logAnnotation == null) {
            // 没有注解，不需要记录日志
            return;
        }

        log.info("记录操作日志：module={}, operation={}", logAnnotation.module(), logAnnotation.operation());

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.warn("无法获取 ServletRequestAttributes");
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        // 构建日志对象
        OperationLog operationLog = new OperationLog();
        operationLog.setId(UUID.randomUUID().toString().replace("-", ""));
        operationLog.setModule(logAnnotation.module());
        operationLog.setOperation(logAnnotation.operation());
        // 手动设置创建时间（解决 MyBatis-Plus FieldFill.INSERT 不生效的问题）
        operationLog.setCreatedAt(System.currentTimeMillis());

        // 解析 SpEL 表达式
        String description = parseSpEL(logAnnotation.description(), point, targetClass);
        operationLog.setDescription(description);
        // 设置 content 字段（兼容旧表结构）
        operationLog.setContent(description);

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

        // 从Token中获取用户信息
        try {
            String authHeader = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(authHeader) && authHeader.length() > 7) {
                String token = authHeader.substring(7);
                String userId = userService.getUserIdFromToken(token);
                if (StringUtils.isNotBlank(userId)) {
                    operationLog.setUserId(userId);
                    // 获取用户名
                    try {
                        UserInfo userInfo = userService.getUserInfo(userId);
                        if (userInfo != null) {
                            operationLog.setUsername(userInfo.getNickname());
                        }
                    } catch (Exception e) {
                        // 获取用户名失败，使用 userId 作为用户名
                        operationLog.setUsername(userId);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("从Token获取用户信息失败", e);
        }

        // 保存日志
        try {
            operationLogMapper.insert(operationLog);
            log.info("操作日志保存成功：id={}, module={}, operation={}",
                operationLog.getId(), operationLog.getModule(), operationLog.getOperation());
        } catch (Exception e) {
            log.error("保存操作日志到数据库失败", e);
        }
    }

    /**
     * 解析 SpEL 表达式
     * 支持模板式占位符，例如：桌台【#tableId】结束计时
     */
    private String parseSpEL(String spel, ProceedingJoinPoint point, Class<?> targetClass) {
        if (StringUtils.isBlank(spel)) {
            return "";
        }

        // 如果不包含 SpEL 表达式标记（# 或 $），直接返回原字符串
        if (!spel.contains("#") && !spel.contains("$")) {
            return spel;
        }

        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();

            // 获取目标类的方法
            Method targetMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());

            // 获取方法参数名和值
            String[] parameterNames = nameDiscoverer.getParameterNames(targetMethod);
            Object[] args = point.getArgs();

            // 创建 SpEL 上下文
            EvaluationContext context = new StandardEvaluationContext();

            // 设置参数到上下文
            if (parameterNames != null) {
                for (int i = 0; i < parameterNames.length; i++) {
                    context.setVariable(parameterNames[i], args[i]);
                }
            }

            // 尝试解析整个表达式（兼容纯表达式情况）
            try {
                Expression expression = parser.parseExpression(spel);
                Object value = expression.getValue(context);
                if (value != null) {
                    return value.toString();
                }
            } catch (Exception e) {
                // 整个字符串解析失败，尝试模板式替换
                log.debug("整体解析失败，尝试模板式替换：{}", spel);
            }

            // 模板式替换：查找所有 #xxx 或 #xxx?.yyy 模式并替换
            String result = spel;
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("#(\\w+(?:\\?\\.\\w+)*)(?=[\\]】\\s,，。]|$)");
            java.util.regex.Matcher matcher = pattern.matcher(result);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String varExpression = matcher.group(0); // 完整匹配，如 #tableId 或 #request?.memberId
                try {
                    Expression expression = parser.parseExpression(varExpression);
                    Object value = expression.getValue(context);
                    String replacement = value != null ? value.toString() : "";
                    matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
                } catch (Exception ex) {
                    log.debug("解析表达式 {} 失败，保持原样", varExpression);
                    matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(varExpression));
                }
            }
            matcher.appendTail(sb);

            return sb.toString();

        } catch (Exception e) {
            log.warn("解析 SpEL 表达式失败：{}", spel, e);
        }

        // 解析失败，返回原始字符串
        return spel;
    }
}
