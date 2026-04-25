# 书店小程序

## 项目结构
```
bookstore-miniapp/
├── uni/                    # 前端（uni-app Vue3 + TypeScript）
│   └── dist/build/h5/      # H5 构建产物
├── server/                 # Node.js 后端（历史版本）
├── java-server/            # Java Spring Boot 后端（主力）
├── scripts/                # 辅助脚本
├── proxy.js                # 本地代理（静态资源 + API 转发）
└── ngrok.yml               # ngrok 配置
```

## 技术栈
- 前端：uni-app (Vue3 + TypeScript) 编译到 H5
- 后端：Spring Boot 3.x + Java 17 + PostgreSQL
- 数据库：PostgreSQL 14+
- 部署：H5 静态托管 + ngrok 内网穿透

## 快速启动

### 前端
```bash
cd uni
npm run build:h5
```

### Java 后端（端口 3001）
```bash
cd java-server
mvn spring-boot:run
```

### 本地代理（端口 8080）
```bash
node proxy.js
```

## 核心功能
- 连续流阅读器（无缝翻页、预加载、滚动追踪）
- 广告解锁章节（激励视频广告模拟）
- 书架管理（加入/移除）
- 阅读进度保存与恢复
- 章节已读状态
- 听书功能（TTS 接口预留）
- 阅读器配置持久化（字号/主题/行间距/亮度）
