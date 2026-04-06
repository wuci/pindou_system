<template>
  <div class="settings-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <!-- 计费规则 -->
        <el-tab-pane v-if="permissions.canConfigRule" label="计费规则" name="billing">
          <div class="tab-content">
            <!-- 渠道选择和管理 -->
            <div class="channel-tabs">
              <el-radio-group v-model="activeChannel" @change="handleChannelChange">
                <el-radio-button
                  v-for="channel in billingData.channels"
                  :key="channel.channel"
                  :value="channel.channel"
                >
                  {{ channel.channelName }}
                </el-radio-button>
              </el-radio-group>
              <el-button
                type="primary"
                :icon="Plus"
                size="small"
                @click="showAddChannelDialog"
                style="margin-left: 12px"
              >
                添加计费方式
              </el-button>
              <el-button
                v-if="billingData.channels.length > 1"
                type="danger"
                :icon="Delete"
                size="small"
                @click="handleDeleteChannel"
              >
                删除计费方式
              </el-button>
            </div>

            <!-- 规则列表 -->
            <div class="rules-section">
              <div class="section-header">
                <h3>{{ getChannelName(activeChannel) }}计费规则</h3>
                <div class="header-actions">
                  <el-button type="success" :icon="Sort" @click="autoSortRules">自动排序</el-button>
                  <el-button type="primary" :icon="Plus" @click="addRule">添加规则</el-button>
                </div>
              </div>

              <div class="rules-list">
                <div
                  v-for="(rule, index) in currentChannelRules"
                  :key="index"
                  class="rule-item"
                >
                  <div class="rule-content">
                    <div class="rule-time">
                      <el-input-number
                        v-model="rule.hours"
                        :min="0"
                        :max="23"
                        :disabled="rule.unlimited"
                        placeholder="0"
                        style="width: 100px"
                        @change="calculateTotalMinutes(rule)"
                      />
                      <span class="unit">小时</span>
                      <el-input-number
                        v-model="rule.minutes_ui"
                        :min="0"
                        :max="59"
                        :disabled="rule.unlimited"
                        placeholder="分钟"
                        style="width: 100px"
                        @change="calculateTotalMinutes(rule)"
                      />
                      <span class="unit">分钟</span>
                      <el-checkbox v-model="rule.unlimited" @change="handleUnlimitedChange(rule, index)">
                        不限时
                      </el-checkbox>
                    </div>
                    <div class="rule-price">
                      <el-input-number
                        v-model="rule.price"
                        :min="0"
                        :max="999.99"
                        :precision="2"
                        :step="0.5"
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
                      type="primary"
                      :icon="Top"
                      size="small"
                      circle
                      :disabled="index === 0"
                      @click="moveRuleUp(index)"
                      title="上移"
                    />
                    <el-button
                      type="primary"
                      :icon="Bottom"
                      size="small"
                      circle
                      :disabled="index === currentChannelRules.length - 1"
                      @click="moveRuleDown(index)"
                      title="下移"
                    />
                    <el-button
                      type="danger"
                      :icon="Delete"
                      size="small"
                      circle
                      @click="removeRule(index)"
                      title="删除"
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
        <el-tab-pane v-if="permissions.canConfigRemind" label="提醒配置" name="remind">
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
                    <div>• 已超时：使用时间超过套餐时长时提醒</div>
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

        <!-- 系统参数配置 -->
        <el-tab-pane v-if="permissions.canConfigParam" label="系统参数配置" name="system">
          <div class="tab-content">
            <el-form
              ref="systemFormRef"
              :model="systemForm"
              :rules="systemRules"
              label-width="140px"
              style="max-width: 600px"
            >
              <el-form-item label="延长时间" prop="extendTime">
                <el-input-number
                  v-model="systemForm.extendTime"
                  :min="1"
                  :max="120"
                  :step="1"
                  style="width: 200px"
                />
                <span class="unit">分钟</span>
                <div class="form-tip">桌台超时后可延长的使用时间</div>
              </el-form-item>

              <el-form-item label="无效订单时间" prop="invalidOrderTime">
                <el-input-number
                  v-model="systemForm.invalidOrderTime"
                  :min="0"
                  :max="60"
                  :step="1"
                  style="width: 200px"
                />
                <span class="unit">分钟</span>
                <div class="form-tip">几分钟内结束的订单视为无效订单，0表示不限制</div>
              </el-form-item>

              <el-form-item>
                <el-alert
                  title="参数说明"
                  type="info"
                  :closable="false"
                  show-icon
                >
                  <template #default>
                    <div>• 延长时间：桌台使用超时后，可以延长的使用时间</div>
                    <div>• 无效订单时间：几分钟内结束的订单视为无效订单，不计入统计</div>
                  </template>
                </el-alert>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :loading="systemSaving" @click="saveSystemConfig">
                  保存配置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 折扣设置 -->
        <el-tab-pane v-if="permissions.canConfigDiscount" label="折扣设置" name="discount">
          <div class="tab-content">
            <!-- 操作按钮 -->
            <div class="section-header">
              <h3>折扣列表</h3>
              <el-button type="primary" :icon="Plus" @click="showAddDiscountDialog">
                添加折扣
              </el-button>
            </div>

            <!-- 折扣列表 -->
            <div class="discount-list">
              <div
                v-for="discount in discountList"
                :key="discount.id"
                class="discount-item"
              >
                <div class="discount-info">
                  <div class="discount-name">{{ discount.name }}</div>
                  <div class="discount-details">
                    <el-tag :type="discount.status === 1 ? 'success' : 'info'" size="small">
                      {{ discount.status === 1 ? '启用' : '禁用' }}
                    </el-tag>
                    <el-tag size="small" style="margin-left: 8px">
                      {{ discount.typeName }}
                    </el-tag>
                    <span class="discount-rate">{{ (discount.discountRate * 10).toFixed(1) }}折</span>
                    <span v-if="discount.minAmount" class="discount-condition">
                      满{{ discount.minAmount }}元
                    </span>
                    <span v-if="discount.memberLevelName" class="discount-condition">
                      {{ discount.memberLevelName }}
                    </span>
                  </div>
                  <div v-if="discount.description" class="discount-desc">
                    {{ discount.description }}
                  </div>
                </div>
                <div class="discount-actions">
                  <el-button
                    type="primary"
                    size="small"
                    @click="editDiscount(discount)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    :type="discount.status === 1 ? 'warning' : 'success'"
                    size="small"
                    @click="toggleDiscountStatus(discount)"
                  >
                    {{ discount.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="deleteDiscount(discount)"
                  >
                    删除
                  </el-button>
                </div>
              </div>

              <el-empty v-if="discountList.length === 0" description="暂无折扣设置" />
            </div>

            <!-- 说明信息 -->
            <el-alert
              title="折扣说明"
              type="info"
              :closable="false"
              show-icon
              style="margin-top: 20px"
            >
              <template #default>
                <div>• 固定折扣：适用于所有订单，按设置的折扣率计算</div>
                <div>• 会员折扣：仅适用于指定会员等级的会员</div>
                <div>• 活动折扣：可用于特定活动，可设置有效期</div>
                <div>• 系统会自动选择最优惠的折扣应用到订单</div>
              </template>
            </el-alert>
          </div>
        </el-tab-pane>

      </el-tabs>
    </el-card>

    <!-- 添加渠道对话框 -->
    <el-dialog
      v-model="addChannelDialogVisible"
      title="添加渠道"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="addChannelFormRef" :model="addChannelForm" :rules="addChannelRules" label-width="100px">
        <el-form-item label="渠道代码" prop="channel">
          <el-input
            v-model="addChannelForm.channel"
            placeholder="请输入渠道代码，如: store"
            maxlength="20"
            show-word-limit
          />
          <div class="form-tip">渠道代码是唯一标识，只能包含字母、数字和下划线</div>
        </el-form-item>
        <el-form-item label="渠道名称" prop="channelName">
          <el-input
            v-model="addChannelForm.channelName"
            placeholder="请输入渠道名称，如: 店内"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addChannelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddChannel">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑折扣对话框 -->
    <el-dialog
      v-model="discountDialogVisible"
      :title="discountDialogMode === 'add' ? '添加折扣' : '编辑折扣'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="discountFormRef" :model="discountForm" :rules="discountRules" label-width="120px">
        <el-form-item label="折扣名称" prop="name">
          <el-input
            v-model="discountForm.name"
            placeholder="请输入折扣名称，如: 全场9折"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="折扣类型" prop="type">
          <el-select v-model="discountForm.type" placeholder="请选择折扣类型" style="width: 100%">
            <el-option :value="1" label="固定折扣" />
            <el-option :value="2" label="会员折扣" />
            <el-option :value="3" label="活动折扣" />
          </el-select>
        </el-form-item>

        <el-form-item label="折扣率" prop="discountRate">
          <el-input-number
            v-model="discountForm.discountRate"
            :min="0.1"
            :max="1.0"
            :step="0.05"
            :precision="2"
            style="width: 200px"
          />
          <span class="unit">（0.9表示9折）</span>
        </el-form-item>

        <el-form-item label="最低消费金额">
          <el-input-number
            v-model="discountForm.minAmount"
            :min="0"
            :precision="2"
            style="width: 200px"
          />
          <span class="unit">元，留空表示无限制</span>
        </el-form-item>

        <el-form-item label="最高优惠金额">
          <el-input-number
            v-model="discountForm.maxDiscount"
            :min="0"
            :precision="2"
            style="width: 200px"
          />
          <span class="unit">元，留空表示无限制</span>
        </el-form-item>

        <el-form-item v-if="discountForm.type === 2" label="会员等级" prop="memberLevelId">
          <el-select v-model="discountForm.memberLevelId" placeholder="请选择会员等级" style="width: 100%">
            <el-option
              v-for="level in memberLevels"
              :key="level.id"
              :value="level.id"
              :label="level.name"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="开始时间">
          <el-date-picker
            v-model="discountForm.startTime"
            type="datetime"
            placeholder="留空表示立即生效"
            style="width: 100%"
            :format="'YYYY-MM-DD HH:mm:ss'"
            :value-format="'X'"
            @change="handleStartTimeChange"
          />
        </el-form-item>

        <el-form-item label="结束时间">
          <el-date-picker
            v-model="discountForm.endTime"
            type="datetime"
            placeholder="留空表示永久有效"
            style="width: 100%"
            :format="'YYYY-MM-DD HH:mm:ss'"
            :value-format="'X'"
            @change="handleEndTimeChange"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="discountForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number
            v-model="discountForm.sort"
            :min="0"
            style="width: 200px"
          />
          <span class="unit">数值越小越靠前</span>
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="discountForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入折扣描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="discountDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="discountSaving" @click="saveDiscount">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Delete, Top, Bottom, Sort } from '@element-plus/icons-vue'
import {
  getBillingRuleConfig,
  updateBillingRule,
  getRemindConfig,
  updateRemindConfig,
  getSystemConfig,
  updateSystemConfig,
  type BillingRule,
  type ChannelBillingRule,
  type BillingRuleItem,
  type RemindConfig,
  type SystemConfig
} from '@/api/config'
import {
  getDiscountList,
  createDiscount,
  updateDiscount,
  deleteDiscount as deleteDiscountApi,
  updateDiscountStatus,
  type DiscountInfo,
  type CreateDiscountParams,
  type UpdateDiscountParams
} from '@/api/discount'
import { getMemberLevelList, type MemberLevelInfo } from '@/api/memberLevel'
import { useUserStore } from '@/stores/user'

// 用户状态和权限
const userStore = useUserStore()

const permissions = computed(() => ({
  canConfigRule: userStore.hasPermission('system:rule'),
  canConfigRemind: userStore.hasPermission('system:remind'),
  canConfigParam: userStore.hasPermission('system:param'),
  canConfigDiscount: userStore.hasPermission('system:discount')
}))

// 当前激活的标签 - 根据权限自动选择第一个有权限的tab
const activeTab = ref('billing')

// 根据权限设置默认tab
onMounted(() => {
  if (permissions.value.canConfigRule) {
    activeTab.value = 'billing'
  } else if (permissions.value.canConfigRemind) {
    activeTab.value = 'remind'
  } else if (permissions.value.canConfigParam) {
    activeTab.value = 'system'
  }
})

// 计费规则相关
const activeChannel = ref<string>('store')
const billingSaving = ref(false)

// 添加渠道对话框
const addChannelDialogVisible = ref(false)
const addChannelFormRef = ref<FormInstance>()
const addChannelForm = reactive({
  channel: '',
  channelName: ''
})

const addChannelRules: FormRules = {
  channel: [
    { required: true, message: '请输入渠道代码', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]+$/,
      message: '渠道代码只能包含字母、数字和下划线',
      trigger: 'blur'
    }
  ],
  channelName: [
    { required: true, message: '请输入渠道名称', trigger: 'blur' }
  ]
}

// 计费规则数据结构
const billingData = reactive<BillingRule>({
  channels: []
})

// 当前渠道的规则
const currentChannelRules = computed(() => {
  const channel = billingData.channels.find(c => c.channel === activeChannel.value)
  return channel?.rules || []
})

// 获取渠道名称
const getChannelName = (channelCode: string): string => {
  const channel = billingData.channels.find(c => c.channel === channelCode)
  return channel?.channelName || channelCode
}

// 显示添加渠道对话框
const showAddChannelDialog = () => {
  addChannelForm.channel = ''
  addChannelForm.channelName = ''
  addChannelDialogVisible.value = true
}

// 添加渠道
const handleAddChannel = async () => {
  if (!addChannelFormRef.value) return

  try {
    await addChannelFormRef.value.validate()

    // 检查渠道代码是否已存在
    const exists = billingData.channels.some(c => c.channel === addChannelForm.channel)
    if (exists) {
      ElMessage.warning('渠道代码已存在')
      return
    }

    // 添加新渠道
    billingData.channels.push({
      channel: addChannelForm.channel,
      channelName: addChannelForm.channelName,
      rules: []
    })

    // 切换到新渠道
    activeChannel.value = addChannelForm.channel

    ElMessage.success('渠道添加成功')
    addChannelDialogVisible.value = false
  } catch (error) {
    // 验证失败
  }
}

// 删除渠道
const handleDeleteChannel = () => {
  if (billingData.channels.length <= 1) {
    ElMessage.warning('至少需要保留一个渠道')
    return
  }

  const currentChannelIndex = billingData.channels.findIndex(c => c.channel === activeChannel.value)
  const currentChannelData = billingData.channels[currentChannelIndex]

  ElMessageBox.confirm(
    `确定要删除"${currentChannelData.channelName}"渠道吗？删除后该渠道的计费规则也将被删除。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    billingData.channels.splice(currentChannelIndex, 1)

    // 切换到第一个渠道
    if (billingData.channels.length > 0) {
      activeChannel.value = billingData.channels[0].channel
    }

    ElMessage.success('渠道删除成功')
  }).catch(() => {
    // 取消删除
  })
}

// 格式化规则显示
const formatRuleDisplay = (rule: BillingRuleItem): string => {
  if (rule.unlimited) {
    return `不限时 ${rule.price.toFixed(2)}元`
  }

  const totalMinutes = rule.minutes || 0
  const hours = Math.floor(totalMinutes / 60)
  const minutes = totalMinutes % 60

  if (hours > 0 && minutes > 0) {
    return `${hours}小时${minutes}分钟 ${rule.price.toFixed(2)}元`
  } else if (hours > 0) {
    return `${hours}小时 ${rule.price.toFixed(2)}元`
  } else {
    return `${minutes}分钟 ${rule.price.toFixed(2)}元`
  }
}

// 处理不限时变化
const handleUnlimitedChange = (rule: BillingRuleItem, _index: number) => {
  if (rule.unlimited) {
    rule.minutes = null
    rule.hours = 0
    rule.minutes_ui = 0
  } else {
    rule.minutes = rule.minutes || 60
    rule.hours = Math.floor((rule.minutes || 60) / 60)
    rule.minutes_ui = (rule.minutes || 60) % 60
  }
}

// 监听小时和分钟变化，自动计算总分钟数
const calculateTotalMinutes = (rule: BillingRuleItem) => {
  if (!rule.unlimited && rule.hours !== undefined && rule.minutes_ui !== undefined) {
    rule.minutes = rule.hours * 60 + rule.minutes_ui
  }
}

// 添加规则
const addRule = () => {
  const channel = billingData.channels.find(c => c.channel === activeChannel.value)
  if (channel) {
    channel.rules.push({
      minutes: 60,
      hours: 1,
      minutes_ui: 0,
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

// 上移规则
const moveRuleUp = (index: number) => {
  if (index === 0) return
  const channel = billingData.channels.find(c => c.channel === activeChannel.value)
  if (channel) {
    const [removed] = channel.rules.splice(index, 1)
    channel.rules.splice(index - 1, 0, removed)
  }
}

// 下移规则
const moveRuleDown = (index: number) => {
  const channel = billingData.channels.find(c => c.channel === activeChannel.value)
  if (channel && index < channel.rules.length - 1) {
    const [removed] = channel.rules.splice(index, 1)
    channel.rules.splice(index + 1, 0, removed)
  }
}

// 自动排序（按时长从小到大，不限时规则排在最后）
const autoSortRules = () => {
  const channel = billingData.channels.find(c => c.channel === activeChannel.value)
  if (channel && channel.rules.length > 1) {
    channel.rules.sort((a, b) => {
      // 不限时规则排在最后
      if (a.unlimited && !b.unlimited) return 1
      if (!a.unlimited && b.unlimited) return -1
      if (a.unlimited && b.unlimited) return 0

      // 按时长从小到大排序
      const aMinutes = a.minutes || 0
      const bMinutes = b.minutes || 0
      return aMinutes - bMinutes
    })
    ElMessage.success('已按时长自动排序')
  }
}

// 渠道切换
const handleChannelChange = () => {
  // 切换渠道时不需要特殊处理，computed会自动更新
}

// 加载计费规则配置
const loadBillingConfig = async () => {
  try {
    const configStr = await getBillingRuleConfig()
    const config = JSON.parse(configStr)
    if (config && config.channels && config.channels.length > 0) {
      // 转换数据格式，添加 UI 辅助字段
      billingData.channels = config.channels.map((channel: ChannelBillingRule) => ({
        ...channel,
        rules: channel.rules.map((rule: BillingRuleItem) => {
          if (rule.unlimited) {
            return {
              ...rule,
              hours: 0,
              minutes_ui: 0
            }
          }
          const totalMinutes = rule.minutes || 0
          return {
            ...rule,
            hours: Math.floor(totalMinutes / 60),
            minutes_ui: totalMinutes % 60
          }
        })
      }))

      // 设置当前选中的渠道
      if (billingData.channels.length > 0) {
        // 如果当前选中的渠道不存在，切换到第一个渠道
        const currentExists = billingData.channels.some(c => c.channel === activeChannel.value)
        if (!currentExists) {
          activeChannel.value = billingData.channels[0].channel
        }
      }
    } else {
      // 使用默认值
      setDefaultRules()
    }
  } catch (error) {
    setDefaultRules()
  }
}

// 设置默认规则
const setDefaultRules = () => {
  const defaultRules = [
    { minutes: 60, hours: 1, minutes_ui: 0, price: 19, unlimited: false },
    { minutes: 120, hours: 2, minutes_ui: 0, price: 35, unlimited: false },
    { minutes: 240, hours: 4, minutes_ui: 0, price: 54, unlimited: false },
    { minutes: null, hours: 0, minutes_ui: 0, price: 68, unlimited: true }
  ]

  const defaultChannels = [
    { channel: 'store', channelName: '店内', rules: JSON.parse(JSON.stringify(defaultRules)) },
    { channel: 'meituan', channelName: '美团', rules: JSON.parse(JSON.stringify(defaultRules)) },
    { channel: 'dianping', channelName: '大众点评', rules: JSON.parse(JSON.stringify(defaultRules)) }
  ]

  billingData.channels = defaultChannels
  activeChannel.value = 'store'
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
      if (!rule.unlimited) {
        const totalMinutes = rule.minutes || 0
        if (totalMinutes < 1) {
          ElMessage.warning(`${channel.channelName}的规则时长必须大于0分钟`)
          return
        }
      }
      if (rule.price == null || rule.price < 0) {
        ElMessage.warning(`${channel.channelName}的规则价格不能为负数`)
        return
      }
    }
  }

  try {
    billingSaving.value = true
    // 构建要发送的数据，移除 UI 辅助字段
    const dataToSend: BillingRule = {
      channels: billingData.channels.map(channel => ({
        ...channel,
        rules: channel.rules.map(rule => {
          const { hours, minutes_ui, ...rest } = rule
          return rest
        })
      }))
    }
    await updateBillingRule(dataToSend)
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
  soundEnabled: 1,
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

// 系统参数配置表单
const systemFormRef = ref<FormInstance>()
const systemSaving = ref(false)
const systemForm = reactive<SystemConfig>({
  tableCount: 0,
  billingRule: { channels: [] },
  remindConfig: {
    threshold: 300,
    soundEnabled: 1,
    repeatInterval: 60,
    expiringCloseTime: 60,
    timeoutCloseTime: 120
  },
  sessionTimeout: 0,
  extendTime: 30,
  invalidOrderTime: 0,
  maxExtendCount: 0,
  autoSettleTime: 0
})

const systemRules: FormRules = {
  extendTime: [{ required: true, message: '请输入延长时间', trigger: 'blur' }],
  invalidOrderTime: [{ required: true, message: '请输入无效订单时间', trigger: 'blur' }]
}

// 加载系统参数配置
const loadSystemConfig = async () => {
  try {
    const configStr = await getSystemConfig()
    const config = JSON.parse(configStr)
    systemForm.extendTime = config.extendTime || 30
    systemForm.invalidOrderTime = config.invalidOrderTime ?? 0
  } catch (error) {
  }
}

// 保存系统参数配置
const saveSystemConfig = async () => {
  if (!systemFormRef.value) return

  try {
    await systemFormRef.value.validate()
    systemSaving.value = true

    await updateSystemConfig(systemForm)
    ElMessage.success('保存成功')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    systemSaving.value = false
  }
}

// 生命周期
onMounted(() => {
  loadBillingConfig()
  loadRemindConfig()
  loadSystemConfig()
  loadDiscountList()
  loadMemberLevels()
})

// ==================== 折扣设置相关 ====================

const discountList = ref<DiscountInfo[]>([])
const memberLevels = ref<MemberLevelInfo[]>([])
const discountDialogVisible = ref(false)
const discountDialogMode = ref<'add' | 'edit'>('add')
const discountFormRef = ref<FormInstance>()
const discountSaving = ref(false)
const editingDiscountId = ref<string>('')

const discountForm = reactive<CreateDiscountParams & { startTime?: number; endTime?: number }>({
  name: '',
  type: 1,
  discountRate: 0.9,
  minAmount: undefined,
  maxDiscount: undefined,
  memberLevelId: undefined,
  startTime: undefined,
  endTime: undefined,
  status: 1,
  sort: 0,
  description: ''
})

const discountRules: FormRules = {
  name: [{ required: true, message: '请输入折扣名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择折扣类型', trigger: 'change' }],
  discountRate: [{ required: true, message: '请输入折扣率', trigger: 'blur' }],
  memberLevelId: [
    {
      validator: (_rule, _value, callback) => {
        if (discountForm.type === 2 && !discountForm.memberLevelId) {
          callback(new Error('会员折扣必须选择会员等级'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 加载折扣列表
const loadDiscountList = async () => {
  try {
    const result = await getDiscountList({ page: 1, pageSize: 100 })
    discountList.value = result.list
  } catch (error) {
  }
}

// 加载会员等级列表
const loadMemberLevels = async () => {
  try {
    memberLevels.value = await getMemberLevelList()
  } catch (error) {
  }
}

// 显示添加折扣对话框
const showAddDiscountDialog = () => {
  discountDialogMode.value = 'add'
  editingDiscountId.value = ''
  Object.assign(discountForm, {
    name: '',
    type: 1,
    discountRate: 0.9,
    minAmount: undefined,
    maxDiscount: undefined,
    memberLevelId: undefined,
    startTime: undefined,
    endTime: undefined,
    status: 1,
    sort: 0,
    description: ''
  })
  discountDialogVisible.value = true
}

// 编辑折扣
const editDiscount = (discount: DiscountInfo) => {
  discountDialogMode.value = 'edit'
  editingDiscountId.value = discount.id
  Object.assign(discountForm, {
    name: discount.name,
    type: discount.type,
    discountRate: discount.discountRate,
    minAmount: discount.minAmount || undefined,
    maxDiscount: discount.maxDiscount || undefined,
    memberLevelId: discount.memberLevelId || undefined,
    startTime: discount.startTime ? discount.startTime / 1000 : undefined,
    endTime: discount.endTime ? discount.endTime / 1000 : undefined,
    status: discount.status,
    sort: discount.sort,
    description: discount.description || ''
  })
  discountDialogVisible.value = true
}

// 保存折扣
const saveDiscount = async () => {
  if (!discountFormRef.value) return

  try {
    await discountFormRef.value.validate()
    discountSaving.value = true

    const data: CreateDiscountParams | UpdateDiscountParams = {
      name: discountForm.name,
      type: discountForm.type,
      discountRate: discountForm.discountRate,
      minAmount: discountForm.minAmount,
      maxDiscount: discountForm.maxDiscount,
      memberLevelId: discountForm.memberLevelId,
      startTime: discountForm.startTime ? discountForm.startTime * 1000 : undefined,
      endTime: discountForm.endTime ? discountForm.endTime * 1000 : undefined,
      status: discountForm.status,
      sort: discountForm.sort || 0,
      description: discountForm.description
    }

    if (discountDialogMode.value === 'add') {
      await createDiscount(data as CreateDiscountParams)
      ElMessage.success('添加成功')
    } else {
      await updateDiscount(editingDiscountId.value, data as UpdateDiscountParams)
      ElMessage.success('更新成功')
    }

    discountDialogVisible.value = false
    loadDiscountList()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(discountDialogMode.value === 'add' ? '添加失败' : '更新失败')
    }
  } finally {
    discountSaving.value = false
  }
}

// 切换折扣状态
const toggleDiscountStatus = async (discount: DiscountInfo) => {
  const newStatus = discount.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'

  try {
    await updateDiscountStatus(discount.id, newStatus)
    ElMessage.success(`${statusText}成功`)
    loadDiscountList()
  } catch (error) {
    ElMessage.error(`${statusText}失败`)
  }
}

// 删除折扣
const deleteDiscount = (discount: DiscountInfo) => {
  ElMessageBox.confirm(
    `确定要删除"${discount.name}"折扣吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteDiscountApi(discount.id)
      ElMessage.success('删除成功')
      loadDiscountList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 处理开始时间变化
const handleStartTimeChange = (value: number | undefined) => {
  discountForm.startTime = value
}

// 处理结束时间变化
const handleEndTimeChange = (value: number | undefined) => {
  discountForm.endTime = value
}
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

.header-actions {
  display: flex;
  gap: 12px;
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

.rule-time,
.rule-price {
  display: flex;
  align-items: center;
}

.rule-display {
  margin-left: auto;
}

.rule-actions {
  display: flex;
  gap: 8px;
  margin-left: 16px;
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

/* 折扣设置样式 */
.discount-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.discount-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.discount-info {
  flex: 1;
}

.discount-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.discount-details {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.discount-rate {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
  margin-left: 8px;
}

.discount-condition {
  font-size: 13px;
  color: #909399;
}

.discount-desc {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.discount-actions {
  display: flex;
  gap: 8px;
}
</style>
