<template>
  <view class="page" :style="{ backgroundColor: COLORS.bg }">
    <view class="header">
      <text class="header-label">书架</text>
      <text class="header-num">003</text>
    </view>

    <scroll-view scroll-y class="content">
      <!-- 加载中 -->
      <view v-if="isLoading" class="skeleton-list">
        <view v-for="i in 3" :key="i" class="skeleton-row">
          <view class="skeleton-cover" />
          <view class="skeleton-info">
            <view class="skeleton-line w60" />
            <view class="skeleton-line w40" />
            <view class="skeleton-line w80" />
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else-if="records.length === 0" class="empty">
        <text class="empty-icon">📚</text>
        <text class="empty-text">书架还是空的</text>
        <text class="empty-sub">去书城发现好书</text>
      </view>

      <!-- 列表 -->
      <view class="list" v-else>
        <BookRow
          v-for="rec in records"
          :key="rec.id"
          v-bind="rec.book"
        />
      </view>

      <Folio :num="3" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { COLORS } from '@/utils/constants';
import { fetchShelf, type Book } from '@/api/book';
import BookRow from '@/components/BookRow.vue';
import Folio from '@/components/Folio.vue';

interface ShelfItem {
  id: number;
  bookId: number;
  createdAt: string;
  book: Book;
}

const records = ref<ShelfItem[]>([]);
const isLoading = ref(true);

async function loadShelf() {
  isLoading.value = true;
  try {
    const items = await fetchShelf();
    console.log('[shelf] fetchShelf result count:', items?.length);
    records.value = items || [];
  } catch (e) {
    console.error('fetch shelf failed', e);
  } finally {
    isLoading.value = false;
  }
}

onMounted(loadShelf);
onShow(loadShelf);

const goDetail = (bookId: number) => {
  uni.navigateTo({ url: `/pages/detail/detail?id=${bookId}` });
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding-bottom: 100rpx;
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
}
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}
.empty-icon {
  font-size: 72rpx;
  margin-bottom: 24rpx;
}
.empty-text {
  font-size: 32rpx;
  color: #888;
  margin-bottom: 16rpx;
}
.empty-sub {
  font-size: 26rpx;
  color: #AAA;
}
.list {
  padding-bottom: 24rpx;
}

/* 骨架屏 */
.skeleton-list {
  padding-bottom: 24rpx;
}
.skeleton-row {
  display: flex;
  flex-direction: row;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #E8E2D8;
}
.skeleton-cover {
  width: 160rpx;
  height: 220rpx;
  border-radius: 12rpx;
  background: #E8E2D8;
  flex-shrink: 0;
  margin-right: 24rpx;
}
.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 16rpx;
}
.skeleton-line {
  height: 28rpx;
  background: #E8E2D8;
  border-radius: 6rpx;
  width: 100%;
}
.skeleton-line.w60 { width: 60%; }
.skeleton-line.w40 { width: 40%; }
.skeleton-line.w80 { width: 80%; }
@keyframes skeleton-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
.skeleton-cover, .skeleton-line {
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}
</style>