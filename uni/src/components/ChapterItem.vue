<template>
  <view class="chapter-item" :class="{ premium: isPremium }" @click="$emit('click')">
    <text class="chapter-num">{{ String(num).padStart(2, '0') }}</text>
    <text class="chapter-title">{{ title }}</text>
    <view class="chapter-status">
      <!-- 正在读 -->
      <svg v-if="status === 'reading'" class="status-icon" viewBox="0 0 24 24" fill="none" stroke="#4CAF50" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M2 20h.01"/>
        <path d="M7 20v-4"/>
        <path d="M12 20v-8"/>
        <path d="M17 20V8"/>
      </svg>
      <!-- 已读 -->
      <svg v-else-if="status === 'read'" class="status-icon" viewBox="0 0 24 24" fill="none" stroke="#A34A2E" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="20 6 9 17 4 12"/>
      </svg>
      <!-- 锁定 -->
      <svg v-else-if="status === 'locked'" class="status-icon" viewBox="0 0 24 24" fill="none" stroke="#AAAAAA" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
        <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
      </svg>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  num: number;
  title: string;
  isPremium?: boolean;
  status?: 'read' | 'reading' | 'locked' | 'default';
}>();
defineEmits(['click']);
</script>

<style scoped>
.chapter-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F5F0EA;
}
.chapter-item:last-child {
  border-bottom: none;
}
.chapter-num {
  font-size: 24rpx;
  color: #AAAAAA;
  font-family: 'Noto Serif SC', serif;
  width: 48rpx;
  flex-shrink: 0;
}
.chapter-title {
  flex: 1;
  font-size: 28rpx;
  color: #2C2C2C;
  font-family: 'Noto Sans SC', sans-serif;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.chapter-item.premium .chapter-title {
  color: #888888;
}
.chapter-status {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}
.status-icon {
  width: 28rpx;
  height: 28rpx;
}
</style>
