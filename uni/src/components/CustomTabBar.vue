<template>
  <view class="custom-tabbar" v-if="showTabBar">
    <view 
      class="tab-item" 
      v-for="tab in tabs" 
      :key="tab.pagePath"
      :class="{ active: currentPath === tab.pagePath }"
      @click="switchTab(tab.pagePath)"
    >
      <!-- 发现/compass -->
      <svg v-if="tab.icon === 'compass'" class="tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"/>
        <polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88" fill="currentColor" stroke="currentColor"/>
      </svg>
      <!-- 书城/store -->
      <svg v-if="tab.icon === 'store'" class="tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/>
        <polyline points="9 22 9 12 15 12 15 22"/>
      </svg>
      <!-- 书架/book -->
      <svg v-if="tab.icon === 'book'" class="tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <path d="M4 19.5A2.5 2.5 0 016.5 17H20"/>
        <path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/>
      </svg>
      <!-- 我的/user -->
      <svg v-if="tab.icon === 'user'" class="tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
        <circle cx="12" cy="7" r="4"/>
      </svg>
      <text class="tab-text">{{ tab.text }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';

const currentPath = ref('pages/index/index');
const showTabBar = ref(true);

const tabs = [
  { pagePath: 'pages/index/index', text: '发现', icon: 'compass' },
  { pagePath: 'pages/store/store', text: '书城', icon: 'store' },
  { pagePath: 'pages/shelf/shelf', text: '书架', icon: 'book' },
  { pagePath: 'pages/me/me', text: '我的', icon: 'user' },
];

function updateCurrentPath() {
  const pages = getCurrentPages();
  if (pages.length > 0) {
    const route = pages[pages.length - 1].route;
    currentPath.value = route || '';
    const tabPaths = tabs.map(t => t.pagePath);
    showTabBar.value = tabPaths.includes(route || '');
  }
}

onMounted(() => {
  updateCurrentPath();
});

onShow(() => {
  updateCurrentPath();
});

function switchTab(path: string) {
  uni.switchTab({ url: '/' + path });
}
</script>

<style scoped>
.custom-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 12rpx 0 calc(12rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  border-top: 1rpx solid rgba(163, 74, 46, 0.08);
  z-index: 999;
}
.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 24rpx;
  color: #888888;
  transition: all 0.2s ease;
}
.tab-item.active {
  color: #A34A2E;
}
.tab-icon {
  width: 44rpx;
  height: 44rpx;
}
.tab-text {
  font-size: 20rpx;
  font-family: 'Noto Sans SC', sans-serif;
}
</style>