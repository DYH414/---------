<template>
  <div class="login-container">
    <a-card title="管理员登录" style="width: 400px">
      <a-form :model="form" @submit="handleLogin">
        <a-form-item label="用户名">
          <a-input v-model:value="form.username" />
        </a-form-item>
        <a-form-item label="密码">
          <a-input-password v-model:value="form.password" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" block>登录</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  try {
    const res = await adminLogin(form.username, form.password)
    userStore.setToken(res.token)
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f0f2f5;
}
</style>
