<template>
  <view class="page" :style="{ backgroundColor: COLORS.bg }">
    <view class="header">
      <text class="header-label">书城</text>
    </view>

    <!-- 搜索栏 -->
    <view class="search-bar">
      <text class="search-icon">⌕</text>
      <input class="search-input" placeholder="搜索书名、作者" placeholder-class="search-placeholder" v-model="search" />
    </view>

    <!-- 分类按钮 -->
    <view class="cat-btns">
      <view
        v-for="cat in categories"
        :key="cat"
        class="cat-btn"
        :class="{ active: activeCat === cat }"
        @click="activeCat = cat"
      >
        <text>{{ cat }}</text>
      </view>
    </view>

    <!-- 一级分类tab -->
    <view class="primary-tabs">
      <view
        v-for="tab in primaryTabs"
        :key="tab"
        class="tab"
        :class="{ active: activePrimary === tab }"
        @click="activePrimary = tab"
      >
        <text>{{ tab }}</text>
      </view>
    </view>

    <!-- 二级榜单tab -->
    <view class="rank-tabs">
      <view
        v-for="tab in rankTabs"
        :key="tab"
        class="tab"
        :class="{ active: activeRank === tab }"
        @click="activeRank = tab"
      >
        <text>{{ tab }}</text>
      </view>
    </view>

    <scroll-view scroll-y class="content">
      <view class="list">
        <BookRow
          v-for="(book, idx) in filteredBooks"
          :key="book.id"
          v-bind="book"
        />
      </view>

      <!-- 底部featured推广 -->
      <view class="featured">
        <view class="featured-icon">
          <text class="featured-icon-text">精选</text>
        </view>
        <view class="featured-info">
          <text class="featured-title">每周精选推荐</text>
          <text class="featured-desc">编辑团队精心挑选的高分作品</text>
        </view>
      </view>

      <Folio :num="2" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { COLORS } from '@/utils/constants';
import { fetchBooks, type Book } from '@/api/book';
import BookRow from '@/components/BookRow.vue';
import Folio from '@/components/Folio.vue';

const search = ref('');
const categories = ['全部', '历史', '科幻', '文学', '悬疑', '社科'];
const primaryTabs = ['全部', '热门', '推荐', '新书'];
const rankTabs = ['推荐榜', '完本榜', '巅峰榜', '新书榜'];

const activeCat = ref('全部');
const activePrimary = ref('全部');
const activeRank = ref('推荐榜');
const apiBooks = ref<Book[]>([]);

const filteredBooks = computed(() => {
  let list = apiBooks.value.map((b, i) => ({
    ...b,
    coverIdx: i,
    score: b.rating,
    chapterPrice: b.price,
    chapterCount: b.wordCount,
  }));
  if (activeCat.value !== '全部') {
    list = list.filter(b => b.category === activeCat.value);
  }
  if (search.value.trim()) {
    const kw = search.value.trim().toLowerCase();
    list = list.filter(b => b.title.includes(kw) || b.author.includes(kw));
  }
  if (activeRank.value === '推荐榜') list = [...list].sort((a,b) => b.score - a.score);
  else if (activeRank.value === '新书榜') list = [...list].reverse();
  else if (activeRank.value === '巅峰榜') list = [...list].sort((a,b) => b.chapterCount - a.chapterCount);
  return list;
});

onMounted(async () => {
  try {
    apiBooks.value = await fetchBooks();
  } catch (e) {
    console.error('fetch books failed', e);
  }
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding-bottom: 100rpx;
}
.header {
  padding: 48rpx 32rpx 0;
}
.header-label {
  font-size: 40rpx;
  font-weight: 700;
  color: #2C2C2C;
  letter-spacing: 4rpx;
}
.search-bar {
  display: flex;
  align-items: center;
  margin: 24rpx 32rpx;
  padding: 16rpx 24rpx;
  background: #FFFFFF;
  border-radius: 40rpx;
  border: 1rpx solid #E8E2D8;
}
.search-icon {
  font-size: 28rpx;
  color: #AAA;
  margin-right: 12rpx;
}
.search-input {
  flex: 1;
  font-size: 26rpx;
  color: #2C2C2C;
}
.search-placeholder {
  color: #AAA;
}
.cat-btns {
  display: flex;
  gap: 16rpx;
  padding: 0 32rpx 16rpx;
  overflow-x: auto;
}
.cat-btn {
  padding: 10rpx 28rpx;
  border-radius: 32rpx;
  background: #FFFFFF;
  border: 1rpx solid #E8E2D8;
  font-size: 24rpx;
  color: #888;
  white-space: nowrap;
  flex-shrink: 0;
}
.cat-btn.active {
  background: #E8A23E;
  border-color: #E8A23E;
  color: #fff;
}
.primary-tabs {
  display: flex;
  padding: 16rpx 32rpx;
  gap: 32rpx;
  border-bottom: 1rpx solid #E8E2D8;
}
.primary-tabs .tab {
  font-size: 28rpx;
  color: #AAA;
  padding-bottom: 8rpx;
  border-bottom: 2rpx solid transparent;
}
.primary-tabs .tab.active {
  color: #2C2C2C;
  border-bottom-color: #E8A23E;
}
.rank-tabs {
  display: flex;
  padding: 16rpx 32rpx;
  gap: 24rpx;
}
.rank-tabs .tab {
  font-size: 26rpx;
  color: #AAA;
}
.rank-tabs .tab.active {
  color: #E8A23E;
  font-weight: 600;
}
.content {
  flex: 1;
  overflow: hidden;
  padding: 0 32rpx;
}
.list {
  padding-bottom: 24rpx;
}
.featured {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  border: 1rpx solid #E8E2D8;
  margin: 24rpx 0;
}
.featured-icon {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  background: #E8A23E;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}
.featured-icon-text {
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
}
.featured-info {
  flex: 1;
}
.featured-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #2C2C2C;
  margin-bottom: 6rpx;
}
.featured-desc {
  font-size: 24rpx;
  color: #888;
}
</style>