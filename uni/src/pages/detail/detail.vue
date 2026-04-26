<template>
  <view class="page" :style="{ backgroundColor: '#F5F0EA' }">
    <!-- 顶部导航栏 -->
    <view class="top-bar">
      <view class="nav-back" @click="uni.navigateBack()">
        <text class="back-arrow">←</text>
        <text class="back-text">返回</text>
      </view>
      <view class="nav-actions">
        <text class="share-icon">↗</text>
      </view>
    </view>

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
      <!-- 书头区：大封面 + 右侧信息 -->
      <view class="book-header">
        <view class="cover" :style="{ backgroundColor: coverColor }">
          <image v-if="book.cover" class="cover-img" :src="book.cover" mode="aspectFill" />
          <text v-else class="cover-text">{{ book.title.charAt(0) }}</text>
        </view>
        <view class="book-meta">
          <text class="title">{{ book.title }}</text>
          <text class="author">{{ book.author }}</text>
          <view class="tag-row">
            <text class="tag-pill" v-if="book.category">{{ book.category }}</text>
          </view>
          <view class="stats-row">
            <view class="stat">
              <text class="score">{{ book.rating }}</text>
              <text class="score-label">分</text>
            </view>
            <view class="stat-divider" />
            <text class="stat-text">{{ chapters.length }}章</text>
            <view class="stat-divider" />
            <text class="stat-text">{{ priceText }}</text>
          </view>
          <view class="shelf-action" :class="{ active: inShelf }" @click="toggleShelf">
            <svg class="shelf-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
            </svg>
            <text class="shelf-action-text">{{ inShelf ? '已加入书架' : '加入书架' }}</text>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="actions">
        <view class="btn btn-outline" @click="toggleShelf">
          <text>{{ inShelf ? '已在书架' : '加入书架' }}</text>
        </view>
        <view class="btn btn-solid" @click="goReader()">
          <text>{{ lastChapterId ? '继续阅读' : '开始阅读' }}</text>
        </view>
      </view>

      <!-- 简介区 -->
      <view class="summary">
        <text class="section-title">简介</text>
        <text class="summary-text">{{ book.summary }}</text>
      </view>

      <!-- 目录列表 -->
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
import { COLORS, COVERS } from '@/utils/constants';
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
const priceText = computed(() => {
  if (chapters.value.every(c => c.isFree)) return '全本免费';
  if (freeCount.value > 0) return `前${freeCount.value}章免费`;
  return `${book.value.price || 10}金币/章`;
});

const goReader = (ch?: Chapter) => {
  const targetCh = ch || chapters.value.find(c => c.id === lastChapterId.value) || chapters.value[0];
  if (!targetCh) return;
  uni.navigateTo({
    url: `/pages/reader/reader?bookId=${book.value.id}&chapterId=${targetCh.id}`
  });
};

const goRecharge = () => {
  uni.navigateTo({ url: '/pages/recharge/recharge' });
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
        if (res.confirm) uni.navigateTo({ url: '/pages/me/me' });
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
        success: () => uni.navigateTo({ url: '/pages/me/me' })
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
  background: #F5F0EA;
}

/* 顶部导航 */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0 16rpx;
  margin-bottom: 16rpx;
}
.nav-back {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.back-arrow {
  font-size: 36rpx;
  color: #E8A23E;
  font-weight: 600;
}
.back-text {
  font-size: 28rpx;
  color: #888888;
  font-family: 'Noto Sans SC', sans-serif;
}
.nav-actions {
  display: flex;
  align-items: center;
  gap: 24rpx;
}
.share-icon {
  font-size: 36rpx;
  color: #E8A23E;
  padding: 8rpx;
}

/* 书头区 */
.book-header {
  display: flex;
  gap: 32rpx;
  margin-bottom: 32rpx;
}
.cover {
  width: 280rpx;
  aspect-ratio: 2 / 3;
  border-radius: 4rpx;
  border: 1rpx solid rgba(0,0,0,0.05);
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.1), 0 4rpx 24rpx rgba(232,162,62,0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}
.cover-text {
  color: #fff;
  font-size: 72rpx;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
  opacity: 0.9;
}
.cover-img {
  width: 100%;
  height: 100%;
  border-radius: 4rpx;
}
.book-meta {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
  overflow: hidden;
}
.title {
  font-size: 40rpx;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
  color: #2C2C2C;
  margin-bottom: 12rpx;
  word-break: break-word;
  overflow-wrap: break-word;
  line-height: 1.3;
}
.author {
  font-size: 26rpx;
  color: #888888;
  font-family: 'Noto Sans SC', sans-serif;
  margin-bottom: 12rpx;
  word-break: break-word;
}
.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-bottom: 12rpx;
}
.tag-pill {
  font-size: 22rpx;
  font-family: 'Noto Sans SC', sans-serif;
  color: #888888;
  background: #E8DED3;
  padding: 4rpx 16rpx;
  border-radius: 48rpx;
  font-weight: 500;
}
.stats-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
  flex-wrap: wrap;
}
.stat {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}
.score {
  font-size: 36rpx;
  font-weight: 700;
  color: #E8A23E;
  font-family: 'Noto Serif SC', serif;
}
.score-label {
  font-size: 22rpx;
  color: #E8A23E;
  font-family: 'Noto Sans SC', sans-serif;
}
.stat-divider {
  width: 1rpx;
  height: 28rpx;
  background: #E8E2D8;
}
.stat-text {
  font-size: 24rpx;
  color: #888888;
  font-family: 'Noto Sans SC', sans-serif;
}
.shelf-action {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 8rpx;
  padding: 12rpx 24rpx;
  border-radius: 48rpx;
  background: #FFFFFF;
  border: 1rpx solid #E8E2D8;
  align-self: flex-start;
  transition: all 0.2s ease;
}
.shelf-action.active {
  background: #E8A23E;
  border-color: #E8A23E;
}
.shelf-action.active .shelf-action-text {
  color: #fff;
}
.shelf-action.active .shelf-icon {
  stroke: #fff;
}
.shelf-icon {
  width: 28rpx;
  height: 28rpx;
  stroke: #AAAAAA;
}
.shelf-action-text {
  font-size: 24rpx;
  color: #2C2C2C;
  font-family: 'Noto Sans SC', sans-serif;
  font-weight: 500;
}

/* 操作按钮 */
.actions {
  display: flex;
  gap: 24rpx;
  padding: 8rpx 0 24rpx;
}
.btn {
  flex: 1;
  padding: 24rpx 0;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}
.btn-outline {
  background: #FFFFFF;
  border: 1rpx solid #E8E2D8;
}
.btn-outline text {
  color: #2C2C2C;
  font-size: 28rpx;
  font-family: 'Noto Sans SC', sans-serif;
  font-weight: 500;
}
.btn-solid {
  background: #E8A23E;
  border: none;
  box-shadow: 0 4rpx 12rpx rgba(232,162,62,0.2);
}
.btn-solid text {
  color: #fff;
  font-size: 28rpx;
  font-family: 'Noto Sans SC', sans-serif;
  font-weight: 600;
}

/* 简介区 */
.summary {
  padding: 24rpx 0;
  border-bottom: 1rpx solid #E8E2D8;
}
.section-title {
  font-size: 30rpx;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
  color: #2C2C2C;
  margin-bottom: 12rpx;
}
.summary-text {
  font-size: 28rpx;
  color: #888888;
  font-family: 'Noto Serif SC', serif;
  line-height: 1.8;
  word-break: break-word;
  overflow-wrap: break-word;
}

/* 目录列表 */
.chapters {
  padding: 24rpx 0;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}
.section-sub {
  font-size: 24rpx;
  color: #4CAF50;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 骨架屏 */
.skeleton {
  padding: 0;
}
.skeleton-header {
  display: flex;
  gap: 32rpx;
  margin-bottom: 32rpx;
}
.skeleton-cover {
  width: 280rpx;
  aspect-ratio: 2 / 3;
  border-radius: 4rpx;
  background: #F8F5F0;
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
  margin-bottom: 32rpx;
}
.skeleton-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #E8E2D8;
}
.skeleton-line {
  height: 28rpx;
  background: #F8F5F0;
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