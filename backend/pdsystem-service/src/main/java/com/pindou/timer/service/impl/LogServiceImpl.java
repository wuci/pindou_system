package com.pindou.timer.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.LogInfoResponse;
import com.pindou.timer.dto.LogQueryRequest;
import com.pindou.timer.entity.OperationLog;
import com.pindou.timer.mapper.OperationLogMapper;
import com.pindou.timer.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日志Service实现类
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Service
public class LogServiceImpl implements LogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    public PageResult<LogInfoResponse> getLogList(LogQueryRequest request) {
        log.info("获取日志列表: request={}", request);

        // 构建查询条件
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        // 模块筛选
        if (StringUtils.isNotBlank(request.getModule())) {
            wrapper.eq(OperationLog::getModule, request.getModule());
        }

        // 操作类型筛选
        if (StringUtils.isNotBlank(request.getOperation())) {
            wrapper.eq(OperationLog::getOperation, request.getOperation());
        }

        // 用户名筛选
        if (StringUtils.isNotBlank(request.getUsername())) {
            wrapper.eq(OperationLog::getUsername, request.getUsername());
        }

        // 时间范围筛选
        if (request.getStartTime() != null) {
            wrapper.ge(OperationLog::getCreatedAt, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(OperationLog::getCreatedAt, request.getEndTime());
        }

        // 状态筛选
        if (request.getStatus() != null) {
            wrapper.eq(OperationLog::getStatus, request.getStatus());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(OperationLog::getCreatedAt);

        // 分页查询
        Page<OperationLog> page = new Page<>(request.getPage(), request.getPageSize());
        Page<OperationLog> resultPage = operationLogMapper.selectPage(page, wrapper);

        // 转换结果
        List<LogInfoResponse> records = resultPage.getRecords().stream()
                .map(this::convertToInfoResponse)
                .collect(Collectors.toList());

        PageResult<LogInfoResponse> pageResult = new PageResult<>();
        pageResult.setList(records);
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setPage(request.getPage());
        pageResult.setPageSize(request.getPageSize());

        log.info("获取日志列表成功: total={}", pageResult.getTotal());
        return pageResult;
    }

    @Override
    public List<LogInfoResponse> exportLogs(LogQueryRequest request) {
        // 设置较大的分页用于导出
        request.setPage(1);
        request.setPageSize(50000);

        PageResult<LogInfoResponse> pageResult = getLogList(request);

        log.info("导出日志成功: count={}", pageResult.getList().size());
        return pageResult.getList();
    }

    /**
     * 转换为日志信息响应
     */
    private LogInfoResponse convertToInfoResponse(OperationLog log) {
        LogInfoResponse response = new LogInfoResponse();
        response.setId(log.getId());
        response.setModule(log.getModule());
        response.setOperation(log.getOperation());
        response.setDescription(log.getDescription());
        response.setUsername(log.getUsername());
        response.setMethod(log.getMethod());
        response.setParams(log.getParams());
        response.setResult(log.getResult());
        response.setDuration(log.getDuration());
        response.setIp(log.getIp());
        response.setStatus(log.getStatus());
        response.setErrorMsg(log.getErrorMsg());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }
}
