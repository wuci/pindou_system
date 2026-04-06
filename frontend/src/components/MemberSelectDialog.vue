<template>
  <el-dialog
    v-model="dialogVisible"
    title="选择会员"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="search-container">
      <el-input
        v-model="keyword"
        placeholder="输入手机号或姓名搜索会员"
        clearable
        @input="handleSearchInput"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <div v-loading="loading" class="member-list">
      <div
        v-for="member in memberList"
        :key="member.id"
        class="member-item"
        :class="{ selected: selectedMember?.id === member.id }"
        @click="handleSelectMember(member)"
      >
        <div class="member-info">
          <div class="member-name">{{ member.name }}</div>
          <div class="member-phone">{{ member.phone }}</div>
        </div>
        <div class="member-level">
          <el-tag type="success" size="small">{{ member.levelName }}</el-tag>
          <div class="member-discount">{{ (member.discountRate * 10).toFixed(1) }}折</div>
        </div>
        <div class="member-amount">
          <div class="label">累计消费</div>
          <div class="value">¥{{ member.totalAmount.toFixed(2) }}</div>
        </div>
      </div>

      <el-empty v-if="!loading && memberList.length === 0" description="暂无会员数据" />
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        small
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          @click="handleConfirm"
          :disabled="!selectedMember"
          :loading="confirming"
        >
          确认选择
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getMemberList, type MemberInfo } from '@/api/member'

interface Props {
  modelValue: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'selected', member: MemberInfo): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const confirming = ref(false)
const keyword = ref('')
const memberList = ref<MemberInfo[]>([])
const selectedMember = ref<MemberInfo | null>(null)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 防抖定时器
let searchTimer: NodeJS.Timeout | null = null

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      selectedMember.value = null
      keyword.value = ''
      page.value = 1
      // 打开弹窗时自动加载第一页会员
      loadMembers()
    }
  }
)

const loadMembers = async () => {
  loading.value = true
  try {
    const result = await getMemberList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    })
    memberList.value = result.list
    total.value = result.total
  } catch (error) {
    ElMessage.error('加载会员列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearchInput = () => {
  // 防抖搜索
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    page.value = 1
    loadMembers()
  }, 300)
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  loadMembers()
}

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  page.value = 1
  loadMembers()
}

const handleSelectMember = (member: MemberInfo) => {
  selectedMember.value = member
}

const handleConfirm = () => {
  if (selectedMember.value) {
    emit('selected', selectedMember.value)
    emit('update:modelValue', false)
  }
}

const handleClose = () => {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.search-container {
  margin-bottom: 20px;
}

.member-list {
  max-height: 400px;
  min-height: 200px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin-bottom: 16px;
}

.member-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.3s;
}

.member-item:hover {
  background-color: #f5f7fa;
}

.member-item.selected {
  background-color: #ecf5ff;
  border-color: #409eff;
}

.member-item:last-child {
  border-bottom: none;
}

.member-info {
  flex: 1;
}

.member-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.member-phone {
  font-size: 14px;
  color: #909399;
}

.member-level {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 20px;
}

.member-discount {
  font-size: 14px;
  color: #e6a23c;
  font-weight: 600;
  margin-top: 4px;
}

.member-amount {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  min-width: 100px;
}

.member-amount .label {
  font-size: 12px;
  color: #909399;
}

.member-amount .value {
  font-size: 16px;
  font-weight: 600;
  color: #67c23a;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 10px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
