<template>
  <el-tree
    ref="treeRef"
    :data="permissionTree"
    :props="treeProps"
    show-checkbox
    node-key="key"
    :default-checked-keys="modelValue"
    @check="handleCheck"
  >
    <template #default="{ node, data }">
      <span class="tree-node">
        <el-icon v-if="data.icon" style="margin-right: 5px">
          <component :is="data.icon" />
        </el-icon>
        <span>{{ node.label }}</span>
      </span>
    </template>
  </el-tree>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { ElTree } from 'element-plus'
import { PermissionTreeNode } from '@/api/role'

interface Props {
  modelValue: string[]
}

interface Emits {
  (e: 'update:modelValue', value: string[]): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const treeRef = ref<InstanceType<typeof ElTree>>()

const treeProps = {
  children: 'children',
  label: 'label'
}

/**
 * 权限树数据
 */
const permissionTree: PermissionTreeNode[] = [
  {
    id: 'user',
    label: '用户管理',
    children: [
      { id: 'user-view', label: '查看用户', key: 'user:view' },
      { id: 'user-create', label: '新增用户', key: 'user:create' },
      { id: 'user-update', label: '编辑用户', key: 'user:update' },
      { id: 'user-delete', label: '删除用户', key: 'user:delete' },
      { id: 'user-reset', label: '重置密码', key: 'user:resetPassword' }
    ]
  },
  {
    id: 'role',
    label: '角色管理',
    children: [
      { id: 'role-view', label: '查看角色', key: 'role:view' },
      { id: 'role-create', label: '新增角色', key: 'role:create' },
      { id: 'role-update', label: '编辑角色', key: 'role:update' },
      { id: 'role-delete', label: '删除角色', key: 'role:delete' }
    ]
  },
  {
    id: 'table',
    label: '桌台管理',
    children: [
      { id: 'table-view', label: '查看桌台', key: 'table:view' },
      { id: 'table-create', label: '新增桌台', key: 'table:create' },
      { id: 'table-update', label: '编辑桌台', key: 'table:update' },
      { id: 'table-delete', label: '删除桌台', key: 'table:delete' }
    ]
  },
  {
    id: 'order',
    label: '订单管理',
    children: [
      { id: 'order-view', label: '查看订单', key: 'order:view' },
      { id: 'order-create', label: '创建订单', key: 'order:create' },
      { id: 'order-update', label: '编辑订单', key: 'order:update' },
      { id: 'order-delete', label: '删除订单', key: 'order:delete' },
      { id: 'order-export', label: '导出订单', key: 'order:export' }
    ]
  },
  {
    id: 'config',
    label: '配置管理',
    children: [
      { id: 'config-view', label: '查看配置', key: 'config:view' },
      { id: 'config-update', label: '修改配置', key: 'config:update' }
    ]
  },
  {
    id: 'statistics',
    label: '统计报表',
    children: [
      { id: 'statistics-view', label: '查看统计', key: 'statistics:view' }
    ]
  }
]

const handleCheck = () => {
  if (!treeRef.value) return
  const checkedKeys = treeRef.value.getCheckedKeys()
  emit('update:modelValue', checkedKeys as string[])
}

// 暴露方法供父组件调用
defineExpose({
  treeRef
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
