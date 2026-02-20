/**
 * 通用类型定义
 */

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  total: number
  page: number
  pageSize: number
  list: T[]
}

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

export interface Category {
  id: number
  name: string
  logoUrl?: string
  sortOrder: number
  status: number
}

export interface RecommendConfig {
  bizDate: string
  mode: 'MANUAL' | 'RANDOM'
  manualShopIds?: number[]
  randomCount?: number
}

export interface RatingStats {
  shopId: number
  name: string
  categoryName: string
  avgScore: number
  ratingCount: number
  lastRatingTime?: string
}
