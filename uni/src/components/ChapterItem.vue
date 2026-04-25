<template>
  <view class="chapter-item" @click="$emit('click')">
    <view class="left">
      <text class="num">{{ num }}</text>
      <text class="title">{{ title }}</text>
    </view>
    <view class="right">
      <text v-if="isPremium" class="lock">锁</text>
      <text v-else class="free">免费</text>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{ num: number; title: string; isPremium: boolean }>();
defineEmits<{ (e: 'click'): void }>();
</script>

<style scoped>
.chapter-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #E8E2D8;
}
.left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  overflow: hidden;
}
.num {
  font-family: 'JetBrains Mono', monospace;
  font-size: 22rpx;
  color: #AAAAAA;
  min-width: 48rpx;
}
.title {
  font-size: 28rpx;
  color: #2C2C2C;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.lock {
  font-size: 24rpx;
  color: #E8A23E;
  font-weight: 600;
}
.free {
  font-size: 24rpx;
  color: #4CAF50;
}
</style>