/**
 * 登录相关工具
 */
const { request } = require('./request.js')

/**
 * 微信登录
 */
const wxLogin = () => {
  return new Promise((resolve, reject) => {
    wx.login({
      success: (res) => {
        if (res.code) {
          // 调用后端登录接口
          request({
            url: '/mp/auth/login',
            method: 'POST',
            data: {
              code: res.code
            }
          }).then((data) => {
            // 保存 token
            wx.setStorageSync('token', data.token)
            resolve(data)
          }).catch((err) => {
            reject(err)
          })
        } else {
          reject(new Error('获取 code 失败'))
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

/**
 * 检查登录状态
 */
const checkLogin = () => {
  const token = wx.getStorageSync('token')
  if (!token) {
    return wxLogin()
  }
  return Promise.resolve()
}

module.exports = {
  wxLogin,
  checkLogin
}
