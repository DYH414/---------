const { request } = require('../../utils/request.js')
const { checkLogin } = require('../../utils/auth.js')

Page({
  data: {
    shopId: null,
    shopDetail: null,
    favorited: false,
    userScore: null
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ shopId: options.id })
      this.checkAuthAndLoad()
    }
  },

  /**
   * 检查登录并加载数据
   */
  async checkAuthAndLoad() {
    try {
      await checkLogin()
      this.loadShopDetail()
    } catch (err) {
      console.error('登录失败:', err)
    }
  },

  /**
   * 加载商家详情
   */
  async loadShopDetail() {
    try {
      const data = await request({
        url: `/mp/shops/${this.data.shopId}`
      })
      this.setData({
        shopDetail: data,
        favorited: data.favorited || false,
        userScore: data.userScore
      })
    } catch (err) {
      console.error('加载商家详情失败:', err)
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    }
  },

  /**
   * 收藏/取消收藏
   */
  async toggleFavorite() {
    try {
      if (this.data.favorited) {
        await request({
          url: `/mp/shops/${this.data.shopId}/favorite`,
          method: 'DELETE'
        })
        this.setData({ favorited: false })
        wx.showToast({
          title: '已取消收藏',
          icon: 'success'
        })
      } else {
        await request({
          url: `/mp/shops/${this.data.shopId}/favorite`,
          method: 'POST'
        })
        this.setData({ favorited: true })
        wx.showToast({
          title: '已收藏',
          icon: 'success'
        })
      }
    } catch (err) {
      console.error('收藏操作失败:', err)
    }
  },

  /**
   * 评分
   */
  async handleRating(e) {
    const score = e.currentTarget.dataset.score
    try {
      await request({
        url: `/mp/shops/${this.data.shopId}/rating`,
        method: 'POST',
        data: { score }
      })
      this.setData({ userScore: score })
      wx.showToast({
        title: '评分成功',
        icon: 'success'
      })
      // 重新加载详情以更新平均分
      this.loadShopDetail()
    } catch (err) {
      console.error('评分失败:', err)
    }
  },

  /**
   * 跳转到平台下单
   */
  goToPlatform(e) {
    const link = e.currentTarget.dataset.link
    const type = e.currentTarget.dataset.type
    
    if (type === 'MINI_PATH') {
      // 小程序内跳转
      wx.navigateTo({
        url: link.path
      })
    } else if (type === 'H5') {
      // H5 跳转
      wx.showModal({
        title: '提示',
        content: '将跳转到外部平台，是否继续？',
        success: (res) => {
          if (res.confirm) {
            // TODO: 使用 web-view 或复制链接
            wx.setClipboardData({
              data: link.url,
              success: () => {
                wx.showToast({
                  title: '链接已复制',
                  icon: 'success'
                })
              }
            })
          }
        }
      })
    }
  }
})
