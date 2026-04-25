// pages/index/index.js
Page({
  data: {
    // 搜索关键词
    searchKeyword: '',

    // 轮播 Banner
    banners: [
      { id: 1, color: '#339af0', text: '新书首发' },
      { id: 2, color: '#20c997', text: '限时免费' },
      { id: 3, color: '#ff6b6b', text: '热门推荐' }
    ],

    // 分类快捷入口
    categories: [
      { id: 1, name: '玄幻', icon: '⚔️' },
      { id: 2, name: '都市', icon: '🏙️' },
      { id: 3, name: '言情', icon: '💕' },
      { id: 4, name: '科幻', icon: '🚀' }
    ],

    // 精选推荐书籍
    featuredBooks: [
      { id: 1, title: '斗破苍穹', author: '天蚕土豆', coverColor: '#e03131' },
      { id: 2, title: '全职高手', author: '蝴蝶蓝', coverColor: '#2f9e44' },
      { id: 3, title: '庆余年', author: '猫腻', coverColor: '#1971c2' },
      { id: 4, title: '诡秘之主', author: '爱潜水的乌贼', coverColor: '#9c36b5' },
      { id: 5, title: '大奉打更人', author: '卖报小郎君', coverColor: '#e8590c' }
    ],

    // 短视频播报
    videoBooks: [
      { id: 1, title: '三体：黑暗森林', author: '刘慈欣', coverColor: '#0b7285', duration: '02:35' },
      { id: 2, title: '凡人修仙传', author: '忘语', coverColor: '#5c940d', duration: '01:48' },
      { id: 3, title: '雪中悍刀行', author: '烽火戏诸侯', coverColor: '#c92a2a', duration: '03:12' }
    ]
  },

  onLoad() {
    // 页面加载
  },

  // 搜索输入
  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value })
  },

  // 搜索确认
  onSearchConfirm() {
    const kw = this.data.searchKeyword.trim()
    if (kw) {
      wx.showToast({ title: `搜索: ${kw}`, icon: 'none' })
    }
  },

  // 点击 Banner
  onBannerTap(e) {
    const id = e.currentTarget.dataset.id
    wx.showToast({ title: `Banner ${id}`, icon: 'none' })
  },

  // 点击分类
  onCategoryTap(e) {
    const name = e.currentTarget.dataset.name
    wx.showToast({ title: `分类: ${name}`, icon: 'none' })
  },

  // 点击推荐书籍
  onBookTap(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/book/detail?id=${id}` })
  },

  // 点击视频
  onVideoTap(e) {
    const id = e.currentTarget.dataset.id
    wx.showToast({ title: `播放视频 ${id}`, icon: 'none' })
  }
})
