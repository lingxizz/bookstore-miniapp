<template>
  <view class="chapter-item" :class="{ premium: isPremium }" @click="$emit('click')">
    <text class="chapter-num">{{ String(num).padStart(2, '0') }}</text>
    <text class="chapter-title">{{ title }}</text>
    <text class="chapter-lock" v-if="isPremium">🔒</text>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  num: number;
  title: string;
  isPremium?: boolean;
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
.chapter-lock {
  font-size: 24rpx;
  flex-shrink: 0;
}
</style>
