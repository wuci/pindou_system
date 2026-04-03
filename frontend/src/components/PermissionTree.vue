<template>
  <el-tree
    ref="treeRef"
    :data="permissionTree"
    :props="treeProps"
    show-checkbox
    node-key="permissionKey"
    :default-checked-keys="modelValue"
    @check="handleCheck"
    :loading="loading"
  >
    <template #default="{ node, data }">
      <span class="tree-node">
        <el-icon v-if="data.icon && iconMap[data.icon]" style="margin-right: 5px">
          <component :is="iconMap[data.icon]" />
        </el-icon>
        <span>{{ node.label }}</span>
      </span>
    </template>
  </el-tree>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import type { ElTree } from 'element-plus'
import { Odometer, Grid, Document, DataAnalysis, User, Lock, Notebook, Star, Tools } from '@element-plus/icons-vue'
import { getPermissionTree, type PermissionResponse } from '@/api/permission'
import { ElMessage } from 'element-plus'

interface Props {
  modelValue: string[]
}

interface Emits {
  (e: 'update:modelValue', value: string[]): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const treeRef = ref<InstanceType<typeof ElTree>>()
const loading = ref(false)
const permissionTree = ref<PermissionResponse[]>([])

const treeProps = {
  children: 'children',
  label: 'permissionName'
}

// 图标组件映射
const iconMap: Record<string, any> = {
  Odometer,
  Grid,
  Document,
  DataAnalysis,
  User,
  Lock,
  Notebook,
  Star,
  Tools
}

/**
 * 加载权限树数据
 */
const loadPermissionTree = async () => {
  loading.value = true
  try {
    const tree = await getPermissionTree()
    permissionTree.value = tree
  } catch (error) {
    ElMessage.error('加载权限配置失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleCheck = () => {
  if (!treeRef.value) return
  // 获取所有选中的节点（包括父节点），不只是叶子节点
  const checkedKeys = treeRef.value.getCheckedKeys(false)
  emit('update:modelValue', checkedKeys as string[])
}

// 监听 modelValue 变化，同步更新树的选中状态
watch(() => props.modelValue, (newVal) => {
  if (treeRef.value && newVal) {
    treeRef.value.setCheckedKeys(newVal)
  }
}, { immediate: false })

// 组件挂载时加载数据
onMounted(() => {
  loadPermissionTree()
})

// 暴露方法供父组件调用
defineExpose({
  treeRef,
  loadPermissionTree
})
</script>

<script lang="ts">
export default {
  name: 'PermissionTree'
}
</script>

<style scoped>
.tree-node {
  display: flex;
  align-items: center;
  font-size: 14px;
}

:deep(.el-tree-node__content) {
  height: 32px;
}
</style>
