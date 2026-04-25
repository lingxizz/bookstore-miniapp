<template>
  <view class="folio">
    <text class="dash">—</text>
    <text class="num">{{ fmtNum }}</text>
    <text class="dash">—</text>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
const props = defineProps<{ num: number }>();
const fmtNum = computed(() => String(props.num).padStart(3, '0'));
</script>

<style scoped>
.folio {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 48rpx 0 120rpx;
}
.dash {
  color: #E8E2D8;
  font-size: 24rpx;
  letter-spacing: 4rpx;
}
.num {
  font-family: 'JetBrains Mono', monospace;
  font-size: 24rpx;
  color: #AAAAAA;
  letter-spacing: 4rpx;
}
</style>