<template>
  <view class="tab-bar" v-if="visible">
    <view
      v-for="item in list"
      :key="item.pagePath"
      class="tab-item"
      :class="{ active: selected === item.pagePath }"
      @click="switchTab(item)"
    >
      <text class="tab-icon">{{ item.icon }}</text>
      <text class="tab-text">{{ item.text }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

const selected = ref('');
const visible = ref(false);
const list = [
  { pagePath: 'pages/index/index', text: '发现', icon: '✦' },
  { pagePath: 'pages/store/store', text: '书城', icon: '◈' },
  { pagePath: 'pages/shelf/shelf', text: '书架', icon: '▤' },
  { pagePath: 'pages/me/me', text: '我的', icon: '◉' },
];

const tabPages = list.map(i => i.pagePath);

const update = () => {
  const pages = getCurrentPages();
  const route = pages[pages.length - 1]?.route || '';
  selected.value = route;
  visible.value = tabPages.includes(route);
};

let timer: any;

onMounted(() => {
  update();
  timer = setInterval(update, 200);
});

onUnmounted(() => {
  clearInterval(timer);
});

const switchTab = (item: typeof list[0]) => {
  uni.switchTab({ url: '/' + item.pagePath });
};
</script>

<style scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 120rpx;
  padding-bottom: env(safe-area-inset-bottom);
  background: #FAF8F5;
  border-top: 1rpx solid #E8E4DF;
  z-index: 999;
}
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 4rpx;
}
.tab-icon {
  font-size: 36rpx;
  color: #A39E99;
  line-height: 1;
}
.tab-text {
  font-size: 22rpx;
  color: #A39E99;
  font-family: 'Inter', -apple-system, sans-serif;
  font-weight: 500;
}
.tab-item.active .tab-icon {
  color: #B8451A;
}
.tab-item.active .tab-text {
  color: #B8451A;
  font-weight: 600;
}
</style>
