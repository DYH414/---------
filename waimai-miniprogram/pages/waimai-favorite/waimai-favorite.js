const { request } = require('../../utils/request.js')
const { checkLogin } = require('../../utils/auth.js')

Page({
  data: {
    favoriteList: [],
    page: 1,
    pageSize: 10,
    hasMore: true,
    loading: false
  },

  onLoad() {
    this.checkAuthAndLoad()
  },

  onShow() {
    // 页面显示时刷新列表
    this.loadFavoriteList(true)
  },

  /**
   * 检查登录并加载数据
   */
  async checkAuthAndLoad() {
    try {
      await checkLogin()
      this.loadFavoriteList(true)
    } catch (err) {
      console.error('登录失败:', err)
    }
  },

  /**
   * 加载收藏列表
   */
  async loadFavoriteList(refresh = false) {
    if (this.data.loading) return
    
    this.setData({ loading: true })
    
    try {
      const page = refresh ? 1 : this.data.page
      const data = await request({
        url: '/mp/user/favorites',
        data: {
          page,
          pageSize: this.data.pageSize
        }
      })
      
      const newList = refresh ? data.list : [...this.data.favoriteList, ...data.list]
      this.setData({
        favoriteList: newList,
        page: page + 1,
        hasMore: data.list.length >= this.data.pageSize,
        loading: false
      })
    } catch (err) {
      console.error('加载收藏列表失败:', err)
      this.setData({ loading: false })
    }
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh() {
    this.loadFavoriteList(true).then(() => {
      wx.stopPullDownRefresh()
    })
  },

  /**
   * 上拉加载更多
   */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadFavoriteList()
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
   * 取消收藏
   */
  async cancelFavorite(e) {
    const shopId = e.currentTarget.dataset.id
    try {
      await request({
        url: `/mp/shops/${shopId}/favorite`,
        method: 'DELETE'
      })
      wx.showToast({
        title: '已取消收藏',
        icon: 'success'
      })
      // 重新加载列表
      this.loadFavoriteList(true)
    } catch (err) {
      console.error('取消收藏失败:', err)
    }
  }
})
