import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const request: AxiosInstance = axios.create({
  baseURL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // TODO: 添加 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code === 0) {
      return res.data
    } else {
      // TODO: 统一错误处理
      console.error('请求错误:', res.message)
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    // TODO: 统一错误处理
    if (error.response?.status === 401) {
      // 未登录，跳转到登录页
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
