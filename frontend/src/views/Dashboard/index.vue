<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card-wrapper" @click="navigateToReports">
          <div class="stat-card">
            <div class="stat-icon revenue-icon">
              <!-- 温柔营收图标 - 岛屿光点 -->
              <svg viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
                <ellipse cx="24" cy="30" rx="16" ry="10" fill="rgba(255,255,255,0.3)" transform="rotate(-10, 24, 30)"/>
                <circle cx="20" cy="28" r="3" fill="#fff" opacity="0.9"/>
                <circle cx="28" cy="32" r="4" fill="#fff" opacity="0.95"/>
                <circle cx="24" cy="24" r="2" fill="#fff" opacity="0.85"/>
                <line x1="20" y1="28" x2="28" y2="32" stroke="#fff" stroke-width="1" opacity="0.3"/>
              </svg>
            </div>
            <div class="stat-content">
              <div class="stat-title">今日营收</div>
              <div class="stat-value">¥{{ todayRevenue.toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card-wrapper" @click="navigateToTables">
          <div class="stat-card">
            <div class="stat-icon table-icon">
              <!-- 温柔桌台图标 - 岛屿群 -->
              <svg viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
                <ellipse cx="16" cy="32" rx="8" ry="6" fill="rgba(255,255,255,0.4)" transform="rotate(-10, 16, 32)"/>
                <ellipse cx="32" cy="32" rx="8" ry="6" fill="rgba(255,255,255,0.5)" transform="rotate(8, 32, 32)"/>
                <circle cx="16" cy="30" r="1.5" fill="#fff" opacity="0.9"/>
                <circle cx="32" cy="32" r="2" fill="#fff" opacity="0.95"/>
                <circle cx="24" cy="26" r="1" fill="#fff" opacity="0.85"/>
                <path d="M22 38 Q26 36 30 38" stroke="#fff" stroke-width="1" opacity="0.3" fill="none"/>
              </svg>
            </div>
            <div class="stat-content">
              <div class="stat-title">使用中桌台</div>
              <div class="stat-value">{{ usingTableCount }} <span v-if="totalTableCount > 0">/ {{ totalTableCount }}</span></div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card-wrapper" @click="navigateToOrders">
          <div class="stat-card">
            <div class="stat-icon order-icon">
              <!-- 温柔订单图标 - 收集的光点 -->
              <svg viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
                <circle cx="24" cy="30" r="12" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="1"/>
                <circle cx="20" cy="28" r="2" fill="#fff" opacity="0.9"/>
                <circle cx="28" cy="26" r="2.5" fill="#fff" opacity="0.95"/>
                <circle cx="24" cy="34" r="1.5" fill="#fff" opacity="0.85"/>
                <circle cx="30" cy="30" r="1.8" fill="#fff" opacity="0.88"/>
                <circle cx="18" cy="32" r="1.2" fill="#fff" opacity="0.82"/>
                <!-- 连接线 -->
                <line x1="20" y1="28" x2="28" y2="26" stroke="#fff" stroke-width="0.5" opacity="0.3"/>
                <line x1="20" y1="28" x2="24" y2="34" stroke="#fff" stroke-width="0.5" opacity="0.25"/>
                <line x1="28" y1="26" x2="24" y2="34" stroke="#fff" stroke-width="0.5" opacity="0.25"/>
              </svg>
            </div>
            <div class="stat-content">
              <div class="stat-title">今日订单</div>
              <div class="stat-value">{{ todayOrderCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon turnover-icon">
              <!-- 温柔翻台率图标 - 流动的光 -->
              <svg viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
                <circle cx="24" cy="30" r="10" fill="none" stroke="rgba(255,255,255,0.4)" stroke-width="1.5"/>
                <circle cx="24" cy="30" r="5" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="1.5"/>
                <!-- 流动的光点 -->
                <circle cx="24" cy="20" r="2" fill="#fff" opacity="0.95">
                  <animate attributeName="cy" values="20;40;20" dur="3s" repeatCount="indefinite"/>
                </circle>
                <circle cx="14" cy="34" r="1.5" fill="#fff" opacity="0.85">
                  <animate attributeName="cx" values="14;34;14" dur="4s" repeatCount="indefinite"/>
                  <animate attributeName="opacity" values="0.85;0.3;0.85" dur="4s" repeatCount="indefinite"/>
                </circle>
                <circle cx="34" cy="34" r="1.5" fill="#fff" opacity="0.85">
                  <animate attributeName="cx" values="34;14;34" dur="4s" repeatCount="indefinite"/>
                  <animate attributeName="opacity" values="0.85;0.3;0.85" dur="4s" repeatCount="indefinite"/>
                </circle>
                <!-- 中心点 -->
                <circle cx="24" cy="30" r="3" fill="#fff" opacity="0.98"/>
              </svg>
            </div>
            <div class="stat-content">
              <div class="stat-title">翻台率</div>
              <div class="stat-value">{{ turnoverRate }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 温柔主题规则展示区 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <div class="gentle-welcome-card">
          <!-- 动态背景层 -->
          <div class="gentle-bg">
            <!-- 浮动岛屿 -->
            <svg class="floating-island island-1" viewBox="0 0 200 120" xmlns="http://www.w3.org/2000/svg">
              <ellipse cx="100" cy="90" rx="70" ry="25" fill="rgba(212, 165, 255, 0.15)" transform="rotate(-5, 100, 90)"/>
              <ellipse cx="100" cy="85" rx="55" ry="18" fill="rgba(212, 165, 255, 0.2)" transform="rotate(-5, 100, 85)"/>
              <circle cx="80" cy="70" r="8" fill="rgba(255, 255, 255, 0.4)"/>
              <circle cx="110" cy="65" r="10" fill="rgba(255, 255, 255, 0.5)"/>
              <circle cx="95" cy="78" r="6" fill="rgba(255, 255, 255, 0.35)"/>
            </svg>
            <svg class="floating-island island-2" viewBox="0 0 160 100" xmlns="http://www.w3.org/2000/svg">
              <ellipse cx="80" cy="75" rx="50" ry="18" fill="rgba(255, 182, 217, 0.12)" transform="rotate(5, 80, 75)"/>
              <circle cx="70" cy="62" r="6" fill="rgba(255, 255, 255, 0.35)"/>
              <circle cx="90" cy="58" r="8" fill="rgba(255, 255, 255, 0.45)"/>
            </svg>
            <svg class="floating-island island-3" viewBox="0 0 140 90" xmlns="http://www.w3.org/2000/svg">
              <ellipse cx="70" cy="68" rx="45" ry="15" fill="rgba(197, 227, 255, 0.12)" transform="rotate(-3, 70, 68)"/>
              <circle cx="60" cy="55" r="5" fill="rgba(255, 255, 255, 0.3)"/>
              <circle cx="80" cy="52" r="7" fill="rgba(255, 255, 255, 0.4)"/>
            </svg>
            <!-- 波浪动画 -->
            <svg class="wave wave-1" viewBox="0 0 1440 100" xmlns="http://www.w3.org/2000/svg">
              <path d="M0,50 C240,80 480,20 720,50 C960,80 1200,20 1440,50 L1440,100 L0,100 Z" fill="rgba(212, 165, 255, 0.08)"/>
            </svg>
            <svg class="wave wave-2" viewBox="0 0 1440 100" xmlns="http://www.w3.org/2000/svg">
              <path d="M0,50 C240,20 480,80 720,50 C960,20 1200,80 1440,50 L1440,100 L0,100 Z" fill="rgba(255, 182, 217, 0.06)"/>
            </svg>
            <!-- 光点粒子 -->
            <div class="particles">
              <span v-for="i in 15" :key="i" class="particle" :style="{ '--delay': `${i * 0.3}s`, '--x': `${Math.random() * 100}%`, '--y': `${Math.random() * 100}%` }"></span>
            </div>
          </div>

          <!-- 内容层 -->
          <div class="welcome-content-layer">
            <div class="welcome-header">
              <svg class="welcome-logo" viewBox="0 0 80 80" xmlns="http://www.w3.org/2000/svg">
                <defs>
                  <linearGradient id="welcomeIslandGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#ffb6d9;stop-opacity:1" />
                    <stop offset="100%" style="stop-color:#d4a5ff;stop-opacity:1" />
                  </linearGradient>
                  <linearGradient id="welcomeWaterGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" style="stop-color:#98d4bb;stop-opacity:1" />
                    <stop offset="100%" style="stop-color:#c5e3ff;stop-opacity:1" />
                  </linearGradient>
                </defs>
                <path d="M10 55 Q25 48 40 55 Q55 62 70 55" fill="none" stroke="url(#welcomeWaterGrad)" stroke-width="2.5" opacity="0.5"/>
                <path d="M40 65 C28 65 20 52 20 40 C20 26 30 18 40 18 C50 18 60 26 60 40 C60 52 52 65 40 65Z" fill="url(#welcomeIslandGrad)"/>
                <circle cx="34" cy="38" r="3.5" fill="#fff" opacity="0.9"/>
                <circle cx="46" cy="36" r="4" fill="#fff" opacity="0.95"/>
                <line x1="34" y1="38" x2="46" y2="36" stroke="#fff" stroke-width="1.2" opacity="0.4"/>
              </svg>
              <h2 class="welcome-title">豆屿温柔集 · 拼豆手作体验馆</h2>
              <p class="welcome-subtitle">顾客须知与服务协议</p>
              <!-- 编辑按钮 -->
              <el-button
                v-if="canEditRules"
                type="primary"
                size="small"
                class="edit-rules-btn"
                @click="handleCreateRule"
              >
                <svg class="edit-icon" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                  <path d="M8 2a.5.5 0 0 1 .5.5v5h5a.5.5 0 0 1 0 1h-5v5a.5.5 0 0 1-1 0v-5h-5a.5.5 0 0 1 0-1h5v-5A.5.5 0 0 1 8 2Z" fill="#fff"/>
                </svg>
                新增规则
              </el-button>
            </div>

            <!-- 规则选项卡 -->
            <el-tabs v-model="activeTab" class="gentle-tabs" v-loading="loading">
              <!-- 套餐规则 -->
              <el-tab-pane label="套餐规则" name="packages">
                <div class="rule-content">
                  <template v-for="rule in rulesData.packages" :key="rule.id">
                    <div class="rule-section">
                      <!-- 编辑按钮 -->
                      <el-button
                        v-if="canEditRules"
                        type="primary"
                        size="small"
                        text
                        class="rule-edit-btn"
                        @click="handleEditRule(rule)"
                      >
                        <svg class="btn-icon" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                          <path d="M12.854 2.854a.5.5 0 0 0-.707 0L11 3.999 12.001 5l.853-.854a.5.5 0 0 0 0-.707l-.5-.5zM10.5 4.5l-7.5 7.5V13.5h1.5l7.5-7.5-1.5-1.5z" fill="currentColor"/>
                        </svg>
                        编辑
                      </el-button>
                      <!-- 表格类型 -->
                      <template v-if="rule.ruleType === RuleType.TABLE">
                        <h3>{{ rule.title || '基础套餐' }}</h3>
                        <el-table :data="packageList" stripe class="gentle-table">
                          <el-table-column prop="name" label="套餐名称" width="150"/>
                          <el-table-column prop="price" label="价格" width="100"/>
                          <el-table-column prop="content" label="包含内容"/>
                          <el-table-column prop="suitable" label="适合人群" width="120"/>
                          <el-table-column prop="duration" label="预计耗时" width="120"/>
                        </el-table>
                      </template>
                      <!-- 列表类型 -->
                      <template v-else-if="rule.ruleType === RuleType.LIST">
                        <h3>{{ rule.title }}</h3>
                        <div v-html="rule.content" class="html-content"></div>
                      </template>
                    </div>
                  </template>
                </div>
              </el-tab-pane>

              <!-- 增值服务 -->
              <el-tab-pane label="增值服务" name="services">
                <div class="rule-content">
                  <template v-for="rule in rulesData.services" :key="rule.id">
                    <div class="rule-section">
                      <!-- 编辑按钮 -->
                      <el-button
                        v-if="canEditRules"
                        type="primary"
                        size="small"
                        text
                        class="rule-edit-btn"
                        @click="handleEditRule(rule)"
                      >
                        <svg class="btn-icon" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                          <path d="M12.854 2.854a.5.5 0 0 0-.707 0L11 3.999 12.001 5l.853-.854a.5.5 0 0 0 0-.707l-.5-.5zM10.5 4.5l-7.5 7.5V13.5h1.5l7.5-7.5-1.5-1.5z" fill="currentColor"/>
                        </svg>
                        编辑
                      </el-button>
                      <!-- 表格类型 -->
                      <template v-if="rule.ruleType === RuleType.TABLE">
                        <h3>{{ rule.title || '付费升级服务' }}</h3>
                        <el-table :data="serviceList" stripe class="gentle-table">
                          <el-table-column prop="name" label="服务项目" width="150"/>
                          <el-table-column prop="price" label="价格" width="120"/>
                          <el-table-column prop="description" label="说明"/>
                        </el-table>
                      </template>
                      <!-- 列表类型 -->
                      <template v-else-if="rule.ruleType === RuleType.LIST">
                        <h3>{{ rule.title }}</h3>
                        <div v-html="rule.content" class="html-content"></div>
                      </template>
                      <!-- 特色服务类型 -->
                      <template v-else-if="rule.ruleType === RuleType.SPECIAL">
                        <div class="special-services">
                          <div class="special-item">
                            <span class="special-icon">✨</span>
                            <div v-html="rule.content" class="html-content"></div>
                          </div>
                        </div>
                      </template>
                    </div>
                  </template>
                </div>
              </el-tab-pane>

              <!-- 安全须知 -->
              <el-tab-pane label="安全须知" name="safety">
                <div class="rule-content">
                  <template v-for="rule in rulesData.safety" :key="rule.id">
                    <div class="rule-section">
                      <!-- 编辑按钮 -->
                      <el-button
                        v-if="canEditRules"
                        type="primary"
                        size="small"
                        text
                        class="rule-edit-btn"
                        @click="handleEditRule(rule)"
                      >
                        <svg class="btn-icon" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                          <path d="M12.854 2.854a.5.5 0 0 0-.707 0L11 3.999 12.001 5l.853-.854a.5.5 0 0 0 0-.707l-.5-.5zM10.5 4.5l-7.5 7.5V13.5h1.5l7.5-7.5-1.5-1.5z" fill="currentColor"/>
                        </svg>
                        编辑
                      </el-button>
                      <!-- 警告框类型 -->
                      <template v-if="rule.ruleType === RuleType.WARNING">
                        <div class="warning-box">
                          <span class="warning-icon">⚠️</span>
                          <h3>{{ rule.title || '安全警告' }}</h3>
                          <div v-html="rule.content" class="html-content"></div>
                        </div>
                      </template>
                      <!-- 列表类型 -->
                      <template v-else-if="rule.ruleType === RuleType.LIST">
                        <h4>{{ rule.title }}</h4>
                        <div v-html="rule.content" class="html-content"></div>
                      </template>
                    </div>
                  </template>
                </div>
              </el-tab-pane>

              <!-- 其他规定 -->
              <el-tab-pane label="其他规定" name="other">
                <div class="rule-content">
                  <template v-for="rule in rulesData.other" :key="rule.id">
                    <div class="rule-section">
                      <!-- 编辑按钮 -->
                      <el-button
                        v-if="canEditRules"
                        type="primary"
                        size="small"
                        text
                        class="rule-edit-btn"
                        @click="handleEditRule(rule)"
                      >
                        <svg class="btn-icon" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                          <path d="M12.854 2.854a.5.5 0 0 0-.707 0L11 3.999 12.001 5l.853-.854a.5.5 0 0 0 0-.707l-.5-.5zM10.5 4.5l-7.5 7.5V13.5h1.5l7.5-7.5-1.5-1.5z" fill="currentColor"/>
                        </svg>
                        编辑
                      </el-button>
                      <h3>{{ rule.title }}</h3>
                      <div v-html="rule.content" class="html-content"
                           :class="{ 'prohibited-list': rule.title?.includes('禁止') }"></div>
                    </div>
                  </template>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 规则编辑对话框 -->
    <StoreRuleDialog
      v-model="dialogVisible"
      :rule="currentRule"
      @success="handleRuleSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getTableList } from '@/api/table'
import { getActiveOrders } from '@/api/order'
import { getRulesByCategory, type StoreRule, RuleCategory, RuleType } from '@/api/storeRules'
import { type TableInfo } from '@/api/table'
import StoreRuleDialog from '@/components/StoreRuleDialog.vue'

const router = useRouter()
const userStore = useUserStore()

// 统计数据
const todayRevenue = ref(0)
const usingTableCount = ref(0)
const totalTableCount = ref(0)
const todayOrderCount = ref(0)
const turnoverRate = ref(0)

// 规则选项卡
const activeTab = ref('packages')

// 规则数据
const rulesData = ref<Record<string, StoreRule[]>>({
  packages: [],
  services: [],
  safety: [],
  other: []
})

// 加载中状态
const loading = ref(false)

// 套餐列表 - 从规则数据中解析
const packageList = computed(() => {
  const tableRule = rulesData.value.packages.find(r => r.ruleType === RuleType.TABLE)
  if (tableRule) {
    try {
      return JSON.parse(tableRule.content)
    } catch {
      return []
    }
  }
  return []
})

// 付费服务列表 - 从规则数据中解析
const serviceList = computed(() => {
  const tableRule = rulesData.value.services.find(r => r.ruleType === RuleType.TABLE)
  if (tableRule) {
    try {
      return JSON.parse(tableRule.content)
    } catch {
      return []
    }
  }
  return []
})

// 权限检查 - 是否可以编辑规则
const canEditRules = computed(() => {
  const permissions = userStore.permissions || []
  return permissions.includes('store:rules:update') || permissions.includes('*')
})

// 对话框状态
const dialogVisible = ref(false)
const currentRule = ref<StoreRule | null>(null)

// 打开新增规则对话框
const handleCreateRule = () => {
  currentRule.value = null
  dialogVisible.value = true
}

// 打开编辑规则对话框
const handleEditRule = (rule: StoreRule) => {
  currentRule.value = rule
  dialogVisible.value = true
}

// 规则保存成功回调
const handleRuleSaved = () => {
  loadRulesData()
}

// 加载规则数据
const loadRulesData = async () => {
  try {
    loading.value = true
    // 获取所有规则（不传category参数）
    const allRules = await getRulesByCategory()
    // 按分类分组
    rulesData.value = {
      packages: allRules.filter(r => r.category === RuleCategory.PACKAGES),
      services: allRules.filter(r => r.category === RuleCategory.SERVICES),
      safety: allRules.filter(r => r.category === RuleCategory.SAFETY),
      other: allRules.filter(r => r.category === RuleCategory.OTHER)
    }
  } catch (error) {
    // 失败时使用默认数据
    rulesData.value = {
      packages: [],
      services: [],
      safety: [],
      other: []
    }
  } finally {
    loading.value = false
  }
}

// 跳转到数据统计
const navigateToReports = () => {
  router.push('/reports')
}

// 跳转到桌台管理
const navigateToTables = () => {
  router.push('/tables')
}

// 跳转到订单管理
const navigateToOrders = () => {
  router.push('/orders')
}

// 加载工作台数据
const loadDashboardData = async () => {
  try {
    // 获取所有桌台
    const tables = await getTableList()

    // 只有成功获取到桌台数据时才更新统计
    if (tables && tables.length >= 0) {
      // 桌台总数（实际存在的桌台数量）
      totalTableCount.value = tables.length

      // 计算使用中的桌台数
      usingTableCount.value = tables.filter((t: TableInfo) => t.status === 'using').length
    }

    // 获取当天已完成的订单
    const todayStart = new Date()
    todayStart.setHours(0, 0, 0, 0)
    const todayStartTime = todayStart.getTime()

    const ordersResult = await getActiveOrders({ page: 1, pageSize: 1000 })
    const todayOrders = ordersResult.list || []

    // 计算今日营收
    todayRevenue.value = todayOrders.reduce((sum: number, order: any) => {
      if (order.createdAt >= todayStartTime) {
        return sum + (order.amount || 0)
      }
      return sum
    }, 0)

    // 今日订单数
    todayOrderCount.value = todayOrders.length

    // 计算翻台率（有桌台数据时才计算）
    if (totalTableCount.value > 0) {
      turnoverRate.value = Math.round((todayOrderCount.value / totalTableCount.value) * 100)
    }
  } catch (error) {
    // 发生错误时不更新统计数据
    console.error('加载工作台数据失败:', error)
  }
}

onMounted(() => {
  loadDashboardData()
  loadRulesData()
})
</script>

<style scoped lang="scss">
.dashboard {
  .stat-card-wrapper {
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 16px rgba(212, 165, 255, 0.2);
    }

    &:active {
      transform: translateY(-2px);
    }
  }

  .stat-card {
    display: flex;
    align-items: center;
    gap: 16px;

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;

      svg {
        width: 36px;
        height: 36px;
      }

      &.revenue-icon {
        background: linear-gradient(135deg, #ffb6d9, #d4a5ff);
        box-shadow: 0 4px 12px rgba(212, 165, 255, 0.3);
      }

      &.table-icon {
        background: linear-gradient(135deg, #98d4bb, #7dd3b0);
        box-shadow: 0 4px 12px rgba(152, 216, 187, 0.3);
      }

      &.order-icon {
        background: linear-gradient(135deg, #c5e3ff, #9ac0ff);
        box-shadow: 0 4px 12px rgba(154, 192, 255, 0.3);
      }

      &.turnover-icon {
        background: linear-gradient(135deg, #ffd993, #ffb86c);
        box-shadow: 0 4px 12px rgba(255, 217, 147, 0.3);
      }
    }

    .stat-content {
      flex: 1;

      .stat-title {
        font-size: 14px;
        color: #999;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        background: linear-gradient(135deg, #d4a5ff 0%, #ffb6d9 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
    }
  }

  // 温柔主题欢迎卡片
  .gentle-welcome-card {
    position: relative;
    background: linear-gradient(135deg, rgba(255, 240, 245, 0.9) 0%, rgba(243, 231, 255, 0.9) 50%, rgba(232, 244, 255, 0.9) 100%);
    border-radius: 24px;
    overflow: hidden;
    box-shadow: 0 8px 32px rgba(212, 165, 255, 0.15);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.5);
    min-height: 600px;

    // 动态背景层
    .gentle-bg {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      overflow: hidden;
      pointer-events: none;

      // 浮动岛屿
      .floating-island {
        position: absolute;
        opacity: 0.6;

        &.island-1 {
          top: 10%;
          right: 5%;
          width: 180px;
          animation: float 8s ease-in-out infinite;
        }

        &.island-2 {
          bottom: 25%;
          left: 3%;
          width: 140px;
          animation: float 10s ease-in-out infinite 1s;
        }

        &.island-3 {
          top: 40%;
          left: 8%;
          width: 120px;
          animation: float 12s ease-in-out infinite 2s;
        }
      }

      // 波浪
      .wave {
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 80px;

        &.wave-1 {
          animation: wave 8s linear infinite;
        }

        &.wave-2 {
          bottom: -10px;
          animation: wave 10s linear infinite reverse;
        }
      }

      // 光点粒子
      .particles {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;

        .particle {
          position: absolute;
          width: 4px;
          height: 4px;
          background: radial-gradient(circle, rgba(212, 165, 255, 0.8), transparent);
          border-radius: 50%;
          left: var(--x);
          top: var(--y);
          animation: particleFloat 6s ease-in-out infinite;
          animation-delay: var(--delay);
        }
      }
    }

    // 内容层
    .welcome-content-layer {
      position: relative;
      z-index: 1;
      padding: 30px;
    }

    .welcome-header {
      text-align: center;
      margin-bottom: 30px;
      position: relative;

      .welcome-logo {
        width: 70px;
        height: 70px;
        margin-bottom: 15px;
        animation: gentlePulse 4s ease-in-out infinite;
      }

      .welcome-title {
        font-size: 24px;
        font-weight: 600;
        background: linear-gradient(135deg, #d4a5ff 0%, #ffb6d9 50%, #98d4bb 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        margin: 10px 0;
      }

      .welcome-subtitle {
        font-size: 14px;
        color: #999;
        margin-bottom: 20px;
      }

      // 新增规则按钮
      .edit-rules-btn {
        position: absolute;
        top: 0;
        right: 0;
        background: linear-gradient(135deg, #d4a5ff 0%, #ffb6d9 100%);
        border: none;
        color: #fff;
        padding: 8px 16px;
        border-radius: 20px;
        font-size: 13px;
        display: flex;
        align-items: center;
        gap: 6px;
        transition: all 0.3s ease;

        .edit-icon {
          width: 14px;
          height: 14px;
        }

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(212, 165, 255, 0.4);
        }
      }
    }

    // 温柔主题选项卡
    .gentle-tabs {
      :deep(.el-tabs__header) {
        margin-bottom: 25px;
      }

      :deep(.el-tabs__nav-wrap::after) {
        display: none;
      }

      :deep(.el-tabs__item) {
        color: #999;
        font-size: 15px;
        padding: 0 25px;
        transition: all 0.3s ease;

        &:hover {
          color: #d4a5ff;
        }

        &.is-active {
          color: #d4a5ff;
          font-weight: 500;
        }
      }

      :deep(.el-tabs__active-bar) {
        background: linear-gradient(90deg, #d4a5ff, #ffb6d9);
        height: 3px;
        border-radius: 2px;
      }

      :deep(.el-tabs__content) {
        max-height: 420px;
        overflow-y: auto;
        padding-right: 10px;

        // 自定义滚动条
        &::-webkit-scrollbar {
          width: 6px;
        }

        &::-webkit-scrollbar-track {
          background: rgba(212, 165, 255, 0.1);
          border-radius: 3px;
        }

        &::-webkit-scrollbar-thumb {
          background: linear-gradient(180deg, #d4a5ff, #ffb6d9);
          border-radius: 3px;
        }
      }
    }

    .rule-content {
      // 规则区块
      .rule-section {
        position: relative;
        margin-bottom: 20px;

        // 编辑按钮
        .rule-edit-btn {
          position: absolute;
          top: 0;
          right: 0;
          color: #fff;
          padding: 4px 8px;
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          border-radius: 12px;
          transition: all 0.3s ease;

          .btn-icon {
            width: 14px;
            height: 14px;
          }

          &:hover {
            background: rgba(212, 165, 255, 0.15);
          }
        }
      }

      h3, h4 {
        color: #666;
        margin: 20px 0 12px;
        font-size: 16px;
        font-weight: 500;

        &:first-child {
          margin-top: 0;
        }
      }

      h4 {
        font-size: 14px;
        margin: 15px 0 8px;
      }

      // HTML内容样式
      .html-content {
        color: #777;
        font-size: 14px;
        line-height: 1.8;

        ul {
          margin: 10px 0;
          padding-left: 20px;

          li {
            margin: 6px 0;

            strong {
              color: #999;
              font-weight: 500;
            }
          }
        }

        p {
          margin: 8px 0;
        }
      }

      // 禁止事项列表
      .prohibited-list {
        ul {
          list-style: none;
          padding-left: 0;

          li {
            padding: 8px 12px;
            background: rgba(255, 182, 217, 0.15);
            border-radius: 8px;
            border-left: 3px solid #ffb6d9;
            margin-bottom: 8px;
          }
        }
      }

      .rule-list {
        margin: 10px 0;
        padding-left: 20px;

        li {
          color: #777;
          font-size: 14px;
          line-height: 1.8;
          margin: 6px 0;

          strong {
            color: #999;
            font-weight: 500;
          }

          &.prohibited {
            list-style: none;
            padding-left: 0;

            li {
              padding: 8px 12px;
              background: rgba(255, 182, 217, 0.15);
              border-radius: 8px;
              border-left: 3px solid #ffb6d9;
            }
          }
        }
      }

      .gentle-table {
        margin: 15px 0;
        border-radius: 12px;
        overflow: hidden;

        :deep(.el-table__header-wrapper) {
          th {
            background: linear-gradient(135deg, rgba(212, 165, 255, 0.1), rgba(255, 182, 217, 0.1));
            color: #666;
            font-weight: 500;
          }
        }

        :deep(.el-table__body) {
          tr {
            &:hover {
              background: rgba(212, 165, 255, 0.05);
            }
          }

          td {
            color: #777;
          }
        }
      }

      .warning-box {
        background: linear-gradient(135deg, rgba(255, 182, 217, 0.2), rgba(255, 217, 147, 0.2));
        border-radius: 16px;
        padding: 20px;
        margin-bottom: 20px;
        border: 1px solid rgba(255, 182, 217, 0.3);
        text-align: center;

        .warning-icon {
          font-size: 32px;
          display: block;
          margin-bottom: 10px;
        }

        h3 {
          margin: 10px 0;
          color: #e67c7c;
        }

        p {
          color: #888;
          font-size: 13px;
          margin: 0;
        }
      }

      .special-services {
        margin-top: 20px;

        .special-item {
          display: flex;
          gap: 15px;
          padding: 18px;
          background: rgba(212, 165, 255, 0.08);
          border-radius: 16px;
          margin-bottom: 12px;
          border: 1px solid rgba(212, 165, 255, 0.15);

          .special-icon {
            font-size: 28px;
            flex-shrink: 0;
          }

          strong {
            color: #888;
            font-size: 15px;
            display: block;
            margin-bottom: 6px;
          }

          p {
            color: #999;
            font-size: 13px;
            line-height: 1.6;
            margin: 0;
          }
        }
      }
    }
  }
}

// 动画定义
@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-15px) rotate(3deg);
  }
}

@keyframes wave {
  0% {
    transform: translateX(0);
  }
  50% {
    transform: translateX(-25px);
  }
  100% {
    transform: translateX(0);
  }
}

@keyframes particleFloat {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-20px) scale(1.3);
    opacity: 1;
  }
}

@keyframes gentlePulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}
</style>
