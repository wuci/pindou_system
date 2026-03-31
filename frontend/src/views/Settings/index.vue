<template>
  <div class="settings-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <!-- 计费规则 -->
        <el-tab-pane label="计费规则" name="billing">
          <div class="tab-content">
            <!-- 渠道选择 -->
            <div class="channel-tabs">
              <el-radio-group v-model="activeChannel" @change="handleChannelChange">
                <el-radio-button value="store">店内</el-radio-button>
                <el-radio-button value="meituan">美团</el-radio-button>
                <el-radio-button value="dianping">大众点评</el-radio-button>
              </el-radio-group>
            </div>

            <!-- 规则列表 -->
            <div class="rules-section">
              <div class="section-header">
                <h3>{{ getChannelName(activeChannel) }}计费规则</h3>
                <el-button type="primary" :icon="Plus" @click="addRule">添加规则</el-button>
              </div>

              <div class="rules-list">
                <div
                  v-for="(rule, index) in currentChannelRules"
                  :key="index"
                  class="rule-item"
                >
                  <div class="rule-content">
                    <div class="rule-hours">
                      <el-input-number
                        v-model="rule.hours"
                        :min="1"
                        :max="24"
                        :disabled="rule.unlimited"
                        placeholder="时长"
                        style="width: 120px"
                      />
                      <span class="unit">小时</span>
                      <el-checkbox v-model="rule.unlimited" @change="handleUnlimitedChange(rule, index)">
                        不限时
                      </el-checkbox>
                    </div>
                    <div class="rule-price">
                      <el-input-number
                        v-model="rule.price"
                        :min="1"
                        :max="999"
                        :precision="0"
                        placeholder="价格"
                        style="width: 120px"
                      />
                      <span class="unit">元</span>
                    </div>
                    <div class="rule-display">
                      <span class="display-text">{{ formatRuleDisplay(rule) }}</span>
                    </div>
                  </div>
                  <div class="rule-actions">
                    <el-button
                      type="danger"
                      :icon="Delete"
                      size="small"
                      circle
                      @click="removeRule(index)"
                    />
                  </div>
                </div>

                <el-empty v-if="currentChannelRules.length === 0" description="暂无计费规则，请添加" />
              </div>
            </div>

            <!-- 保存按钮 -->
            <div class="save-section">
              <el-button type="primary" :loading="billingSaving" @click="saveBillingRule">
                保存配置
              </el-button>
            </div>

            <!-- 说明信息 -->
            <el-alert
              title="计费说明"
              type="info"
              :closable="false"
              show-icon
              style="margin-top: 20px"
            >
              <template #default>
                <div>• 每个渠道可设置多条计费规则</div>
                <div>• 系统会根据使用时长自动匹配最优惠的价格</div>
                <div>• 不限时规则表示封顶价格，超过此时长不再额外计费</div>
                <div>• 建议按时长从小到大排序设置规则</div>
              </template>
            </el-alert>
          </div>
        </el-tab-pane>

        <!-- 提醒配置 -->
        <el-tab-pane label="提醒配置" name="remind">
          <div class="tab-content">
            <el-form
              ref="remindFormRef"
              :model="remindForm"
              :rules="remindRules"
              label-width="140px"
              style="max-width: 600px"
            >
              <el-form-item label="提醒阈值" prop="threshold">
                <el-input-number
                  v-model="remindForm.threshold"
                  :min="30"
                  :max="600"
                  :step="30"
                  style="width: 200px"
                />
                <span class="unit">秒</span>
                <div class="form-tip">剩余时间小于等于此值时提醒</div>
              </el-form-item>

              <el-form-item label="声音开关" prop="soundEnabled">
                <el-switch
                  v-model="remindForm.soundEnabledBoolean"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>

              <el-form-item label="重复间隔" prop="repeatInterval">
                <el-input-number
                  v-model="remindForm.repeatInterval"
                  :min="10"
                  :max="300"
                  :step="10"
                  style="width: 200px"
                />
                <span class="unit">秒</span>
                <div class="form-tip">超时后重复提醒的间隔时间</div>
              </el-form-item>

              <el-form-item>
                <el-alert
                  title="提醒说明"
                  type="info"
                  :closable="false"
                  show-icon
                >
                  <template #default>
                    <div>• 即将到期：剩余时间达到阈值时提醒</div>
                    <div>• 已超时：使用时间超过预设时长时提醒</div>
                    <div>• 超时后会按重复间隔持续提醒</div>
                  </template>
                </el-alert>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :loading="remindSaving" @click="saveRemindConfig">
                  保存配置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import {
  getBillingRuleConfig,
  updateBillingRule,
  getRemindConfig,
  updateRemindConfig,
  type BillingRule,
  type ChannelBillingRule,
  type BillingRuleItem,
  type RemindConfig
} from '@/api/config'

// 当前激活的标签
const activeTab = ref('billing')

// 计费规则相关
const activeChannel = ref<'store' | 'meituan' | 'dianping'>('store')
const billingSaving = ref(false)

// 计费规则数据结构
const billingData = reactive<BillingRule>({
  channels: [
    {
      channel: 'store',
      channelName: '店内',
      rules: []
    },
    {
      channel: 'meituan',
      channelName: '美团',
      rules: []
    },
    {
      channel: 'dianping',
      channelName: '大众点评',
      rules: []
    }
  ]
})

// 当前渠道的规则
const currentChannelRules = computed(() => {
  const channel = billingData.channels.find(c => c.channel === activeChannel.value)
  return channel?.rules || []
})

// 获取渠道名称
const getChannelName = (channel: string): string => {
  const names: Record<string, string> = {
    store: '店内',
    meituan: '美团',
    dianping: '大众点评'
  }
  return names[channel] || channel
}

// 格式化规则显示
const formatRuleDisplay = (rule: BillingRuleItem): string => {
  if (rule.unlimited) {
    return `不限时 ${rule.price}元`
  }
  return `${rule.hours}小时 ${rule.price}元`
}

// 处理不限时变化
const handleUnlimitedChange = (rule: BillingRuleItem, index: number) => {
  if (rule.unlimited) {
    rule.hours = null
  } else {
    rule.hours = rule.hours || 1
  }
}

// 添加规则
const addRule = () => {
  const channel = billingData.channels.find(c => c.channel === activeChannel.value)
  if (channel) {
    channel.rules.push({
      hours: 1,
      price: 19,
      unlimited: false
    })
  }
}

// 删除规则
const removeRule = (index: number) => {
  const channel = billingData.channels.find(c => c.channel === activeChannel.value)
  if (channel) {
    channel.rules.splice(index, 1)
  }
}

// 渠道切换
const handleChannelChange = () => {
  // 切换渠道时不需要特殊处理，computed会自动更新
}

// 加载计费规则配置
const loadBillingConfig = async () => {
  try {
    const config = await getBillingRuleConfig()
    if (config && config.channels && config.channels.length > 0) {
      billingData.channels = config.channels
    } else {
      // 使用默认值
      setDefaultRules()
    }
  } catch (error) {
    console.error('加载计费规则配置失败', error)
    setDefaultRules()
  }
}

// 设置默认规则
const setDefaultRules = () => {
  const defaultRules = [
    { hours: 1, price: 19, unlimited: false },
    { hours: 2, price: 35, unlimited: false },
    { hours: 4, price: 54, unlimited: false },
    { hours: null, price: 68, unlimited: true }
  ]

  billingData.channels.forEach(channel => {
    channel.rules = JSON.parse(JSON.stringify(defaultRules))
  })
}

// 保存计费规则配置
const saveBillingRule = async () => {
  // 验证规则
  for (const channel of billingData.channels) {
    if (channel.rules.length === 0) {
      ElMessage.warning(`${channel.channelName}至少需要一条计费规则`)
      return
    }

    for (const rule of channel.rules) {
      if (!rule.unlimited && (!rule.hours || rule.hours < 1)) {
        ElMessage.warning(`${channel.channelName}的规则时长必须大于0小时`)
        return
      }
      if (!rule.price || rule.price < 1) {
        ElMessage.warning(`${channel.channelName}的规则价格必须大于0元`)
        return
      }
    }
  }

  try {
    billingSaving.value = true
    await updateBillingRule(billingData)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    billingSaving.value = false
  }
}

// 提醒配置表单
const remindFormRef = ref<FormInstance>()
const remindSaving = ref(false)
const remindForm = reactive<RemindConfig & { soundEnabledBoolean: boolean }>({
  threshold: 300,
  soundEnabledBoolean: true,
  repeatInterval: 60,
  expiringCloseTime: 60,
  timeoutCloseTime: 120
})

const remindRules: FormRules = {
  threshold: [{ required: true, message: '请输入提醒阈值', trigger: 'blur' }],
  repeatInterval: [{ required: true, message: '请输入重复间隔', trigger: 'blur' }]
}

// 加载提醒配置
const loadRemindConfig = async () => {
  try {
    const configStr = await getRemindConfig()
    const config = JSON.parse(configStr)
    remindForm.threshold = config.threshold || 300
    remindForm.soundEnabledBoolean = config.soundEnabled === 1
    remindForm.repeatInterval = config.repeatInterval || 60
  } catch (error) {
    console.error('加载提醒配置失败', error)
  }
}

// 保存提醒配置
const saveRemindConfig = async () => {
  if (!remindFormRef.value) return

  try {
    await remindFormRef.value.validate()
    remindSaving.value = true

    const config: RemindConfig = {
      threshold: remindForm.threshold,
      soundEnabled: remindForm.soundEnabledBoolean ? 1 : 0,
      repeatInterval: remindForm.repeatInterval,
      expiringCloseTime: 60,
      timeoutCloseTime: 120
    }

    await updateRemindConfig(config)
    ElMessage.success('保存成功')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    remindSaving.value = false
  }
}

// 生命周期
onMounted(() => {
  loadBillingConfig()
  loadRemindConfig()
})
</script>

<style scoped>
.settings-page {
  padding: 20px;
}

.tab-content {
  padding: 20px 0;
}

.channel-tabs {
  margin-bottom: 20px;
}

.rules-section {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.rules-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rule-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.rule-content {
  display: flex;
  align-items: center;
  gap: 24px;
  flex: 1;
}

.rule-hours,
.rule-price {
  display: flex;
  align-items: center;
}

.rule-display {
  margin-left: auto;
}

.display-text {
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
}

.unit {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
  white-space: nowrap;
}

.save-section {
  margin-top: 20px;
  text-align: center;
}

.form-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}
</style>
