<template>
  <view class="folio" :style="{ paddingTop: pad + 'rpx', paddingBottom: pad + 'rpx' }">
    <view class="folio-line" />
    <text class="folio-num">— {{ num }} —</text>
    <view class="folio-line" />
  </view>
</template>

<script setup lang="ts">
defineProps<{ num: number; pad?: number }>();
</script>

<style scoped>
.folio {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  margin: 32rpx 0;
}
.folio-line {
  flex: 1;
  height: 1rpx;
  background: #E8E2D8;
}
.folio-num {
  font-size: 22rpx;
  color: #AAAAAA;
  font-family: 'Noto Serif SC', serif;
  white-space: nowrap;
}
</style>
