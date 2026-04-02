import { ref, computed } from 'vue'
import { getBillingRuleConfig, type BillingRule, type ChannelBillingRule } from '@/api/config'

/**
 * 渠道翻译 Composable
 * 从计费规则配置中动态获取渠道名称
 */
const billingRules = ref<BillingRule | null>(null)
const loading = ref(false)

/**
 * 加载计费规则
 */
const loadBillingRules = async () => {
  if (billingRules.value) {
    return billingRules.value
  }

  try {
    loading.value = true
    const configStr = await getBillingRuleConfig()

    let parsed: any
    try {
      parsed = JSON.parse(configStr)
    } catch (parseError) {
      console.error('计费规则JSON解析失败:', parseError)
      return null
    }

    if (!parsed || typeof parsed !== 'object' || !parsed.channels || !Array.isArray(parsed.channels)) {
      console.error('计费规则数据格式错误')
      return null
    }

    billingRules.value = parsed
    return parsed
  } catch (error: any) {
    console.error('加载计费规则失败', error)
    return null
  } finally {
    loading.value = false
  }
}

/**
 * 根据渠道代码获取渠道名称
 * @param channelCode 渠道代码（如 store, meituan, dianping）
 * @returns 渠道名称（如 店内, 美团, 大众点评），如果未找到则返回原渠道代码
 */
const getChannelName = (channelCode: string): string => {
  if (!billingRules.value || !channelCode) {
    return channelCode
  }

  const channel = billingRules.value.channels.find(
    (c: ChannelBillingRule) => c.channel === channelCode
  )

  return channel?.channelName || channelCode
}

/**
 * 获取所有渠道列表
 */
const getChannels = computed(() => {
  if (!billingRules.value) {
    return []
  }
  return billingRules.value.channels
})

/**
 * 重置计费规则缓存
 */
const resetCache = () => {
  billingRules.value = null
}

/**
 * 使用渠道翻译
 */
export const useChannelTranslation = () => {
  return {
    billingRules,
    loading,
    loadBillingRules,
    getChannelName,
    getChannels,
    resetCache
  }
}
