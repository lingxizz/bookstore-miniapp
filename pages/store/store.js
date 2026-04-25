// pages/store/store.js
Page({
  data: {
    // 当前选中的分类 Tab
    currentTab: 0,

    // Tab 列表
    tabs: ['热门', '推荐', '新书'],

    // 书籍列表
    books: [
      {
        id: 1,
        title: '斗破苍穹',
        author: '天蚕土豆',
        coverColor: '#e03131',
        summary: '这里是属于斗气的世界，没有花俏艳丽的魔法，有的，仅仅是繁衍到巅峰的斗气！',
        score: 9.2,
        chapterPrice: 10,
        freeChapters: 50,
        totalChapters: 1623
      },
      {
        id: 2,
        title: '全职高手',
        author: '蝴蝶蓝',
        coverColor: '#2f9e44',
        summary: '网游荣耀中被誉为教科书级别的顶尖高手，因为种种原因遭到俱乐部的驱逐。',
        score: 9.5,
        chapterPrice: 8,
        freeChapters: 100,
        totalChapters: 1728
      },
      {
        id: 3,
        title: '庆余年',
        author: '猫腻',
        coverColor: '#1971c2',
        summary: '一个年轻的病人，因为一次毫不意外的经历，重生到一个完全不同的世界。',
        score: 9.3,
        chapterPrice: 12,
        freeChapters: 30,
        totalChapters: 378
      },
      {
        id: 4,
        title: '诡秘之主',
        author: '爱潜水的乌贼',
        coverColor: '#9c36b5',
        summary: '蒸汽与机械的浪潮中，谁能触及非凡？历史和黑暗的迷雾里，又是谁在耳语？',
        score: 9.7,
        chapterPrice: 15,
        freeChapters: 80,
        totalChapters: 1394
      },
      {
        id: 5,
        title: '大奉打更人',
        author: '卖报小郎君',
        coverColor: '#e8590c',
        summary: '这个世界，有儒；有道；有佛；有妖；有术士。',
        score: 9.1,
        chapterPrice: 10,
        freeChapters: 60,
        totalChapters: 1200
      },
      {
        id: 6,
        title: '凡人修仙传',
        author: '忘语',
        coverColor: '#5c940d',
        summary: '一个普通山村小子，偶然下进入到当地江湖小门派，成了一名记名弟子。',
        score: 9.0,
        chapterPrice: 8,
        freeChapters: 200,
        totalChapters: 2446
      },
      {
        id: 7,
        title: '雪中悍刀行',
        author: '烽火戏诸侯',
        coverColor: '#c92a2a',
        summary: '江湖是一张珠帘。大人物小人物，是珠子，大故事小故事，是串线。',
        score: 9.4,
        chapterPrice: 12,
        freeChapters: 40,
        totalChapters: 461
      },
      {
        id: 8,
        title: '三体',
        author: '刘慈欣',
        coverColor: '#0b7285',
        summary: '文化大革命如火如荼进行的同时，军方探寻外星文明的绝秘计划"红岸工程"取得了突破性进展。',
        score: 9.8,
        chapterPrice: 15,
        freeChapters: 20,
        totalChapters: 90
      }
    ]
  },

  onLoad() {
    // 页面加载
  },

  // 切换 Tab
  onTabChange(e) {
    const index = e.currentTarget.dataset.index
    this.setData({ currentTab: index })
    // 模拟切换数据
    wx.showToast({ title: this.data.tabs[index], icon: 'none' })
  },

  // 点击书籍
  onBookTap(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/book/detail?id=${id}` })
  }
})
