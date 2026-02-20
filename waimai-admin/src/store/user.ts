import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const username = ref<string>('')

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function clearToken() {
    token.value = null
    localStorage.removeItem('token')
  }

  function setUsername(name: string) {
    username.value = name
  }

  return {
    token,
    username,
    setToken,
    clearToken,
    setUsername
  }
})
