<template>
  <view class="page" :style="{ backgroundColor: COLORS.bg }">
    <scroll-view scroll-y class="content">
      <!-- 顶部 -->
      <view class="header">
        <text class="header-label">发现</text>
        <text class="header-num">001</text>
      </view>

      <!-- 大字标题 -->
      <view class="hero">
        <text class="hero-title">在书页间，发现的方法。</text>
        <text class="hero-desc">精选好书，按章节付费解锁。让阅读回归本质，每一章都物有所值。</text>
      </view>

      <!-- 每日推荐 -->
      <view class="section">
        <text class="section-label">每日推荐</text>
        <view class="featured-card" @click="goDetail(featuredBook.id)">
          <view class="featured-cover" :style="{ backgroundColor: featuredColor }">
            <image v-if="featuredBook.cover" class="featured-cover-img" :src="featuredBook.cover" mode="aspectFill" />
            <text v-else class="featured-cover-text">{{ featuredBook.title.charAt(0) }}</text>
          </view>
          <view class="featured-info">
            <text class="featured-title">{{ featuredBook.title }}</text>
            <view class="featured-meta">
              <text class="featured-author">{{ featuredBook.author }}</text>
              <text class="meta-dot">·</text>
              <text class="featured-cat">{{ featuredBook.category }}</text>
            </view>
            <text class="featured-summary">{{ featuredBook.summary }}</text>
            <view class="featured-btn">
              <text class="featured-btn-text">立即阅读</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 热门榜单 -->
      <view class="section">
        <text class="section-label">热门榜单</text>
        <view class="grid">
          <view
            v-for="(book, i) in picks"
            :key="book.id"
            class="grid-item"
            @click="goDetail(book.id)"
          >
            <view class="cover-wrap">
              <view class="cover" :style="{ backgroundColor: coverColor(book.coverIdx) }">
                <image v-if="book.cover" class="cover-img" :src="book.cover" mode="aspectFill" />
                <text v-else class="cover-text">{{ book.title.charAt(0) }}</text>
              </view>
              <view class="rank-badge" :class="{ top3: i < 3 }">
                <text>{{ i + 1 }}</text>
              </view>
            </view>
            <text class="book-title">{{ book.title }}</text>
            <text class="book-author">{{ book.author }}</text>
            <view class="book-meta">
              <text class="score">{{ book.score }}</text>
              <text class="sep">·</text>
              <text class="cat">{{ book.category }}</text>
            </view>
          </view>
        </view>
      </view>

      <Folio :num="1" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { COLORS, COVERS } from '@/utils/constants';
import { fetchBooks, type Book } from '@/api/book';
import Folio from '@/components/Folio.vue';

const apiBooks = ref<Book[]>([]);

const featuredBook = computed(() => {
  const b = apiBooks.value[0];
  if (!b) return { id: 0, title: '', author: '', category: '', summary: '', cover: '' };
  return { ...b, coverIdx: 0, score: b.rating };
});
const featuredColor = computed(() => COVERS[0]);
const picks = computed(() => apiBooks.value.slice(0, 6).map((b, i) => ({ ...b, coverIdx: i, score: b.rating })));

const coverColor = (idx: number) => COVERS[idx % COVERS.length];

onMounted(async () => {
  document.title = '发现';
  try {
    apiBooks.value = await fetchBooks();
  } catch (e) {
    console.error('fetch books failed', e);
  }
});

const goDetail = (id: number) => {
  if (!id) return;
  uni.navigateTo({ url: '/pages/detail/detail?id=' + id });
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding-bottom: 100rpx;
  box-sizing: border-box;
  overflow-x: hidden;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding: 48rpx 32rpx 0;
}
.header-label {
  font-size: 40rpx;
  font-weight: 700;
  color: #2C2C2C;
  letter-spacing: 4rpx;
}
.header-num {
  font-size: 24rpx;
  color: #E8A23E;
  font-weight: 600;
}
.content {
  flex: 1;
  overflow: hidden;
  padding: 0 32rpx;
  box-sizing: border-box;
}
/* Hero */
.hero {
  padding: 32rpx 0 40rpx;
  border-bottom: 1rpx solid #E8E2D8;
}
.hero-title {
  font-size: 44rpx;
  font-weight: 700;
  color: #2C2C2C;
  line-height: 1.3;
  display: block;
  margin-bottom: 16rpx;
}
.hero-desc {
  font-size: 26rpx;
  color: #888;
  line-height: 1.7;
  display: block;
}
/* Section */
.section {
  margin-top: 40rpx;
}
.section-label {
  font-size: 20rpx;
  color: #E8A23E;
  letter-spacing: 2rpx;
  text-transform: uppercase;
  display: block;
  margin-bottom: 20rpx;
  font-weight: 700;
}
/* Featured Card */
.featured-card {
  display: flex;
  gap: 24rpx;
  padding: 24rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  border: 1rpx solid #E8E2D8;
}
.featured-cover {
  width: 180rpx;
  height: 240rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.4);
  flex-shrink: 0;
  overflow: hidden;
}
.featured-cover-img {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
}
.featured-cover-text {
  color: #fff;
  font-size: 64rpx;
  font-weight: 700;
  opacity: 0.9;
}
.featured-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
}
.featured-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #2C2C2C;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.featured-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 12rpx;
}
.featured-author {
  font-size: 24rpx;
  color: #888;
}
.meta-dot {
  font-size: 20rpx;
  color: #AAA;
}
.featured-cat {
  font-size: 24rpx;
  color: #E8A23E;
}
.featured-summary {
  font-size: 24rpx;
  color: #777;
  line-height: 1.5;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.featured-btn {
  align-self: flex-start;
  padding: 12rpx 32rpx;
  background: #E8A23E;
  border-radius: 32rpx;
}
.featured-btn text {
  color: #fff;
  font-size: 26rpx;
  font-weight: 600;
}
.featured-btn-text {
  color: #fff;
  font-size: 26rpx;
  font-weight: 600;
}
/* Grid */
.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}
.grid-item {
  width: 100%;
  margin-bottom: 0;
  min-width: 0;
  overflow: hidden;
}
.cover-wrap {
  position: relative;
  margin-bottom: 12rpx;
}
.cover {
  width: 100%;
  aspect-ratio: 3/4;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.4);
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
.rank-badge {
  position: absolute;
  top: 12rpx;
  left: 12rpx;
  min-width: 36rpx;
  height: 36rpx;
  border-radius: 999rpx;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}
.rank-badge.top3 {
  border-radius: 8rpx;
  background: #E8A23E;
}
.rank-badge text {
  font-size: 20rpx;
  font-weight: 800;
  color: #fff;
  font-family: 'JetBrains Mono', monospace;
}
.book-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #2C2C2C;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.book-author {
  font-size: 22rpx;
  color: #888;
  margin-top: 4rpx;
}
.book-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 4rpx;
}
.score {
  font-size: 22rpx;
  color: #E8A23E;
  font-weight: 600;
}
.sep {
  font-size: 20rpx;
  color: #AAA;
}
.cat {
  font-size: 22rpx;
  color: #AAA;
}
</style>