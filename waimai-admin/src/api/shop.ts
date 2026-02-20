import request from './request'
import type { PageResult, Shop } from './types'

/**
 * 获取商家列表
 */
export function getShopList(params: {
  page?: number
  pageSize?: number
  platform?: string
  categoryId?: number
  status?: number
  keyword?: string
  sort?: string
}) {
  return request.get<PageResult<Shop>>('/admin/shops', { params })
}

/**
 * 获取商家详情
 */
export function getShopDetail(id: number) {
  return request.get<Shop>(`/admin/shops/${id}`)
}

/**
 * 新增商家
 */
export function createShop(data: any) {
  return request.post('/admin/shops', data)
}

/**
 * 更新商家
 */
export function updateShop(id: number, data: any) {
  return request.put(`/admin/shops/${id}`, data)
}

/**
 * 删除商家
 */
export function deleteShop(id: number) {
  return request.delete(`/admin/shops/${id}`)
}

/**
 * 更新商家状态
 */
export function updateShopStatus(id: number, status: number) {
  return request.post(`/admin/shops/${id}/status`, { status })
}
