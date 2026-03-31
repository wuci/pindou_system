package com.pindou.timer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pindou.timer.dto.TableLayoutConfigRequest;
import com.pindou.timer.dto.TableLayoutConfigResponse;
import com.pindou.timer.entity.TableLayoutConfig;
import com.pindou.timer.mapper.TableLayoutConfigMapper;
import com.pindou.timer.service.TableLayoutConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 桌台布局配置服务实现
 *
 * @author wuci
 * @since 2026-03-30
 */
@Slf4j
@Service
public class TableLayoutConfigServiceImpl implements TableLayoutConfigService {

    @Resource
    private TableLayoutConfigMapper layoutConfigMapper;

    @Override
    public TableLayoutConfigResponse getLayoutConfig(Long categoryId) {
        TableLayoutConfig config = layoutConfigMapper.selectOne(
                new LambdaQueryWrapper<TableLayoutConfig>()
                        .eq(TableLayoutConfig::getCategoryId, categoryId)
                        .last("LIMIT 1")
        );

        TableLayoutConfigResponse response = new TableLayoutConfigResponse();
        response.setCategoryId(categoryId);

        if (config != null) {
            response.setConfig(config.getConfig());
        }

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLayoutConfig(TableLayoutConfigRequest request) {
        // 查询是否已存在该分类的配置
        TableLayoutConfig existingConfig = layoutConfigMapper.selectOne(
                new LambdaQueryWrapper<TableLayoutConfig>()
                        .eq(TableLayoutConfig::getCategoryId, request.getCategoryId())
                        .last("LIMIT 1")
        );

        if (existingConfig != null) {
            // 更新现有配置
            existingConfig.setConfig(request.getConfig());
            layoutConfigMapper.updateById(existingConfig);
            log.info("更新布局配置成功, categoryId={}", request.getCategoryId());
        } else {
            // 新增配置
            TableLayoutConfig newConfig = new TableLayoutConfig();
            newConfig.setCategoryId(request.getCategoryId());
            newConfig.setConfig(request.getConfig());
            layoutConfigMapper.insert(newConfig);
            log.info("保存布局配置成功, categoryId={}", request.getCategoryId());
        }
    }
}
