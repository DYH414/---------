<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider v-model:collapsed="collapsed" theme="light">
      <div class="logo">外卖管理</div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="inline"
        :items="menuItems"
        @click="handleMenuClick"
      />
    </a-layout-sider>
    <a-layout>
      <a-layout-header style="background: #fff; padding: 0 24px">
        <div style="display: flex; justify-content: space-between; align-items: center">
          <h2>校园外卖聚合平台</h2>
          <a-button @click="handleLogout">退出登录</a-button>
        </div>
      </a-layout-header>
      <a-layout-content style="margin: 24px; padding: 24px; background: #fff">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const collapsed = ref(false)
const selectedKeys = ref<string[]>(['shops'])

const menuItems = [
  { key: 'shops', label: '商家管理', icon: 'shop' },
  { key: 'categories', label: '分类管理', icon: 'appstore' },
  { key: 'recommend', label: '每日推荐', icon: 'star' },
  { key: 'ratings', label: '评分统计', icon: 'like' }
]

const handleMenuClick = ({ key }: { key: string }) => {
  router.push(`/${key}`)
}

const handleLogout = () => {
  userStore.clearToken()
  router.push('/login')
}
</script>

<style scoped>
.logo {
  height: 64px;
  line-height: 64px;
  text-align: center;
  font-weight: bold;
  font-size: 18px;
}
</style>
