<template>
  <view class="terminal">
    <view class="line" v-for="(line, i) in lines" :key="i">
      <text class="prompt">$</text>
      <text class="cmd">{{ line.cmd }}</text>
    </view>
    <view class="line cursor-line">
      <text class="prompt">$</text>
      <text class="cmd">{{ currentCmd }}</text>
      <text class="cursor">_</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

const lines = ref([
  { cmd: 'whoami' },
  { cmd: 'cat /etc/motto' },
]);
const currentCmd = ref('');
const targetCmd = 'read --book "长安十二时辰"';

onMounted(() => {
  let i = 0;
  const interval = setInterval(() => {
    if (i <= targetCmd.length) {
      currentCmd.value = targetCmd.slice(0, i);
      i++;
    } else {
      clearInterval(interval);
    }
  }, 80);
});
</script>

<style scoped>
.terminal {
  background: #1A1A1A;
  border-radius: 16rpx;
  padding: 32rpx;
  margin: 24rpx 0;
}
.line {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}
.prompt {
  font-family: 'SF Mono', monospace;
  font-size: 24rpx;
  color: #2D6A4F;
  font-weight: 600;
}
.cmd {
  font-family: 'SF Mono', monospace;
  font-size: 24rpx;
  color: #D4CFC9;
}
.cursor-line {
  margin-bottom: 0;
}
.cursor {
  font-family: 'SF Mono', monospace;
  font-size: 24rpx;
  color: #B8451A;
  animation: blink 1s step-end infinite;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
</style>
