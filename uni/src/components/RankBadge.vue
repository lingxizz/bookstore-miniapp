<template>
  <view class="badge" :class="{ top3: rank <= 3 }">
    <text class="text">{{ rank }}</text>
  </view>
</template>

<script setup lang="ts">
defineProps<{ rank: number }>();
</script>

<style scoped>
.badge {
  width: 40rpx;
  height: 40rpx;
  border-radius: 8rpx;
  background: #E8E4DF;
  display: flex;
  align-items: center;
  justify-content: center;
}
.badge.top3 {
  background: #B8451A;
}
.text {
  font-family: 'SF Mono', monospace;
  font-size: 22rpx;
  color: #6B6560;
  font-weight: 600;
}
.top3 .text {
  color: #fff;
}
</style>
