const { request } = require('../../utils/request.js')
const { checkLogin } = require('../../utils/auth.js')

Page({
  data: {
    shopList: [],
    page: 1,
    pageSize: 10,
    hasMore: true,
    loading: false,
    platform: '',
    categoryId: null,
    keyword: ''
  },

  onLoad(options) {
    if (options.platform) {
      this.setData({ platform: options.platform })
    }
    if (options.categoryId) {
      this.setData({ categoryId: options.categoryId })
    }
    this.checkAuthAndLoad()
  },

  /**
   * 检查登录并加载数据
   */
  async checkAuthAndLoad() {
    try {
      await checkLogin()
      this.loadShopList(true)
    } catch (err) {
      console.error('登录失败:', err)
    }
  },

  /**
   * 加载商家列表
   */
  async loadShopList(refresh = false) {
    if (this.data.loading) return
    
    this.setData({ loading: true })
    
    try {
      const page = refresh ? 1 : this.data.page
      const data = await request({
        url: '/mp/shops',
        data: {
          page,
          pageSize: this.data.pageSize,
          platform: this.data.platform || undefined,
          categoryId: this.data.categoryId || undefined,
          keyword: this.data.keyword || undefined,
          sort: 'updatedAt'
        }
      })
      
      const newList = refresh ? data.list : [...this.data.shopList, ...data.list]
      this.setData({
        shopList: newList,
        page: page + 1,
        hasMore: data.list.length >= this.data.pageSize,
        loading: false
      })
    } catch (err) {
      console.error('加载商家列表失败:', err)
      this.setData({ loading: false })
    }
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh() {
    this.loadShopList(true).then(() => {
      wx.stopPullDownRefresh()
    })
  },

  /**
   * 上拉加载更多
   */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadShopList()
    }
  },

  /**
   * 搜索
   */
  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  /**
   * 执行搜索
   */
  handleSearch() {
    this.loadShopList(true)
  },

  /**
   * 跳转到商家详情
   */
  goToDetail(e) {
    const shopId = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/waimai-detail/waimai-detail?id=${shopId}`
    })
  }
})
