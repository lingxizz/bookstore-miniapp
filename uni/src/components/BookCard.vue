<template>
  <view class="book-card" @click="navigate">
    <view class="cover" :style="{ backgroundColor: coverColor }">
      <image v-if="props.cover" class="cover-img" :src="props.cover" mode="aspectFill" />
      <text v-else class="cover-text">{{ title.charAt(0) }}</text>
    </view>
    <text class="title">{{ title }}</text>
    <text class="author">{{ author }}</text>
    <view class="meta">
      <text class="score">{{ score }}</text>
      <text class="sep">·</text>
      <text class="cat">{{ category }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { COVERS } from '@/utils/constants';

const props = defineProps<{
  id: number;
  title: string;
  author: string;
  category: string;
  score: number;
  coverIdx: number;
  cover?: string;
}>();

const coverColor = computed(() => COVERS[props.coverIdx % COVERS.length]);

const navigate = () => {
  uni.navigateTo({ url: '/pages/detail/detail?id=' + props.id });
};
</script>

<style scoped>
.book-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8rpx 0;
}
.cover {
  width: 240rpx;
  height: 320rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.4);
  margin-bottom: 16rpx;
  overflow: hidden;
}
.cover-img {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
}
.cover-text {
  color: #fff;
  font-size: 72rpx;
  font-weight: 700;
  opacity: 0.9;
}
.title {
  font-size: 28rpx;
  font-weight: 600;
  color: #2C2C2C;
  margin-bottom: 6rpx;
  max-width: 260rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.author {
  font-size: 22rpx;
  color: #888888;
  margin-bottom: 6rpx;
}
.meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.score {
  font-size: 22rpx;
  color: #E8A23E;
  font-weight: 600;
}
.sep {
  font-size: 20rpx;
  color: #AAAAAA;
}
.cat {
  font-size: 22rpx;
  color: #AAAAAA;
}
</style>