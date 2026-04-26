<template>
  <view class="page" :style="{ backgroundColor: '#F8F4F0' }">
    <!-- Loading skeleton -->
    <view class="skeleton" v-if="isLoading">
      <view class="skeleton-header">
        <view class="skeleton-cover" />
        <view class="skeleton-meta">
          <view class="skeleton-line w60" />
          <view class="skeleton-line w40" />
          <view class="skeleton-line w30" />
        </view>
      </view>
      <view class="skeleton-block">
        <view class="skeleton-line w20" />
        <view class="skeleton-line" />
        <view class="skeleton-line" />
        <view class="skeleton-line w80" />
      </view>
      <view class="skeleton-block">
        <view class="skeleton-line w20" />
        <view class="skeleton-item" v-for="i in 5" :key="i">
          <view class="skeleton-line w10" />
          <view class="skeleton-line w70" />
        </view>
      </view>
    </view>

    <template v-else>
      <!-- Top bar -->
      <view class="top-bar">
        <view class="nav-back" @click="uni.navigateBack()">
          <svg viewBox="0 0 24 24" fill="none" stroke="#A34A2E" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M15 18l-6-6 6-6"/>
          </svg>
          <text class="back-text">返回</text>
        </view>
      </view>

      <!-- Book header card -->
      <view class="book-header">
        <view class="cover" :style="{ backgroundColor: coverColor }">
          <image v-if="book.cover" class="cover-img" :src="book.cover" mode="aspectFill" />
          <text v-else class="cover-text">{{ book.title.charAt(0) }}</text>
        </view>
        <view class="book-meta">
          <text class="title">{{ book.title }}</text>
          <text class="author">{{ book.author }}</text>
          <view class="tag-row">
            <text class="tag-pill">{{ book.category }}</text>
            <text class="tag-pill wordcount">{{ formatWordCount(book.wordCount) }}字</text>
          </view>
          <view class="stats-row">
            <view class="stat">
              <text class="score">{{ book.rating }}</text>
              <text class="score-label">分</text>
            </view>
            <view class="stat-divider" />
            <text class="stat-text">{{ chapters.length }}章</text>
            <view class="stat-divider" />
            <text class="stat-text" :class="book.status">{{ book.status === 'completed' ? '已完结' : '连载中' }}</text>
          </view>
        </view>
      </view>

      <!-- Action buttons -->
      <view class="actions">
        <view class="btn btn-outline" :class="{ active: inShelf }" @click="toggleShelf">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
          </svg>
          <text>{{ inShelf ? '已在书架' : '加入书架' }}</text>
        </view>
        <view class="btn btn-solid" @click="goReader()">
          <text>{{ lastChapterId ? '继续阅读' : '开始阅读' }}</text>
        </view>
      </view>

      <!-- Summary card -->
      <view class="summary">
        <text class="section-title">简介</text>
        <text class="summary-text">{{ book.summary }}</text>
      </view>

      <!-- Chapters card -->
      <view class="chapters">
        <view class="section-header">
          <text class="section-title">章节目录</text>
          <text class="section-sub">前{{ freeCount }}章免费</text>
        </view>
        <ChapterItem
          v-for="ch in chapters"
          :key="ch.id"
          :num="ch.order"
          :title="ch.title"
          :is-premium="!ch.isFree"
          @click="goReader(ch)"
        />
      </view>

      <Folio :num="5" />
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { COVERS } from '@/utils/constants';
import { fetchBook, fetchChapters, checkShelf, fetchProgress, addToShelf, removeFromShelf, type Book, type Chapter } from '@/api/book';
import ChapterItem from '@/components/ChapterItem.vue';
import Folio from '@/components/Folio.vue';

const book = ref<Partial<Book>>({});
const chapters = ref<Chapter[]>([]);
const isLoading = ref(true);
const firstLoad = ref(true);
const inShelf = ref(false);
const lastChapterId = ref<number | null>(null);

const coverColor = computed(() => COVERS[(book.value.id || 0) % COVERS.length]);

onLoad(async (opts: any) => {
  const id = parseInt(opts?.id || '1');
  try {
    const [b, chs, shelfRes, progress] = await Promise.all([
      fetchBook(id),
      fetchChapters(id),
      checkShelf(id).catch(() => ({ inShelf: false })),
      fetchProgress(id).catch(() => ({ chapterId: null, progress: 0 })),
    ]);
    if (b) book.value = b;
    if (chs) chapters.value = chs;
    inShelf.value = shelfRes.inShelf;
    lastChapterId.value = progress.chapterId;
  } catch (e) {
    console.error('fetch detail failed', e);
  } finally {
    isLoading.value = false;
    firstLoad.value = false;
  }
});

const freeCount = computed(() => chapters.value.filter(c => c.isFree).length);

function formatWordCount(n?: number): string {
  if (!n) return '0';
  if (n >= 10000) return (n / 10000).toFixed(1) + '万';
  return String(n);
}

const goReader = (ch?: Chapter) => {
  const targetCh = ch || chapters.value.find(c => c.id === lastChapterId.value) || chapters.value[0];
  if (!targetCh) return;
  uni.navigateTo({
    url: `/pages/reader/reader?bookId=${book.value.id}&chapterId=${targetCh.id}`
  });
};

const toggleShelf = async () => {
  if (!book.value.id) return;
  const token = uni.getStorageSync('token');
  if (!token) {
    uni.showModal({
      title: '需要登录',
      content: '登录后即可管理书架',
      showCancel: true,
      success: (res: any) => {
        if (res.confirm) uni.switchTab({ url: '/pages/me/me' });
      }
    });
    return;
  }
  try {
    if (inShelf.value) {
      await removeFromShelf(book.value.id);
      inShelf.value = false;
      uni.showToast({ title: '已移除书架', icon: 'none' });
    } else {
      await addToShelf(book.value.id);
      inShelf.value = true;
      uni.showToast({ title: '已加入书架', icon: 'none' });
    }
  } catch (e: any) {
    const msg = String(e?.message || e || '');
    if (msg.includes('Unauthorized') || msg.includes('Forbidden') || msg.includes('401') || msg.includes('403')) {
      uni.showModal({
        title: '登录已过期',
        content: '请重新登录',
        showCancel: false,
        success: () => uni.switchTab({ url: '/pages/me/me' })
      });
      return;
    }
    uni.showToast({ title: '操作失败: ' + msg, icon: 'none' });
  }
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 0 32rpx 120rpx;
  background: #F8F4F0;
}

/* Top bar */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0 16rpx;
}
.nav-back {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.nav-back svg {
  width: 32rpx;
  height: 32rpx;
}
.back-text {
  font-size: 28rpx;
  color: #A34A2E;
  font-family: 'Noto Sans SC', sans-serif;
}

/* Book header */
.book-header {
  display: flex;
  gap: 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 24rpx rgba(163, 74, 46, 0.04);
  border: 1rpx solid rgba(163, 74, 46, 0.06);
}
.cover {
  width: 200rpx;
  aspect-ratio: 2 / 3;
  border-radius: 16rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.1);
}
.cover-img {
  width: 100%;
  height: 100%;
}
.cover-text {
  color: #fff;
  font-size: 64rpx;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
}
.book-meta {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
  gap: 12rpx;
  overflow: hidden;
}
.title {
  font-size: 36rpx;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
  color: #1C1C19;
  line-height: 1.3;
  word-break: break-word;
}
.author {
  font-size: 26rpx;
  color: #55423D;
  font-family: 'Noto Sans SC', sans-serif;
}
.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}
.tag-pill {
  font-size: 22rpx;
  font-family: 'Noto Sans SC', sans-serif;
  color: #A34A2E;
  background: rgba(163, 74, 46, 0.08);
  padding: 6rpx 16rpx;
  border-radius: 48rpx;
}
.tag-pill.wordcount {
  color: #645D55;
  background: #F5F0EA;
}
.stats-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
  margin-top: 4rpx;
}
.stat {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}
.score {
  font-size: 36rpx;
  font-weight: 700;
  color: #A34A2E;
  font-family: 'Noto Serif SC', serif;
}
.score-label {
  font-size: 22rpx;
  color: #A34A2E;
  font-family: 'Noto Sans SC', sans-serif;
}
.stat-divider {
  width: 1rpx;
  height: 28rpx;
  background: #E8E2D8;
}
.stat-text {
  font-size: 24rpx;
  color: #645D55;
  font-family: 'Noto Sans SC', sans-serif;
}
.stat-text.ongoing {
  color: #4CAF50;
}
.stat-text.completed {
  color: #A34A2E;
}

/* Action buttons */
.actions {
  display: flex;
  gap: 24rpx;
  margin-bottom: 24rpx;
}
.btn {
  flex: 1;
  padding: 24rpx 0;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  transition: all 0.2s ease;
}
.btn-outline {
  background: #FFFFFF;
  border: 1rpx solid rgba(163, 74, 46, 0.2);
  box-shadow: 0 2rpx 12rpx rgba(163, 74, 46, 0.04);
}
.btn-outline text {
  color: #1C1C19;
  font-size: 28rpx;
  font-family: 'Noto Sans SC', sans-serif;
  font-weight: 500;
}
.btn-outline.active {
  background: rgba(163, 74, 46, 0.08);
  border-color: #A34A2E;
}
.btn-outline.active text {
  color: #A34A2E;
}
.btn-solid {
  background: #A34A2E;
  border: none;
  box-shadow: 0 4rpx 16rpx rgba(163, 74, 46, 0.2);
}
.btn-solid text {
  color: #fff;
  font-size: 28rpx;
  font-family: 'Noto Sans SC', sans-serif;
  font-weight: 600;
}
.btn-icon {
  width: 28rpx;
  height: 28rpx;
}

/* Summary card */
.summary {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 24rpx rgba(163, 74, 46, 0.04);
  border: 1rpx solid rgba(163, 74, 46, 0.06);
}
.section-title {
  font-size: 32rpx;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
  color: #1C1C19;
  margin-bottom: 16rpx;
}
.summary-text {
  font-size: 28rpx;
  color: #645D55;
  font-family: 'Noto Serif SC', serif;
  line-height: 1.8;
  word-break: break-word;
}

/* Chapters card */
.chapters {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 24rpx rgba(163, 74, 46, 0.04);
  border: 1rpx solid rgba(163, 74, 46, 0.06);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}
.section-sub {
  font-size: 24rpx;
  color: #4CAF50;
  font-family: 'Noto Sans SC', sans-serif;
}

/* Skeleton */
.skeleton {
  padding: 0;
}
.skeleton-header {
  display: flex;
  gap: 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}
.skeleton-cover {
  width: 200rpx;
  aspect-ratio: 2 / 3;
  border-radius: 16rpx;
  background: #F5F0EA;
  flex-shrink: 0;
}
.skeleton-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 16rpx;
}
.skeleton-block {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}
.skeleton-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F5F0EA;
}
.skeleton-line {
  height: 28rpx;
  background: #F5F0EA;
  border-radius: 6rpx;
  width: 100%;
}
.skeleton-line.w80 { width: 80%; }
.skeleton-line.w70 { width: 70%; }
.skeleton-line.w60 { width: 60%; }
.skeleton-line.w40 { width: 40%; }
.skeleton-line.w30 { width: 30%; }
.skeleton-line.w20 { width: 20%; }
.skeleton-line.w10 { width: 48rpx; }
@keyframes skeleton-pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}
.skeleton-cover, .skeleton-line {
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}
</style>
