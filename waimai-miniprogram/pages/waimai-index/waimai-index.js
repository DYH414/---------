const { request } = require('../../utils/request.js')
const { checkLogin } = require('../../utils/auth.js')

Page({
  data: {
    dailyRecommend: [],
    shopList: []
  },

  onLoad() {
    this.checkAuthAndLoad()
  },

  /**
   * 检查登录并加载数据
   */
  async checkAuthAndLoad() {
    try {
      await checkLogin()
      this.loadDailyRecommend()
      this.loadShopList()
    } catch (err) {
      console.error('登录失败:', err)
    }
  },

  /**
   * 加载每日推荐
   */
  async loadDailyRecommend() {
    try {
      const data = await request({
        url: '/mp/recommend/daily'
      })
      this.setData({
        dailyRecommend: data.list || []
      })
    } catch (err) {
      console.error('加载每日推荐失败:', err)
    }
  },

  /**
   * 加载商家列表
   */
  async loadShopList() {
    try {
      const data = await request({
        url: '/mp/shops',
        data: {
          page: 1,
          pageSize: 10
        }
      })
      this.setData({
        shopList: data.list || []
      })
    } catch (err) {
      console.error('加载商家列表失败:', err)
    }
  },

  /**
   * 跳转到商家详情
   */
  goToDetail(e) {
    const shopId = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/waimai-detail/waimai-detail?id=${shopId}`
    })
  },

  /**
   * 跳转到商家列表
   */
  goToList() {
    wx.navigateTo({
      url: '/pages/waimai-list/waimai-list'
    })
  }
})
