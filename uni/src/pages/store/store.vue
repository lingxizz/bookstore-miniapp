<template>
  <view class="page" :style="{ backgroundColor: '#F8F4F0' }">
    <!-- Loading skeleton -->
    <view v-if="isLoading" class="skeleton-page">
      <view class="sk-header"></view>
      <view class="sk-search"></view>
      <view class="sk-filter-row">
        <view class="sk-filter-tag" v-for="i in 4" :key="i"></view>
      </view>
      <view class="sk-filter-row">
        <view class="sk-filter-tag" v-for="i in 3" :key="i"></view>
      </view>
      <view class="sk-book-row" v-for="i in 5" :key="i">
        <view class="sk-cover"></view>
        <view class="sk-book-info">
          <view class="sk-line w70"></view>
          <view class="sk-line w40"></view>
          <view class="sk-line w60"></view>
        </view>
      </view>
    </view>

    <view v-else>
      <!-- 顶部 -->
      <view class="header">
        <text class="header-label">书城</text>
      </view>

      <!-- 搜索栏 -->
      <view class="search-bar" @click="goSearch">
        <svg class="search-icon-svg" viewBox="0 0 24 24" fill="none" stroke="#AAAAAA" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"/>
          <path d="m21 21-4.3-4.3"/>
        </svg>
        <input class="search-input" placeholder="搜索书名、作者" placeholder-class="search-placeholder" v-model="search" />
      </view>

      <!-- 分类筛选 -->
      <scroll-view class="filter-scroll" scroll-x show-scrollbar="false">
        <view class="filter-row">
          <view class="filter-tag" v-for="cat in categories" :key="cat"
                :class="{ active: activeCat === cat }" @click="activeCat = cat">
            <text>{{ cat }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 排序 + 状态 -->
      <view class="sub-filter">
        <view class="sort-tabs">
          <view class="sort-tab" v-for="s in sortOptions" :key="s.value"
                :class="{ active: sort === s.value }" @click="sort = s.value">
            <text>{{ s.label }}</text>
          </view>
        </view>
        <view class="status-tabs">
          <view class="status-tab" v-for="st in statusOptions" :key="st.value"
                :class="{ active: status === st.value }" @click="status = st.value">
            <text>{{ st.label }}</text>
          </view>
        </view>
      </view>

      <!-- 切换 loading -->
      <view v-show="isSwitching && !isLoading" class="switch-loading">
        <view class="switch-spinner" />
      </view>

      <!-- 书籍列表 -->
      <view v-show="!isSwitching" class="book-list">
        <view class="book-row" v-for="book in filteredBooks" :key="book.id" @click="goDetail(book.id)">
          <view class="book-cover" :style="{ backgroundColor: book.coverColor || '#A34A2E' }">
            <image v-if="book.cover" class="book-cover-img" :src="book.cover" mode="aspectFill" />
            <text v-else class="book-cover-text">{{ book.title.charAt(0) }}</text>
          </view>
          <view class="book-info">
            <text class="book-title">{{ book.title }}</text>
            <text class="book-author">{{ book.author }}</text>
            <view class="book-meta">
              <text class="book-tag category">{{ book.category }}</text>
              <text class="book-tag wordcount">{{ formatWordCount(book.wordCount) }}字</text>
            </view>
            <view class="book-extra">
              <text class="book-status" :class="book.status">{{ book.status === 'completed' ? '已完结' : '连载中' }}</text>
              <text v-if="book.status === 'ongoing' && book.chapterCount" class="book-chapters">更新至{{ book.chapterCount }}章</text>
              <view class="book-rating" v-if="book.rating">
                <text class="rating-star">★</text>
                <text class="rating-num">{{ book.rating }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 结果数 -->
      <view class="result-count" v-if="filteredBooks.length">
        <text>共 {{ filteredBooks.length }} 本</text>
      </view>

      <Folio :num="2" />
    </view>
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { COLORS } from '@/utils/constants';
import { fetchBookFilter, type Book } from '@/api/book';
import Folio from '@/components/Folio.vue';
import CustomTabBar from '@/components/CustomTabBar.vue';

const search = ref('');
const categories = ref(['全部', '武侠仙侠', '玄幻奇幻', '都市言情', '历史军事', '科幻灵异', '游戏竞技']);
const activeCat = ref('全部');
const sort = ref('hot');
const sortOptions = [
  { label: '热度', value: 'hot' },
  { label: '评分', value: 'rating' },
  { label: '最新', value: 'new' },
];
const status = ref('');
const statusOptions = [
  { label: '全部', value: '' },
  { label: '连载', value: 'ongoing' },
  { label: '完本', value: 'completed' },
];
const books = ref<Book[]>([]);
const isLoading = ref(true);
const isSwitching = ref(false);
const firstLoad = ref(true);

const filteredBooks = computed(() => {
  let result = books.value;
  if (activeCat.value !== '全部') {
    result = result.filter(b => b.category === activeCat.value);
  }
  if (search.value) {
    const q = search.value.toLowerCase();
    result = result.filter(b => b.title.toLowerCase().includes(q) || b.author.toLowerCase().includes(q));
  }
  return result;
});

let loadTimer: ReturnType<typeof setTimeout> | null = null;
function debouncedLoadBooks() {
  if (loadTimer) clearTimeout(loadTimer);
  loadTimer = setTimeout(() => loadBooks(), 300);
}

watch([activeCat, sort, status], () => {
  isSwitching.value = true;
  debouncedLoadBooks();
});

onMounted(() => {
  isLoading.value = firstLoad.value;
  loadBooks();
});

async function loadBooks() {
  isSwitching.value = true;
  const startTime = Date.now();
  try {
    const params: any = { sort: sort.value, status: status.value };
    if (activeCat.value !== '全部') params.category = activeCat.value;
    books.value = await fetchBookFilter(params);
  } catch (e) {
    console.error('fetch books failed', e);
  } finally {
    // 保证 loading 至少显示 200ms，避免快速请求闪烁
    const elapsed = Date.now() - startTime;
    if (elapsed < 200) {
      await new Promise(r => setTimeout(r, 200 - elapsed));
    }
    isLoading.value = false;
    firstLoad.value = false;
    isSwitching.value = false;
  }
}

function goDetail(id: number) {
  uni.navigateTo({ url: '/pages/detail/detail?id=' + id });
}
function goSearch() {
  uni.navigateTo({ url: '/pages/search/search' });
}
function formatWordCount(n?: number): string {
  if (!n) return '0';
  if (n >= 10000) return (n / 10000).toFixed(1) + '万';
  return String(n);
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 0 32rpx 160rpx;
  background: #F8F4F0;
}

/* Skeleton */
.skeleton-page {
  padding: 24rpx 0;
}
.sk-header {
  height: 48rpx;
  width: 120rpx;
  background: #E8E2D8;
  border-radius: 8rpx;
  margin-bottom: 24rpx;
  animation: shimmer 1.5s infinite;
}
.sk-search {
  height: 80rpx;
  background: #FFFFFF;
  border-radius: 48rpx;
  margin-bottom: 24rpx;
  animation: shimmer 1.5s infinite;
}
.sk-filter-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 16rpx;
}
.sk-filter-tag {
  width: 140rpx;
  height: 56rpx;
  background: #E8E2D8;
  border-radius: 48rpx;
  animation: shimmer 1.5s infinite;
}
.sk-book-row {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F5F0EA;
}
.sk-cover {
  width: 140rpx;
  aspect-ratio: 2/3;
  border-radius: 12rpx;
  background: #E8E2D8;
  animation: shimmer 1.5s infinite;
}
.sk-book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.sk-line {
  height: 24rpx;
  background: #E8E2D8;
  border-radius: 8rpx;
  animation: shimmer 1.5s infinite;
}
.sk-line.w70 { width: 70%; }
.sk-line.w40 { width: 40%; }
.sk-line.w60 { width: 60%; }
@keyframes shimmer {
  0% { background: #E8E2D8; }
  50% { background: #F0EBE3; }
  100% { background: #E8E2D8; }
}

/* Header */
.header {
  padding: 48rpx 0 24rpx;
}
.header-label {
  font-size: 40rpx;
  font-weight: 700;
  color: #1C1C19;
  font-family: 'Noto Serif SC', serif;
}

/* Search */
.search-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: #FFFFFF;
  border-radius: 48rpx;
  padding: 20rpx 32rpx;
  margin-bottom: 24rpx;
  border: 1rpx solid rgba(163, 74, 46, 0.08);
}
.search-icon-svg {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
}
.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #1C1C19;
  font-family: 'Noto Sans SC', sans-serif;
}
.search-placeholder {
  color: #AAAAAA;
}

/* Filter */
.filter-scroll {
  margin-bottom: 16rpx;
}
.filter-row {
  display: flex;
  gap: 16rpx;
  flex-wrap: nowrap;
  white-space: nowrap;
}
.filter-tag {
  padding: 12rpx 28rpx;
  border-radius: 48rpx;
  background: #FFFFFF;
  border: 1rpx solid rgba(163, 74, 46, 0.1);
  flex-shrink: 0;
  white-space: nowrap;
}
.filter-tag.active {
  background: #A34A2E;
  border-color: #A34A2E;
}
.filter-tag text {
  font-size: 26rpx;
  color: #1C1C19;
  font-family: 'Noto Sans SC', sans-serif;
}
.filter-tag.active text {
  color: #FFFFFF;
}

/* Sub filter */
.sub-filter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}
.sort-tabs, .status-tabs {
  display: flex;
  gap: 8rpx;
}
.sort-tab, .status-tab {
  padding: 8rpx 20rpx;
  border-radius: 24rpx;
}
.sort-tab.active, .status-tab.active {
  background: rgba(163, 74, 46, 0.08);
}
.sort-tab text, .status-tab text {
  font-size: 24rpx;
  color: #645D55;
  font-family: 'Noto Sans SC', sans-serif;
}
.sort-tab.active text, .status-tab.active text {
  color: #A34A2E;
  font-weight: 600;
}

/* Switch loading */
.switch-loading {
  display: flex;
  justify-content: center;
  padding: 24rpx 0;
}
.switch-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 4rpx solid #E8E2D8;
  border-top-color: #A34A2E;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Switch loading */
.switch-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400rpx;
}
.switch-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 4rpx solid #E8E2D8;
  border-top-color: #A34A2E;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Book list */
.book-list {
  margin-bottom: 24rpx;
}
.book-row {
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
  padding: 24rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
  border: 1rpx solid rgba(163, 74, 46, 0.06);
}
.book-cover {
  width: 120rpx;
  aspect-ratio: 2 / 3;
  border-radius: 12rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.book-cover-img {
  width: 100%;
  height: 100%;
}
.book-cover-text {
  font-size: 40rpx;
  color: #fff;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
}
.book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 12rpx;
  overflow: hidden;
  min-height: 180rpx;
}
.book-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1C1C19;
  font-family: 'Noto Sans SC', sans-serif;
}
.book-author {
  font-size: 24rpx;
  color: #55423D;
  font-family: 'Noto Sans SC', sans-serif;
}
.book-meta {
  display: flex;
  gap: 12rpx;
  align-items: center;
  flex-wrap: wrap;
}
.book-tag {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-family: 'Noto Sans SC', sans-serif;
}
.book-tag.category {
  background: rgba(163, 74, 46, 0.08);
  color: #A34A2E;
}
.book-tag.wordcount {
  background: #F5F0EA;
  color: #645D55;
}
.book-extra {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 4rpx;
  flex-wrap: wrap;
}
.book-status {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-family: 'Noto Sans SC', sans-serif;
}
.book-status.ongoing {
  background: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}
.book-status.completed {
  background: rgba(163, 74, 46, 0.1);
  color: #A34A2E;
}
.book-chapters {
  font-size: 20rpx;
  color: #888888;
  font-family: 'Noto Sans SC', sans-serif;
}
.book-rating {
  display: flex;
  align-items: center;
  gap: 4rpx;
}
.rating-star {
  font-size: 20rpx;
  color: #FFD700;
}
.rating-num {
  font-size: 22rpx;
  color: #A34A2E;
  font-weight: 600;
}
.result-count {
  text-align: center;
  padding: 24rpx 0;
}
.result-count text {
  font-size: 24rpx;
  color: #AAAAAA;
  font-family: 'Noto Sans SC', sans-serif;
}
</style>
