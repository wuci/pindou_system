package com.pindou.timer.common.result;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 分页结果
 *
 * @param <T> 数据类型
 * @author pindou
 * @since 1.0.0
 */
@Data
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    public PageResult() {
    }

    public PageResult(List<T> list, Long total, Integer page, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 创建分页结果
     *
     * @param list     数据列表
     * @param total    总记录数
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param <T>      数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Integer page, Integer pageSize) {
        return new PageResult<>(list, total, page, pageSize);
    }

    /**
     * 创建空的分页结果
     *
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param <T>      数据类型
     * @return 空的分页结果
     */
    public static <T> PageResult<T> empty(Integer page, Integer pageSize) {
        return new PageResult<>(Collections.emptyList(), 0L, page, pageSize);
    }
}
