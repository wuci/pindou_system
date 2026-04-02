<template>
  <el-dialog
    v-model="dialogVisible"
    title="会员充值"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="member" class="member-info">
      <div class="info-item">
        <span class="label">会员姓名：</span>
        <span class="value">{{ member.name }}</span>
      </div>
      <div class="info-item">
        <span class="label">手机号码：</span>
        <span class="value">{{ member.phone }}</span>
      </div>
      <div class="info-item">
        <span class="label">当前余额：</span>
        <span class="value balance">¥{{ member.balance.toFixed(2) }}</span>
      </div>
      <div class="info-item">
        <span class="label">会员等级：</span>
        <el-tag type="success" size="small">{{ member.levelName }}</el-tag>
        <span style="margin-left: 8px; color: #e6a23c;">{{ (member.discountRate * 10).toFixed(1) }}折</span>
      </div>
    </div>

    <el-divider />

    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="充值金额" prop="amount">
        <el-input-number
          v-model="formData.amount"
          :min="0.01"
          :max="10000"
          :precision="2"
          :step="10"
          style="width: 100%"
        />
        <div class="form-tip">
          快速选择：
          <el-button size="small" @click="formData.amount = 50">50元</el-button>
          <el-button size="small" @click="formData.amount = 100">100元</el-button>
          <el-button size="small" @click="formData.amount = 200">200元</el-button>
          <el-button size="small" @click="formData.amount = 500">500元</el-button>
        </div>
      </el-form-item>

      <el-form-item label="支付方式" prop="paymentMethod">
        <el-radio-group v-model="formData.paymentMethod">
          <el-radio label="cash">现金</el-radio>
          <el-radio label="wechat">微信支付</el-radio>
          <el-radio label="alipay">支付宝</el-radio>
          <el-radio label="card">刷卡</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="充值后余额">
        <span class="balance-after">¥{{ balanceAfter.toFixed(2) }}</span>
      </el-form-item>

      <el-form-item label="备注">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="2"
          placeholder="请输入备注（选填）"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确认充值
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { recharge, type MemberInfo } from '@/api/member'

interface Props {
  modelValue: boolean
  member: MemberInfo | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const formData = ref({
  amount: 100,
  paymentMethod: 'cash',
  remark: ''
})

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const balanceAfter = computed(() => {
  if (!props.member) return 0
  return props.member.balance + (formData.value.amount || 0)
})

const formRules: FormRules = {
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '充值金额必须大于0', trigger: 'blur' }
  ],
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ]
}

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      resetForm()
    }
  }
)

const handleSubmit = async () => {
  if (!formRef.value || !props.member) return

  try {
    await formRef.value.validate()
    submitting.value = true

    await recharge(props.member.id, {
      amount: formData.value.amount,
      paymentMethod: formData.value.paymentMethod,
      remark: formData.value.remark
    })

    ElMessage.success(`充值成功！充值金额：¥${formData.value.amount.toFixed(2)}`)
    emit('success')
    emit('update:modelValue', false)
  } catch (error) {
    if (error !== false) {
      ElMessage.error('充值失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  emit('update:modelValue', false)
}

const resetForm = () => {
  formData.value = {
    amount: 100,
    paymentMethod: 'cash',
    remark: ''
  }
  formRef.value?.resetFields()
}
</script>

<style scoped>
.member-info {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  font-size: 14px;
  color: #606266;
  width: 100px;
}

.info-item .value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.info-item .value.balance {
  font-size: 18px;
  color: #67c23a;
  font-weight: 600;
}

.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.balance-after {
  font-size: 18px;
  font-weight: 600;
  color: #e6a23c;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
