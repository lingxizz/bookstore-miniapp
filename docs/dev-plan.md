# 付费解锁小说小程序 · 开发计划

## 技术栈

| 层 | 技术 |
|------|------|
| 前端 | uniapp (Vue3 + TypeScript + uni-ui) + Pinia |
| 后端 | Node.js + Fastify + PostgreSQL |
| ORM | Prisma |
| 支付 | 微信小程序支付 |
| 部署 | 微信小程序 + H5 |

## 目录结构

```
bookstore-miniapp/
├── uni/                          # uniapp 前端
│   ├── src/
│   │   ├── components/           # 公共组件
│   │   ├── pages/                # 页面
│   │   ├── static/               # 静态资源
│   │   ├── store/                # Pinia 状态
│   │   ├── utils/                # 工具函数
│   │   ├── App.vue
│   │   └── main.ts
│   ├── manifest.json
│   ├── pages.json
│   └── package.json
├── server/                       # 后端
│   ├── src/
│   ├── prisma/
│   └── package.json
└── docs/
    └── dev-plan.md               # 本文件
```

## 页面清单

| 页面 | 路径 | 说明 |
|------|------|------|
| 首页 | pages/index/index | 发现，双列封面网格 |
| 书城 | pages/store/store | 搜索+分类+榜单+行铺列表 |
| 书架 | pages/shelf/shelf | 阅读记录 |
| 我的 | pages/me/me | 账户+终端组件 |
| 书籍详情 | pages/detail/detail | 封面+信息+章节目录 |
| 阅读器 | pages/reader/reader | 章节内容+深色模式 |
| 充值 | pages/recharge/recharge | 金币套餐 |

## 数据模型

```typescript
interface Book {
  id: number;
  title: string;
  author: string;
  category: string;
  summary: string;
  cover: string;
  freeChapters: number;
  chapterPrice: number;
  chapterCount: number;
  score: number;
}

interface Chapter {
  id: number;
  bookId: number;
  title: string;
  content: string;
  isPremium: boolean;
}

interface User {
  id: string;
  openid: string;
  nickname: string;
  avatar: string;
  coins: number;
  vipStatus: 'normal' | 'vip';
  readTime: number;      // 分钟
  createdAt: string;
}

interface ReadingRecord {
  id: number;
  userId: string;
  bookId: number;
  chapterId: number;
  progress: number;      // 0-100
  updatedAt: string;
}
```

## API 设计

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 书籍列表 | GET | /api/books | 支持 cat/rank/search/sort |
| 书籍详情 | GET | /api/books/:id | |
| 章节目录 | GET | /api/books/:id/chapters | |
| 章节内容 | GET | /api/chapters/:id | 带权限校验 |
| 微信登录 | POST | /api/auth/login | code2Session |
| 用户信息 | GET | /api/users/me | |
| 充值订单 | POST | /api/orders | 创建微信支付订单 |
| 解锁章节 | POST | /api/chapters/:id/unlock | 扣除金币 |
| 书架同步 | POST | /api/reading-records | 更新进度 |
| 书架列表 | GET | /api/reading-records | 当前用户 |

## 开发阶段

### Phase 1：项目搭建 + 静态页面
- [ ] 初始化 uniapp 项目（Vue3 + TS + Vite）
- [ ] 配置 pages.json、manifest.json、TabBar
- [ ] 搭建基础组件（BookCard、SearchBar、ChapterItem、RankBadge、Folio、Terminal）
- [ ] 实现所有静态页面（mock 数据，完整交互）
- [ ] 阅读器翻章、深色模式、字体设置

### Phase 2：后端 + 用户系统
- [ ] 搭建 Node.js + Fastify 服务
- [ ] PostgreSQL + Prisma ORM 建模
- [ ] 微信登录接入（code2Session + JWT）
- [ ] 书籍/章节/用户 API 实现
- [ ] 书架阅读进度持久化

### Phase 3：支付 + 测试
- [ ] 微信支付接入（统一订单、回调、金币到账）
- [ ] 金币消费逻辑（解锁章节、VIP 校验）
- [ ] 后端管理后台（书籍上传、章节管理、订单查看）
- [ ] 真机测试 + 微信小程序发布
