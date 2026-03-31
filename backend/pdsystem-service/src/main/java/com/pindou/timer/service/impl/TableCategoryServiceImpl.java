package com.pindou.timer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.dto.TableCategoryRequest;
import com.pindou.timer.dto.TableCategoryResponse;
import com.pindou.timer.entity.Table;
import com.pindou.timer.entity.TableCategory;
import com.pindou.timer.mapper.TableCategoryMapper;
import com.pindou.timer.mapper.TableMapper;
import com.pindou.timer.service.TableCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 桌台分类服务实现
 *
 * @author wuci
 * @date 2026-03-29
 */
@Slf4j
@Service
public class TableCategoryServiceImpl implements TableCategoryService {

    @Resource
    private TableCategoryMapper categoryMapper;

    @Resource
    private TableMapper tableMapper;

    @Override
    public List<TableCategoryResponse> getAllCategories() {
        // 查询所有分类
        List<TableCategory> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<TableCategory>()
                        .orderByAsc(TableCategory::getSortOrder)
        );

        // 查询所有桌台
        List<Table> tables = tableMapper.selectList(null);

        // 按分类统计桌台数量
        Map<Long, List<Table>> categoryTables = tables.stream()
                .collect(Collectors.groupingBy(t -> t.getCategoryId() != null ? t.getCategoryId() : 0L));

        // 构建响应
        List<TableCategoryResponse> responseList = new ArrayList<>();

        // 添加"全部"分类
        TableCategoryResponse allCategory = new TableCategoryResponse();
        allCategory.setId(0L);
        allCategory.setName("全部");
        allCategory.setIcon("grid");
        allCategory.setSortOrder(0);
        allCategory.setTableCount(tables.size());
        allCategory.setUsingCount((int) tables.stream().filter(t -> "using".equals(t.getStatus())).count());
        responseList.add(allCategory);

        // 添加其他分类
        for (TableCategory category : categories) {
            TableCategoryResponse response = BeanUtil.copyProperties(category, TableCategoryResponse.class);
            List<Table> categoryTablesList = categoryTables.getOrDefault(category.getId(), new ArrayList<>());
            response.setTableCount(categoryTablesList.size());
            response.setUsingCount((int) categoryTablesList.stream().filter(t -> "using".equals(t.getStatus())).count());
            responseList.add(response);
        }

        return responseList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(TableCategoryRequest request) {
        // 检查名称是否重复
        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<TableCategory>().eq(TableCategory::getName, request.getName())
        );
        if (count > 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "分类名称已存在");
        }

        TableCategory category = BeanUtil.copyProperties(request, TableCategory.class);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        // 默认排序
        if (category.getSortOrder() == null) {
            category.setSortOrder(getMaxSortOrder() + 1);
        }

        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(TableCategoryRequest request) {
        if (request.getId() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "分类ID不能为空");
        }

        TableCategory exist = categoryMapper.selectById(request.getId());
        if (exist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }

        // 检查名称是否重复
        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<TableCategory>()
                        .eq(TableCategory::getName, request.getName())
                        .ne(TableCategory::getId, request.getId())
        );
        if (count > 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "分类名称已存在");
        }

        TableCategory category = BeanUtil.copyProperties(request, TableCategory.class);
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        if (id == null || id == 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "不能删除默认分类");
        }

        TableCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }

        // 检查是否有桌台使用该分类
        Long tableCount = tableMapper.selectCount(
                new LambdaQueryWrapper<Table>().eq(Table::getCategoryId, id)
        );
        if (tableCount > 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "该分类下还有桌台，无法删除");
        }

        categoryMapper.deleteById(id);
    }

    @Override
    public TableCategoryResponse getCategory(Long id) {
        if (id == null || id == 0) {
            TableCategoryResponse response = new TableCategoryResponse();
            response.setId(0L);
            response.setName("全部");
            response.setIcon("grid");
            return response;
        }

        TableCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }

        return BeanUtil.copyProperties(category, TableCategoryResponse.class);
    }

    /**
     * 获取最大排序号
     */
    private Integer getMaxSortOrder() {
        List<TableCategory> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<TableCategory>()
                        .orderByDesc(TableCategory::getSortOrder)
                        .last("LIMIT 1")
        );
        return CollUtil.isEmpty(categories) ? 0 : categories.get(0).getSortOrder();
    }
}
