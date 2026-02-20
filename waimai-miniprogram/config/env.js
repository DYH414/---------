/**
 * 环境配置
 */
const config = {
  // 开发环境
  dev: {
    baseURL: 'http://localhost:8080/api'
  },
  // 生产环境
  prod: {
    baseURL: 'https://api.yourdomain.com/api'
  }
}

// 当前环境（根据实际情况修改）
const env = 'dev'

module.exports = config[env]
