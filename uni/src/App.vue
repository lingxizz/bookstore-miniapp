<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app';

onLaunch(() => {
  console.log('App Launch');
  // H5 隐藏原生 tabbar，使用自定义
  // #ifdef H5
  uni.hideTabBar();
  // #endif
});

onShow(() => {
  console.log('App Show');
});

onHide(() => {
  console.log('App Hide');
});
</script>

<style>
/* 全局基础样式 */
page {
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #F8F4F0;
  color: #1C1C19;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 全局字体 */
@font-face {
  font-family: 'Noto Serif SC';
  src: local('Noto Serif SC'), local('Songti SC'), local('STSong');
}

@font-face {
  font-family: 'Noto Sans SC';
  src: local('Noto Sans SC'), local('PingFang SC'), local('Helvetica Neue');
}

/* 安全区域适配 */
.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* 强制隐藏原生 TabBar（H5 端） */
uni-tabbar,
.uni-tabbar,
.uni-tabbar-bottom,
.uni-tabbar-border {
  display: none !important;
  height: 0 !important;
  min-height: 0 !important;
  overflow: hidden !important;
}

/* 移除 tabbar 占位 */
uni-app.uni-app--showtabbar .uni-app--tabbar {
  display: none !important;
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  display: none;
  width: 0 !important;
  height: 0 !important;
  -webkit-appearance: none;
  background: transparent;
}

/* 通用动画 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { transform: translateY(20rpx); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.fade-in {
  animation: fadeIn 0.3s ease;
}

.slide-up {
  animation: slideUp 0.3s ease;
}

/* 点击态 */
.hover-opacity {
  transition: opacity 0.2s;
}
.hover-opacity:active {
  opacity: 0.7;
}

/* 文字省略 */
.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ellipsis-2 {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 禁用状态 */
.disabled {
  opacity: 0.4;
  pointer-events: none;
}
</style>