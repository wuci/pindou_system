/**
 * 通用类型定义
 */

/**
 * API响应结构
 */
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data?: T;
  timestamp?: number;
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages?: number;
}

/**
 * 分页查询参数
 */
export interface PageParams {
  page?: number;
  pageSize?: number;
}

/**
 * 用户信息
 */
export interface UserInfo {
  id: string;
  username: string;
  nickname: string;
  roleId: string;
  roleName: string;
  permissions: string[];
  avatar?: string | null;
}

/**
 * 桌台状态
 */
export type TableStatus = 'idle' | 'using' | 'paused';

/**
 * 订单状态
 */
export type OrderStatus = 'active' | 'completed';

/**
 * 用户状态
 */
export type UserStatus = 1 | 0;

/**
 * 性别
 */
export type Gender = 'male' | 'female' | 'unknown';
