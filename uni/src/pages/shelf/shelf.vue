<template>
  <view class="page" :style="{ backgroundColor: '#F8F4F0' }">
    <!-- Loading skeleton -->
    <view v-if="isLoading" class="skeleton-page">
      <view class="sk-header"></view>
      <view class="sk-card">
        <view class="sk-cover-lg"></view>
        <view class="sk-info">
          <view class="sk-line w70"></view>
          <view class="sk-line w40"></view>
          <view class="sk-line w90"></view>
        </view>
      </view>
      <view class="sk-stats">
        <view class="sk-stat" v-for="i in 3" :key="i">
          <view class="sk-line w60"></view>
          <view class="sk-line w40"></view>
        </view>
      </view>
      <view class="sk-tabs">
        <view class="sk-tab" v-for="i in 3" :key="i"></view>
      </view>
      <view class="sk-book-row" v-for="i in 3" :key="i">
        <view class="sk-cover"></view>
        <view class="sk-book-info">
          <view class="sk-line w70"></view>
          <view class="sk-line w40"></view>
          <view class="sk-line w60"></view>
        </view>
      </view>
    </view>

    <view v-else>
      <!-- Header -->
      <view class="header">
        <text class="header-label">书架</text>
        <text class="header-num">{{ String(records.length).padStart(3, '0') }}</text>
      </view>

      <!-- 最近在读 -->
      <view class="recent-card" v-if="recentBook" @click="goDetail(recentBook.bookId)">
        <view class="recent-cover" :style="{ backgroundColor: recentBook.coverColor || '#A34A2E' }">
          <image v-if="recentBook.cover" class="recent-cover-img" :src="recentBook.cover" mode="aspectFill" />
          <text v-else class="recent-cover-text">{{ recentBook.title?.charAt(0) }}</text>
        </view>
        <view class="recent-info">
          <text class="recent-title">{{ recentBook.title }}</text>
          <text class="recent-author">{{ recentBook.author }}</text>
          <view class="recent-meta">
            <text class="recent-wordcount">{{ formatWordCount(recentBook.wordCount) }}字</text>
            <text class="recent-dot">·</text>
            <text class="recent-status" :class="recentBook.status">{{ recentBook.status === 'ongoing' ? '连载中' : '已完结' }}</text>
          </view>
          <view class="recent-progress">
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: recentBook.progress + '%' }"></view>
            </view>
            <text class="progress-text">已读 {{ recentBook.progress }}%</text>
          </view>
          <text class="recent-continue">继续阅读 →</text>
        </view>
      </view>

      <!-- Stats -->
      <view class="stats-panel">
        <view class="stat-item">
          <text class="stat-num">{{ readingCount }}</text>
          <text class="stat-label">在读</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-num">{{ finishedCount }}</text>
          <text class="stat-label">读完</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-num">{{ records.length }}</text>
          <text class="stat-label">总计</text>
        </view>
      </view>

      <!-- Tabs -->
      <view class="tabs">
        <view class="tab" v-for="t in tabList" :key="t.value"
              :class="{ active: activeTab === t.value }" @click="activeTab = t.value">
          <text>{{ t.label }}</text>
        </view>
      </view>

      <!-- Book list -->
      <view class="book-list">
        <view class="book-row" v-for="rec in filteredRecords" :key="rec.id" @click="goDetail(rec.bookId)">
          <view class="book-cover" :style="{ backgroundColor: rec.coverColor || '#A34A2E' }">
            <image v-if="rec.cover" class="book-cover-img" :src="rec.cover" mode="aspectFill" />
            <text v-else class="book-cover-text">{{ rec.title?.charAt(0) }}</text>
          </view>
          <view class="book-body">
            <view class="book-main">
              <text class="book-title">{{ rec.title }}</text>
              <text class="book-author">{{ rec.author }}</text>
              <view class="book-tags">
                <text class="book-tag category">{{ rec.category }}</text>
                <text class="book-tag wordcount">{{ formatWordCount(rec.wordCount) }}字</text>
                <text class="book-tag status" :class="rec.status">{{ rec.status === 'ongoing' ? '连载中' : '已完结' }}</text>
              </view>
            </view>
            <view class="book-progress-wrap">
              <view class="progress-bar-sm">
                <view class="progress-fill-sm" :style="{ width: rec.progress + '%' }"></view>
              </view>
              <text class="progress-text-sm">{{ rec.progress }}%</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Empty state -->
      <view class="empty" v-if="filteredRecords.length === 0">
        <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="#CCCCCC" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
          <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
        </svg>
        <text class="empty-text">书架还是空的</text>
        <text class="empty-sub">去书城发现好书</text>
      </view>

      <Folio :num="3" />
    </view>
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { COVERS } from '@/utils/constants';
import { fetchShelf, type ShelfItem } from '@/api/book';
import Folio from '@/components/Folio.vue';
import CustomTabBar from '@/components/CustomTabBar.vue';

interface ExtendedShelfItem extends ShelfItem {
  coverColor?: string;
}

const records = ref<ExtendedShelfItem[]>([]);
const isLoading = ref(true);
const firstLoad = ref(true);
const activeTab = ref('all');
const tabList = [
  { label: '全部', value: 'all' },
  { label: '在读', value: 'reading' },
  { label: '读完', value: 'finished' },
];

const readingCount = computed(() => records.value.filter(r => r.readStatus === 'reading').length);
const finishedCount = computed(() => records.value.filter(r => r.readStatus === 'finished').length);

const recentBook = computed(() => {
  const reading = records.value.filter(r => r.readStatus === 'reading');
  return reading.length > 0 ? reading[0] : records.value[0];
});

const filteredRecords = computed(() => {
  if (activeTab.value === 'all') return records.value;
  return records.value.filter(r => r.readStatus === activeTab.value);
});

function formatWordCount(n?: number): string {
  if (!n) return '0';
  if (n >= 10000) return (n / 10000).toFixed(1) + '万';
  return String(n);
}

onMounted(() => {
  isLoading.value = firstLoad.value;
  loadShelf();
});

onShow(() => {
  if (firstLoad.value) {
    isLoading.value = true;
    loadShelf();
  }
});

async function loadShelf() {
  try {
    const items = await fetchShelf();
    records.value = items.map((item: any) => ({
      ...item,
      coverColor: COVERS[(item.bookId || 0) % COVERS.length],
    }));
  } catch (e) {
    console.error('fetch shelf failed', e);
  } finally {
    isLoading.value = false;
    firstLoad.value = false;
  }
}

function goDetail(id: number) {
  if (!id) return;
  uni.navigateTo({ url: '/pages/detail/detail?id=' + id });
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
.sk-card {
  display: flex;
  gap: 24rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}
.sk-cover-lg {
  width: 160rpx;
  aspect-ratio: 2/3;
  border-radius: 16rpx;
  background: #E8E2D8;
  animation: shimmer 1.5s infinite;
}
.sk-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.sk-line {
  height: 24rpx;
  background: #E8E2D8;
  border-radius: 8rpx;
  animation: shimmer 1.5s infinite;
}
.sk-line.w70 { width: 70%; }
.sk-line.w40 { width: 40%; }
.sk-line.w90 { width: 90%; }
.sk-line.w60 { width: 60%; }
.sk-stats {
  display: flex;
  justify-content: space-around;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}
.sk-stat {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  align-items: center;
}
.sk-tabs {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}
.sk-tab {
  width: 120rpx;
  height: 56rpx;
  background: #E8E2D8;
  border-radius: 28rpx;
  animation: shimmer 1.5s infinite;
}
.sk-book-row {
  display: flex;
  gap: 20rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F5F0EA;
}
.sk-cover {
  width: 100rpx;
  aspect-ratio: 2/3;
  border-radius: 8rpx;
  background: #E8E2D8;
  animation: shimmer 1.5s infinite;
}
.sk-book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
@keyframes shimmer {
  0% { background: #E8E2D8; }
  50% { background: #F0EBE3; }
  100% { background: #E8E2D8; }
}

/* Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding: 48rpx 0 24rpx;
}
.header-label {
  font-size: 40rpx;
  font-weight: 700;
  color: #1C1C19;
  font-family: 'Noto Serif SC', serif;
}
.header-num {
  font-size: 24rpx;
  color: #AAAAAA;
  font-family: 'Noto Serif SC', serif;
}

/* Recent card */
.recent-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx;
  display: flex;
  gap: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 24rpx rgba(163, 74, 46, 0.04);
  border: 1rpx solid rgba(163, 74, 46, 0.06);
}
.recent-cover {
  width: 160rpx;
  aspect-ratio: 2 / 3;
  border-radius: 16rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.recent-cover-img {
  width: 100%;
  height: 100%;
}
.recent-cover-text {
  font-size: 48rpx;
  color: #fff;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
}
.recent-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  overflow: hidden;
  min-width: 0;
}
.recent-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1C1C19;
  font-family: 'Noto Serif SC', serif;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.recent-author {
  font-size: 24rpx;
  color: #55423D;
  font-family: 'Noto Sans SC', sans-serif;
}
.recent-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 4rpx;
}
.recent-wordcount {
  font-size: 22rpx;
  color: #645D55;
  font-family: 'Noto Sans SC', sans-serif;
}
.recent-dot {
  font-size: 22rpx;
  color: #CCCCCC;
}
.recent-status {
  font-size: 22rpx;
  font-family: 'Noto Sans SC', sans-serif;
}
.recent-status.ongoing {
  color: #4CAF50;
}
.recent-status.completed {
  color: #A34A2E;
}
.recent-progress {
  margin-top: 8rpx;
}
.progress-bar {
  height: 8rpx;
  background: #F0EBE3;
  border-radius: 4rpx;
  margin-bottom: 8rpx;
}
.progress-fill {
  height: 100%;
  background: #A34A2E;
  border-radius: 4rpx;
}
.progress-text {
  font-size: 22rpx;
  color: #645D55;
  font-family: 'Noto Sans SC', sans-serif;
}
.recent-continue {
  font-size: 24rpx;
  color: #A34A2E;
  font-weight: 600;
  font-family: 'Noto Sans SC', sans-serif;
  margin-top: 8rpx;
}

/* Stats */
.stats-panel {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 24rpx rgba(163, 74, 46, 0.04);
  border: 1rpx solid rgba(163, 74, 46, 0.06);
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}
.stat-num {
  font-size: 40rpx;
  font-weight: 700;
  color: #A34A2E;
  font-family: 'Noto Serif SC', serif;
}
.stat-label {
  font-size: 24rpx;
  color: #645D55;
  font-family: 'Noto Sans SC', sans-serif;
}
.stat-divider {
  width: 1rpx;
  height: 60rpx;
  background: #E8E2D8;
}

/* Tabs */
.tabs {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}
.tab {
  padding: 12rpx 32rpx;
  border-radius: 48rpx;
  background: #FFFFFF;
  border: 1rpx solid rgba(163, 74, 46, 0.1);
}
.tab.active {
  background: #A34A2E;
  border-color: #A34A2E;
}
.tab text {
  font-size: 26rpx;
  color: #1C1C19;
  font-family: 'Noto Sans SC', sans-serif;
}
.tab.active text {
  color: #FFFFFF;
}

/* Book list */
.book-list {
  margin-bottom: 24rpx;
}
.book-row {
  display: flex;
  gap: 20rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 20rpx;
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
.book-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
  min-width: 0;
  gap: 12rpx;
}
.book-main {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
.book-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1C1C19;
  font-family: 'Noto Sans SC', sans-serif;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.book-author {
  font-size: 22rpx;
  color: #55423D;
  font-family: 'Noto Sans SC', sans-serif;
}
.book-tags {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 4rpx;
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
.book-tag.status.ongoing {
  background: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}
.book-tag.status.completed {
  background: rgba(163, 74, 46, 0.1);
  color: #A34A2E;
}
.book-progress-wrap {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.progress-bar-sm {
  flex: 1;
  height: 6rpx;
  background: #F0EBE3;
  border-radius: 3rpx;
}
.progress-fill-sm {
  height: 100%;
  background: #A34A2E;
  border-radius: 3rpx;
}
.progress-text-sm {
  font-size: 20rpx;
  color: #645D55;
  font-family: 'Noto Sans SC', sans-serif;
  flex-shrink: 0;
}

/* Empty */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  padding: 80rpx 0;
}
.empty-icon {
  width: 80rpx;
  height: 80rpx;
}
.empty-text {
  font-size: 28rpx;
  color: #645D55;
  font-family: 'Noto Sans SC', sans-serif;
}
.empty-sub {
  font-size: 24rpx;
  color: #AAAAAA;
  font-family: 'Noto Sans SC', sans-serif;
}
</style>
