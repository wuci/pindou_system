package com.pindou.timer.service;

import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.LogInfoResponse;
import com.pindou.timer.dto.LogQueryRequest;

import java.util.List;

/**
 * 日志Service接口
 *
 * @author wuci
 * @date 2026-03-28
 */
public interface LogService {

    /**
     * 获取日志列表（分页）
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<LogInfoResponse> getLogList(LogQueryRequest request);

    /**
     * 导出日志数据
     *
     * @param request 查询请求
     * @return 日志列表
     */
    List<LogInfoResponse> exportLogs(LogQueryRequest request);
}
