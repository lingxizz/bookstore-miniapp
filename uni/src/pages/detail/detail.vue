<template>
  <view class="page" :style="{ backgroundColor: COLORS.bg }">
    <view class="nav-back" @click="uni.navigateBack()">
      <text class="back-arrow">←</text>
      <text class="back-text">返回</text>
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
      <view class="book-header">
        <view class="cover" :style="{ backgroundColor: coverColor }">
          <image v-if="book.cover" class="cover-img" :src="book.cover" mode="aspectFill" />
          <text v-else class="cover-text">{{ book.title.charAt(0) }}</text>
        </view>
        <view class="book-meta">
          <text class="title">{{ book.title }}</text>
          <view class="meta-row">
            <text class="author">{{ book.author }}</text>
            <text class="meta-dot">·</text>
            <text class="category">{{ book.category }}</text>
          </view>
          <view class="score-wrap">
            <text class="score">{{ book.rating }}</text>
            <text class="score-label">分</text>
          </view>
          <text class="price">{{ priceText }} · {{ chapters.length }}章</text>
          <view class="shelf-action" :class="{ active: inShelf }" @click="toggleShelf">
            <svg class="shelf-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
            </svg>
            <text class="shelf-action-text">{{ inShelf ? '已加入书架' : '加入书架' }}</text>
          </view>
        </view>
      </view>

      <view class="summary">
        <text class="section-title">简介</text>
        <text class="summary-text">{{ book.summary }}</text>
      </view>

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

      <view class="actions">
        <view class="btn btn-primary" @click="goReader()">
          <text>{{ lastChapterId ? '继续阅读' : '开始阅读' }}</text>
        </view>
        <view class="btn btn-secondary" @click="goRecharge">
          <text>充值</text>
        </view>
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
  padding: 48rpx 32rpx 120rpx;
}
.nav-back {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 32rpx;
}
.back-arrow {
  font-size: 36rpx;
  color: #2C2C2C;
}
.back-text {
  font-size: 28rpx;
  color: #888;
}
.book-header {
  display: flex;
  gap: 32rpx;
  margin-bottom: 32rpx;
}
.cover {
  width: 200rpx;
  height: 280rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.4);
  flex-shrink: 0;
  overflow: hidden;
}
.cover-text {
  color: #fff;
  font-size: 64rpx;
  font-weight: 700;
  opacity: 0.9;
}
.cover-img {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
}
.book-meta {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
}
.title {
  font-size: 44rpx;
  font-weight: 700;
  color: #2C2C2C;
  margin-bottom: 12rpx;
  word-break: break-word;
  overflow-wrap: break-word;
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 12rpx;
}
.author {
  font-size: 26rpx;
  color: #888;
  word-break: break-word;
}
.meta-dot {
  font-size: 20rpx;
  color: #AAA;
}
.category {
  font-size: 26rpx;
  color: #E8A23E;
}
.score-wrap {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
  margin-bottom: 12rpx;
  flex-wrap: wrap;
}
.score {
  font-size: 36rpx;
  font-weight: 700;
  color: #E8A23E;
}
.score-label {
  font-size: 24rpx;
  color: #E8A23E;
}
.price {
  font-size: 24rpx;
  color: #888888;
  margin-bottom: 8rpx;
}
.shelf-action {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 16rpx;
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
  background: #FFFFFF;
  border: 1rpx solid #E8E2D8;
  align-self: flex-start;
}
.shelf-action.active {
  background: #2D6A4F;
  border-color: #2D6A4F;
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
  stroke: #888888;
}
.shelf-action-text {
  font-size: 24rpx;
  color: #2C2C2C;
  font-weight: 500;
}
.summary {
  padding: 24rpx 0;
  border-bottom: 1rpx solid #E8E2D8;
}
.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #2C2C2C;
  margin-bottom: 12rpx;
}
.summary-text {
  font-size: 26rpx;
  color: #888888;
  line-height: 1.6;
  word-break: break-word;
  overflow-wrap: break-word;
}
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
}
.actions {
  display: flex;
  gap: 24rpx;
  padding: 24rpx 0;
}
.btn {
  flex: 1;
  padding: 24rpx 0;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.btn-primary {
  background: #E8A23E;
}
.btn-primary text {
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
}
.btn-secondary {
  background: #FFFFFF;
  border: 1rpx solid #E8E2D8;
}
.btn-secondary text {
  color: #2C2C2C;
  font-size: 30rpx;
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
  width: 200rpx;
  height: 280rpx;
  border-radius: 12rpx;
  background: #E8E2D8;
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
  background: #E8E2D8;
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
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
.skeleton-cover, .skeleton-line {
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}
</style>