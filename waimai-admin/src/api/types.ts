/**
 * API 响应类型定义
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/**
 * 分页结果类型
 */
export interface PageResult<T> {
  total: number
  page: number
  pageSize: number
  list: T[]
}

/**
 * 商家信息
 */
export interface Shop {
  id: number
  name: string
  introduction?: string
  categoryId: number
  categoryName?: string
  coverUrl?: string
  startingPrice?: number
  deliveryFeeDesc?: string
  status: number
  sortWeight: number
  createTime: string
  updateTime: string
}

/**
 * 分类信息
 */
export interface Category {
  id: number
  name: string
  logoUrl?: string
  sortOrder: number
  status: number
}
