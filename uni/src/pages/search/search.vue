<template>
  <view class="page">
    <!-- 搜索栏 -->
    <view class="search-header">
      <view class="search-input-wrap">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="#A34A2E" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"/>
          <path d="m21 21-4.3-4.3"/>
        </svg>
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索书名、作者"
          confirm-type="search"
          maxlength="20"
          @confirm="onSearch"
          focus
        />
        <view v-if="keyword" class="search-clear" @click="keyword = ''">
          <svg viewBox="0 0 24 24" fill="none" stroke="#AAAAAA" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <path d="m15 9-6 6M9 9l6 6"/>
          </svg>
        </view>
      </view>
      <text class="search-cancel" @click="goBack">取消</text>
    </view>

    <!-- 搜索结果 -->
    <view class="result-list" v-if="hasSearched">
      <view v-if="results.length === 0" class="result-empty">
        <text>未找到相关书籍</text>
      </view>
      <view
        class="result-item"
        v-for="book in results"
        :key="book.id"
        @click="goDetail(book.id)"
      >
        <view class="result-cover" :style="{ backgroundColor: COVERS[(book.id || 0) % COVERS.length] }">
          <image v-if="book.cover" class="result-cover-img" :src="book.cover" mode="aspectFill" />
          <text v-else class="result-cover-text">{{ book.title.charAt(0) }}</text>
        </view>
        <view class="result-info">
          <text class="result-title">{{ book.title }}</text>
          <text class="result-author">{{ book.author }}</text>
          <text class="result-summary">{{ book.summary }}</text>
          <view class="result-footer">
            <text class="result-category">{{ book.category }}</text>
            <text class="result-status" :class="book.status">{{ book.status === 'ongoing' ? '连载中' : '已完结' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 搜索历史 -->
    <view class="history-section" v-else-if="searchHistory.length > 0">
      <view class="history-header">
        <text class="history-title">搜索历史</text>
        <text class="history-action" @click="toggleManageMode">
          {{ isManageMode ? '完成' : '管理' }}
        </text>
      </view>
      <view class="history-list">
        <view
          class="history-tag"
          v-for="word in searchHistory"
          :key="word"
          :class="{ managing: isManageMode }"
          @click="onTagClick(word)"
          @longpress="enterManageMode"
        >
          <text class="history-tag-text">{{ word }}</text>
          <view v-if="isManageMode" class="history-delete" @click.stop="removeHistory(word)">
            <svg viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 6 6 18M6 6l12 12"/>
            </svg>
          </view>
        </view>
      </view>
      <view v-if="isManageMode" class="history-clear-all" @click="clearHistory">
        <text>清空全部</text>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="search-tips" v-else>
      <text class="tips-text">输入书名或作者名进行搜索</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { searchBooks, type Book } from '@/api/book';
import { COVERS } from '@/utils/constants';

const keyword = ref('');
const results = ref<Book[]>([]);
const hasSearched = ref(false);
const searchHistory = ref<string[]>([]);
const isManageMode = ref(false);

const HISTORY_KEY = 'search_history';
const MAX_HISTORY = 15;

function loadHistory() {
  try {
    const raw = uni.getStorageSync(HISTORY_KEY);
    if (raw) searchHistory.value = JSON.parse(raw);
  } catch (e) {}
}

function saveHistory(word: string) {
  let list = searchHistory.value.filter(w => w !== word);
  list.unshift(word);
  if (list.length > MAX_HISTORY) list = list.slice(0, MAX_HISTORY);
  searchHistory.value = list;
  uni.setStorageSync(HISTORY_KEY, JSON.stringify(list));
}

function removeHistory(word: string) {
  searchHistory.value = searchHistory.value.filter(w => w !== word);
  uni.setStorageSync(HISTORY_KEY, JSON.stringify(searchHistory.value));
  if (searchHistory.value.length === 0) isManageMode.value = false;
}

function clearHistory() {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空所有搜索历史吗？',
    success: (res) => {
      if (res.confirm) {
        searchHistory.value = [];
        uni.removeStorageSync(HISTORY_KEY);
        isManageMode.value = false;
      }
    },
  });
}

function toggleManageMode() {
  isManageMode.value = !isManageMode.value;
}

function enterManageMode() {
  if (!isManageMode.value) isManageMode.value = true;
}

function onTagClick(word: string) {
  if (isManageMode.value) return;
  keyword.value = word;
  onSearch();
}

async function onSearch() {
  const word = keyword.value.trim();
  if (!word) return;
  if (word.length > 20) {
    uni.showToast({ title: '搜索内容过长', icon: 'none' });
    return;
  }
  isManageMode.value = false;
  hasSearched.value = true;
  saveHistory(word);
  try {
    const data = await searchBooks(word);
    results.value = data.map((b: any) => ({
      ...b,
      coverColor: COVERS[(b.id || 0) % COVERS.length],
    }));
  } catch (e) {
    results.value = [];
  }
}

function goDetail(id: number) {
  if (!id) return;
  uni.navigateTo({ url: `/pages/detail/detail?id=${id}` });
}

function goBack() {
  uni.navigateBack();
}

onShow(() => {
  loadHistory();
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #F8F4F0;
  padding: 0 32rpx;
}

/* 搜索栏 */
.search-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx 0;
}
.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: #FFFFFF;
  border-radius: 48rpx;
  padding: 16rpx 24rpx;
  border: 1rpx solid rgba(163, 74, 46, 0.08);
  box-shadow: 0 2rpx 12rpx rgba(163, 74, 46, 0.04);
}
.search-icon {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
}
.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #1C1C19;
  font-family: 'Noto Sans SC', sans-serif;
  border: none;
  outline: none;
  background: transparent;
}
.search-input::placeholder {
  color: #AAAAAA;
}
.search-clear {
  width: 36rpx;
  height: 36rpx;
  flex-shrink: 0;
}
.search-cancel {
  font-size: 28rpx;
  color: #A34A2E;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 搜索历史 */
.history-section {
  padding-top: 24rpx;
}
.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}
.history-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1C1C19;
  font-family: 'Noto Serif SC', serif;
}
.history-action {
  font-size: 26rpx;
  color: #A34A2E;
  font-family: 'Noto Sans SC', sans-serif;
}
.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}
.history-tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  border-radius: 48rpx;
  background: #FFFFFF;
  border: 1rpx solid rgba(163, 74, 46, 0.1);
  box-shadow: 0 2rpx 8rpx rgba(163, 74, 46, 0.04);
  position: relative;
  transition: all 0.2s ease;
}
.history-tag:active {
  opacity: 0.7;
}
.history-tag.managing {
  padding-right: 48rpx;
  background: #FEF6F3;
  border-color: rgba(163, 74, 46, 0.2);
}
.history-tag-text {
  font-size: 26rpx;
  color: #1C1C19;
  font-family: 'Noto Sans SC', sans-serif;
}
.history-delete {
  position: absolute;
  right: -8rpx;
  top: -8rpx;
  width: 32rpx;
  height: 32rpx;
  background: #FF5252;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.history-delete svg {
  width: 16rpx;
  height: 16rpx;
}
.history-clear-all {
  margin-top: 40rpx;
  text-align: center;
  padding: 20rpx 0;
}
.history-clear-all text {
  font-size: 26rpx;
  color: #AAAAAA;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 搜索结果 */
.result-list {
  padding-bottom: 40rpx;
}
.result-empty {
  padding: 120rpx 0;
  text-align: center;
}
.result-empty text {
  font-size: 28rpx;
  color: #AAAAAA;
  font-family: 'Noto Sans SC', sans-serif;
}
.result-item {
  display: flex;
  gap: 20rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
  border: 1rpx solid rgba(163, 74, 46, 0.06);
}
.result-cover {
  width: 140rpx;
  aspect-ratio: 2 / 3;
  border-radius: 12rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.result-cover-img {
  width: 100%;
  height: 100%;
}
.result-cover-text {
  font-size: 48rpx;
  color: #fff;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
}
.result-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  overflow: hidden;
  min-width: 0;
}
.result-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1C1C19;
  font-family: 'Noto Sans SC', sans-serif;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.result-author {
  font-size: 22rpx;
  color: #55423D;
  font-family: 'Noto Sans SC', sans-serif;
}
.result-summary {
  font-size: 22rpx;
  color: #888888;
  font-family: 'Noto Sans SC', sans-serif;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.result-footer {
  display: flex;
  gap: 12rpx;
  margin-top: auto;
  padding-top: 4rpx;
}
.result-category {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background: rgba(163, 74, 46, 0.08);
  color: #A34A2E;
  font-family: 'Noto Sans SC', sans-serif;
}
.result-status {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-family: 'Noto Sans SC', sans-serif;
}
.result-status.ongoing {
  background: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}
.result-status.completed {
  background: rgba(163, 74, 46, 0.1);
  color: #A34A2E;
}

/* 搜索提示 */
.search-tips {
  padding: 120rpx 0;
  text-align: center;
}
.tips-text {
  font-size: 28rpx;
  color: #AAAAAA;
  font-family: 'Noto Sans SC', sans-serif;
}
</style>
