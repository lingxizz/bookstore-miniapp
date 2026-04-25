<template>
  <view class="app-wrap">
    <!-- H5 自定义 TabBar -->
    <view class="h5-tabbar" v-if="isH5 && showTabBar">
      <view
        v-for="item in tabList"
        :key="item.pagePath"
        class="tab-item"
        :class="{ active: selected === item.pagePath }"
        @click="switchTab(item)"
      >
        <text class="tab-icon">{{ item.icon }}</text>
        <text class="tab-text">{{ item.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { onLaunch, onShow, onHide } from "@dcloudio/uni-app";

onLaunch(() => {
  console.log("App Launch");
});
onShow(() => {
  console.log("App Show");
});
onHide(() => {
  console.log("App Hide");
});

// H5 TabBar
const isH5 = computed(() => {
  // @ts-ignore
  return typeof window !== 'undefined' && uni.getSystemInfoSync().platform === 'web';
});

const tabList = [
  { pagePath: 'pages/index/index', text: '\u53d1\u73b0', icon: '\u2726' },
  { pagePath: 'pages/store/store', text: '\u4e66\u57ce', icon: '\u25c8' },
  { pagePath: 'pages/shelf/shelf', text: '\u4e66\u67b6', icon: '\u25a4' },
  { pagePath: 'pages/me/me', text: '\u6211\u7684', icon: '\u25c9' },
];

const tabPages = tabList.map(i => i.pagePath);
const selected = ref('');

const updateRoute = () => {
  const pages = getCurrentPages();
  const route = pages[pages.length - 1]?.route || '';
  selected.value = route;
};

let timer: any;

onMounted(() => {
  if (isH5.value) {
    updateRoute();
    timer = setInterval(updateRoute, 200);
  }
});

onUnmounted(() => {
  clearInterval(timer);
});

const showTabBar = computed(() => tabPages.includes(selected.value));

const switchTab = (item: typeof tabList[0]) => {
  uni.switchTab({ url: '/' + item.pagePath });
};
</script>

<style>
/* 全局基础样式 */
page {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Segoe UI', Roboto, sans-serif;
  background-color: #F5F0EA;
  color: #2C2C2C;
  box-sizing: border-box;
  -webkit-font-smoothing: antialiased;
}
view, text, input, scroll-view {
  box-sizing: border-box;
}
/* 文本溢出工具类 */
.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.text-ellipsis-2 {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* H5 TabBar */
.h5-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 120rpx;
  padding-bottom: env(safe-area-inset-bottom);
  background: #FFFFFF;
  border-top: 1rpx solid #E8E2D8;
  z-index: 999;
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
}
.h5-tabbar .tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 4rpx;
  transition: all 0.2s ease;
}
.h5-tabbar .tab-icon {
  font-size: 36rpx;
  color: #AAA;
  line-height: 1;
  transition: color 0.2s ease;
}
.h5-tabbar .tab-text {
  font-size: 22rpx;
  color: #AAA;
  font-family: 'Inter', -apple-system, sans-serif;
  font-weight: 500;
  transition: color 0.2s ease;
}
.h5-tabbar .tab-item.active .tab-icon {
  color: #E8A23E;
}
.h5-tabbar .tab-item.active .tab-text {
  color: #E8A23E;
  font-weight: 600;
}
</style>