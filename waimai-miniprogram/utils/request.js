/**
 * 网络请求封装
 */
const config = require('../config/env.js')

const request = (options) => {
  return new Promise((resolve, reject) => {
    // 获取 token
    const token = wx.getStorageSync('token')
    
    wx.request({
      url: config.baseURL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 0) {
            resolve(res.data.data)
          } else {
            // 处理业务错误
            if (res.data.code === 2000 || res.data.code === 2001 || res.data.code === 2002) {
              // token 相关错误，跳转登录
              wx.removeStorageSync('token')
              wx.reLaunch({
                url: '/pages/login/login'
              })
            } else {
              wx.showToast({
                title: res.data.message || '请求失败',
                icon: 'none'
              })
            }
            reject(res.data)
          }
        } else {
          wx.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err) => {
        wx.showToast({
          title: '网络请求失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

module.exports = {
  request
}
