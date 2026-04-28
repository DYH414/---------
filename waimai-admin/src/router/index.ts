import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/layouts/BasicLayout.vue'),
    redirect: '/shops',
    children: [
      {
        path: 'shops',
        name: 'ShopList',
        component: () => import('@/views/shop/ShopList.vue')
      },
      {
        path: 'shops/create',
        name: 'ShopCreate',
        component: () => import('@/views/shop/ShopEdit.vue')
      },
      {
        path: 'shops/:id/edit',
        name: 'ShopEdit',
        component: () => import('@/views/shop/ShopEdit.vue')
      },
      {
        path: 'categories',
        name: 'CategoryList',
        component: () => import('@/views/category/CategoryList.vue')
      },
      {
        path: 'recommend',
        name: 'RecommendConfig',
        component: () => import('@/views/recommend/RecommendConfig.vue')
      },
      {
        path: 'ratings',
        name: 'RatingStats',
        component: () => import('@/views/rating/RatingStats.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((_to, _from, next) => {
  // TODO: 实现登录校验逻辑
  next()
})

export default router
