package com.pindou.timer.service;

import com.pindou.timer.dto.TableCategoryRequest;
import com.pindou.timer.dto.TableCategoryResponse;

import java.util.List;

/**
 * 桌台分类服务接口
 *
 * @author wuci
 * @date 2026-03-29
 */
public interface TableCategoryService {

    /**
     * 获取所有分类（带桌台统计）
     *
     * @return 分类列表
     */
    List<TableCategoryResponse> getAllCategories();

    /**
     * 创建分类
     *
     * @param request 分类信息
     * @return 分类ID
     */
    Long createCategory(TableCategoryRequest request);

    /**
     * 更新分类
     *
     * @param request 分类信息
     */
    void updateCategory(TableCategoryRequest request);

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);

    /**
     * 根据ID获取分类
     *
     * @param id 分类ID
     * @return 分类信息
     */
    TableCategoryResponse getCategory(Long id);
}
