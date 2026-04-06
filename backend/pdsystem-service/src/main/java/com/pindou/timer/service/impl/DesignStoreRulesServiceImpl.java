package com.pindou.timer.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.constants.RuleCategoryEnum;
import com.pindou.timer.constants.RuleTypeEnum;
import com.pindou.timer.dto.DesignStoreRulesRequest;
import com.pindou.timer.dto.DesignStoreRulesResponse;
import com.pindou.timer.entity.DesignStoreRules;
import com.pindou.timer.mapper.DesignStoreRulesMapper;
import com.pindou.timer.service.DesignStoreRulesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 店铺规则Service实现类
 *
 * @author wuci
 * @date 2026-04-06
 */
@Slf4j
@Service
public class DesignStoreRulesServiceImpl implements DesignStoreRulesService {

    @Resource
    private DesignStoreRulesMapper designStoreRulesMapper;

    @Override
    public List<DesignStoreRulesResponse> getRulesByCategory(String category) {
        log.info("根据分类获取规则列表: category={}", category);

        LambdaQueryWrapper<DesignStoreRules> wrapper = new LambdaQueryWrapper<>();
        // 只查询启用的规则
        wrapper.eq(DesignStoreRules::getIsEnabled, true);

        if (StringUtils.hasText(category)) {
            wrapper.eq(DesignStoreRules::getCategory, category);
        }

        // 按排序号排序
        wrapper.orderByAsc(DesignStoreRules::getSortOrder);

        List<DesignStoreRules> rules = designStoreRulesMapper.selectList(wrapper);

        return rules.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DesignStoreRulesResponse getRuleById(String id) {
        log.info("根据ID获取规则: id={}", id);

        if (!StringUtils.hasText(id)) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "规则ID不能为空");
        }

        DesignStoreRules rule = designStoreRulesMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "规则不存在");
        }

        return convertToResponse(rule);
    }

    @Override
    public DesignStoreRulesResponse createRule(DesignStoreRulesRequest request) {
        log.info("创建规则: request={}", request);

        // 参数校验
        validateRuleRequest(request);

        DesignStoreRules rule = new DesignStoreRules();
        BeanUtils.copyProperties(request, rule);

        rule.setId(IdUtil.simpleUUID());
        rule.setCreatedAt(System.currentTimeMillis());
        rule.setUpdatedAt(System.currentTimeMillis());

        // 默认启用
        if (rule.getIsEnabled() == null) {
            rule.setIsEnabled(true);
        }

        designStoreRulesMapper.insert(rule);

        log.info("规则创建成功: id={}", rule.getId());

        return convertToResponse(rule);
    }

    @Override
    public DesignStoreRulesResponse updateRule(DesignStoreRulesRequest request) {
        log.info("更新规则: request={}", request);

        if (!StringUtils.hasText(request.getId())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "规则ID不能为空");
        }

        // 参数校验
        validateRuleRequest(request);

        DesignStoreRules existing = designStoreRulesMapper.selectById(request.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "规则不存在");
        }

        // 更新字段
        BeanUtils.copyProperties(request, existing, "id", "createdAt", "createdBy");
        existing.setUpdatedAt(System.currentTimeMillis());

        designStoreRulesMapper.updateById(existing);

        log.info("规则更新成功: id={}", existing.getId());

        return convertToResponse(existing);
    }

    @Override
    public Boolean deleteRule(String id) {
        log.info("删除规则: id={}", id);

        if (!StringUtils.hasText(id)) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "规则ID不能为空");
        }

        DesignStoreRules rule = designStoreRulesMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "规则不存在");
        }

        int result = designStoreRulesMapper.deleteById(id);

        log.info("规则删除成功: id={}, result={}", id, result);

        return result > 0;
    }

    @Override
    public Boolean toggleRuleEnabled(String id) {
        log.info("切换规则启用状态: id={}", id);

        if (!StringUtils.hasText(id)) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "规则ID不能为空");
        }

        DesignStoreRules rule = designStoreRulesMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "规则不存在");
        }

        rule.setIsEnabled(!rule.getIsEnabled());
        rule.setUpdatedAt(System.currentTimeMillis());

        int result = designStoreRulesMapper.updateById(rule);

        log.info("规则状态切换成功: id={}, enabled={}", id, rule.getIsEnabled());

        return result > 0;
    }

    /**
     * 校验规则请求参数
     */
    private void validateRuleRequest(DesignStoreRulesRequest request) {
        if (!StringUtils.hasText(request.getCategory())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "规则分类不能为空");
        }

        if (!StringUtils.hasText(request.getContent())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "规则内容不能为空");
        }

        // 校验分类是否合法
        if (!RuleCategoryEnum.isValid(request.getCategory())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "规则分类不合法");
        }

        // 校验规则类型是否合法
        if (StringUtils.hasText(request.getRuleType()) && !RuleTypeEnum.isValid(request.getRuleType())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "规则类型不合法");
        }
    }

    /**
     * 转换为响应对象
     */
    private DesignStoreRulesResponse convertToResponse(DesignStoreRules rule) {
        DesignStoreRulesResponse response = new DesignStoreRulesResponse();
        BeanUtils.copyProperties(rule, response);
        return response;
    }
}
