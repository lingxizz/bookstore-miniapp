<template>
  <view class="book-row" @click="navigate">
    <view class="cover" :style="{ backgroundColor: coverColor }">
      <image v-if="props.cover" class="cover-img" :src="props.cover" mode="aspectFill" />
      <text v-else class="cover-text">{{ title.charAt(0) }}</text>
    </view>
    <view class="info">
      <text class="title">{{ title }}</text>
      <text class="author">{{ author }} · {{ category }}</text>
      <text class="summary">{{ summary }}</text>
      <view class="meta">
        <text class="score">{{ displayScore }}分</text>
        <text class="sep">·</text>
        <text class="price">{{ displayPrice }}金币/章</text>
        <text class="sep">·</text>
        <text class="chapters">{{ displayCount }}字</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { COVERS } from '@/utils/constants';

const props = withDefaults(defineProps<{
  id: number;
  title: string;
  author: string;
  category: string;
  summary: string;
  rating?: number;
  score?: number;
  price?: number;
  chapterPrice?: number;
  wordCount?: number;
  chapterCount?: number;
  coverIdx?: number;
  cover?: string;
}>(), {
  coverIdx: 0
});

const displayScore = computed(() => props.rating ?? props.score ?? 0);
const displayPrice = computed(() => props.price ?? props.chapterPrice ?? 0);
const displayCount = computed(() => props.wordCount ?? props.chapterCount ?? 0);

const coverColor = computed(() => COVERS[props.coverIdx % COVERS.length]);

const navigate = () => {
  uni.navigateTo({ url: '/pages/detail/detail?id=' + props.id });
};
</script>

<style scoped>
.book-row {
  display: flex;
  flex-direction: row;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #E8E2D8;
}
.cover {
  width: 160rpx;
  height: 220rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.3);
  flex-shrink: 0;
  margin-right: 24rpx;
  overflow: hidden;
}
.cover-text {
  color: #fff;
  font-size: 52rpx;
  font-weight: 700;
  opacity: 0.9;
}
.cover-img {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
}
.info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
  overflow: hidden;
}
.title {
  font-size: 30rpx;
  font-weight: 600;
  color: #2C2C2C;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.author {
  font-size: 24rpx;
  color: #888888;
  margin-bottom: 10rpx;
}
.summary {
  font-size: 24rpx;
  color: #AAAAAA;
  line-height: 1.5;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-wrap: wrap;
}
.score {
  font-size: 24rpx;
  color: #E8A23E;
  font-weight: 600;
}
.price {
  font-size: 24rpx;
  color: #E8A23E;
}
.chapters {
  font-size: 24rpx;
  color: #AAAAAA;
}
.sep {
  font-size: 20rpx;
  color: #AAAAAA;
}
</style>