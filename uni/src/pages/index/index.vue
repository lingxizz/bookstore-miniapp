<template>
  <view class="page" :style="{ backgroundColor: '#F8F4F0' }">
    <!-- Loading skeleton -->
    <view v-if="isLoading" class="skeleton-page">
      <view class="sk-search"></view>
      <view class="sk-card">
        <view class="sk-cover-lg"></view>
        <view class="sk-info">
          <view class="sk-line w70"></view>
          <view class="sk-line w40"></view>
          <view class="sk-line w90"></view>
          <view class="sk-btn-sm"></view>
        </view>
      </view>
      <view class="sk-section">
        <view class="sk-line w50 title"></view>
      </view>
      <view class="sk-rank">
        <view class="sk-rank-item" v-for="i in 5" :key="i">
          <view class="sk-num"></view>
          <view class="sk-cover-sm"></view>
          <view class="sk-rank-text">
            <view class="sk-line w60"></view>
            <view class="sk-line w30"></view>
          </view>
        </view>
      </view>
      <view class="sk-section">
        <view class="sk-line w50 title"></view>
      </view>
      <view class="sk-tags">
        <view class="sk-tag" v-for="i in 5" :key="i"></view>
      </view>
      <view class="sk-section">
        <view class="sk-line w50 title"></view>
      </view>
      <view class="sk-grid">
        <view class="sk-grid-item" v-for="i in 3" :key="i">
          <view class="sk-cover-md"></view>
          <view class="sk-grid-text">
            <view class="sk-line w60"></view>
            <view class="sk-line w40"></view>
            <view class="sk-line w80"></view>
          </view>
        </view>
      </view>
    </view>

    <view v-else>
      <!-- 顶部搜索栏 -->
      <view class="search-bar" @click="goSearch">
        <svg class="search-icon-svg" viewBox="0 0 24 24" fill="none" stroke="#A34A2E" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"/>
          <path d="m21 21-4.3-4.3"/>
        </svg>
        <text class="search-placeholder">搜索书名、作者</text>
      </view>

      <!-- 引用卡片（可滑动） -->
      <swiper class="quote-swiper" circular autoplay interval="5000">
        <swiper-item v-for="(quote, idx) in quotes" :key="idx">
          <view class="quote-card">
            <view class="quote-content">
              <svg class="quote-icon" viewBox="0 0 24 24" fill="#A34A2E">
                <path d="M4.583 17.321C3.553 16.227 3 15 3 13.011c0-3.5 2.457-6.637 6.03-8.188l.893 1.378c-3.335 1.804-3.987 4.145-4.247 5.621.537-.278 1.24-.375 1.929-.311 1.804.167 3.226 1.648 3.226 3.489a3.5 3.5 0 01-3.5 3.5c-1.073 0-2.099-.49-2.748-1.179zm10 0C13.553 16.227 13 15 13 13.011c0-3.5 2.457-6.637 6.03-8.188l.893 1.378c-3.335 1.804-3.987 4.145-4.247 5.621.537-.278 1.24-.375 1.929-.311 1.804.167 3.226 1.648 3.226 3.489a3.5 3.5 0 01-3.5 3.5c-1.073 0-2.099-.49-2.748-1.179z"/>
              </svg>
              <text class="quote-text">{{ quote.text }}</text>
              <text class="quote-author">— {{ quote.author }}</text>
            </view>
            <view class="quote-glow" />
          </view>
        </swiper-item>
      </swiper>

      <!-- 每日推荐 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">每日推荐</text>
        </view>
        <view class="featured-card" @click="goDetail(todayPick.id)">
          <view class="featured-cover" :style="{ backgroundColor: todayPick.coverColor || '#A34A2E' }">
            <image v-if="todayPick.cover" class="featured-cover-img" :src="todayPick.cover" mode="aspectFill" />
            <text v-else class="featured-cover-text">{{ todayPick.title?.charAt(0) }}</text>
            <view class="featured-rating">
              <text class="rating-star">★</text>
              <text>{{ todayPick.rating || '9.4' }}</text>
            </view>
          </view>
          <view class="featured-info">
            <view class="featured-meta">
              <text class="featured-title">{{ todayPick.title || '三体' }}</text>
              <text class="featured-author">{{ todayPick.author || '刘慈欣' }}</text>
            </view>
            <text class="featured-summary">{{ todayPick.summary || '文化大革命期间，红岸工程的叶文洁为了报复人类，向宇宙深处发出了地球文明的第一声啼鸣...' }}</text>
            <view class="featured-btn">
              <text>立即阅读</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 热门榜单 + 分类 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">热门榜单</text>
          <view class="section-more" @click="goStore">
            <text>更多</text>
            <svg viewBox="0 0 24 24" fill="none" stroke="#A34A2E" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
          </view>
        </view>
        <!-- 分类标签 -->
        <view class="category-scroll">
          <view class="category-list">
            <view
              class="category-tag"
              v-for="c in rankTabs"
              :key="c.id"
              :class="{ active: activeCategory === c.id }"
              @tap="selectCategory(c)"
            >
              {{ c.name }}
            </view>
          </view>
        </view>
        <swiper class="rank-swiper" :current="activeRankTabIndex" @change="onRankSwiperChange">
          <swiper-item v-for="(items, idx) in rankItemsByTab" :key="rankTabs[idx].id">
            <view class="rank-list">
              <view class="rank-item" v-for="(item, i) in items" :key="item.bookId || item.id" @click="goDetail(item.bookId || item.id)">
                <text class="rank-num" :class="{ top3: i < 3 }">{{ i + 1 }}</text>
                <view class="rank-cover" :style="{ backgroundColor: item.coverColor || '#A34A2E' }">
                  <image v-if="item.cover" class="rank-cover-img" :src="item.cover" mode="aspectFill" />
                  <text v-else class="rank-cover-text">{{ item.title.charAt(0) }}</text>
                </view>
                <view class="rank-info">
                  <text class="rank-title">{{ item.title }}</text>
                  <text class="rank-author">{{ item.author }}</text>
                </view>
                <view class="rank-score">
                  <text>{{ item.rating }}分</text>
                </view>
              </view>
              <view v-if="items.length === 0" class="rank-empty">
                <text>暂无该分类书籍</text>
              </view>
            </view>
          </swiper-item>
        </swiper>
      </view>

      <!-- 猜你喜欢 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">猜你喜欢</text>
          <view class="section-more" @click="shuffleGuessLike">
            <svg class="refresh-icon" viewBox="0 0 24 24" fill="none" stroke="#A34A2E" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.3"/>
            </svg>
            <text>换一换</text>
          </view>
        </view>
        <view class="guess-list">
          <view class="guess-card" v-for="b in displayedGuessLike" :key="b.id" @click="goDetail(b.id)">
            <view class="guess-card-cover" :style="{ backgroundColor: b.coverColor || '#A34A2E' }">
              <image v-if="b.cover" class="guess-card-cover-img" :src="b.cover" mode="aspectFill" />
              <text v-else class="guess-card-cover-text">{{ b.title.charAt(0) }}</text>
            </view>
            <view class="guess-card-body">
              <text class="guess-card-title">{{ b.title }}</text>
              <text class="guess-card-author">{{ b.author }}</text>
              <text class="guess-card-summary">{{ b.summary }}</text>
              <view class="guess-card-footer">
                <text class="guess-card-status" :class="b.status">{{ b.status === 'ongoing' ? '连载中' : '已完结' }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <Folio :num="1" />
    </view>
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { COVERS } from '@/utils/constants';
import { fetchBanners, fetchCategories, fetchTodayPick, fetchHotRank, fetchGuessLike, type Book, type RankItem } from '@/api/book';
import Folio from '@/components/Folio.vue';
import CustomTabBar from '@/components/CustomTabBar.vue';

const quotes = ref([
  { text: '在书页间，发现更多美好。', author: '今日推荐' },
  { text: '读书不是为了雄辩和驳斥，而是为了思考和权衡。', author: '培根' },
  { text: '书籍是人类进步的阶梯。', author: '高尔基' },
]);
const banners = ref<any[]>([]);
const categories = ref<any[]>([
  { id: 0, name: '全部' },
  { id: 1, name: '文学' },
  { id: 2, name: '科幻' },
  { id: 3, name: '心理' },
  { id: 4, name: '历史' },
  { id: 5, name: '悬疑' },
  { id: 6, name: '言情' },
]);
const todayPick = ref<Partial<Book>>({});
const hotRank = ref<RankItem[]>([]);
const guessLike = ref<Book[]>([]);
const guessOffset = ref(0);
const activeCategory = ref<number>(0);
const isLoading = ref(true);
const firstLoad = ref(true);

const enrichBook = (b: any) => ({ ...b, coverColor: COVERS[(b.id || 0) % COVERS.length] });

const rankTabs = computed(() => categories.value);

const activeRankTabIndex = computed(() => rankTabs.value.findIndex(c => c.id === activeCategory.value));

const rankItemsByTab = computed(() => {
  return rankTabs.value.map(tab => {
    if (tab.id === 0) return hotRank.value.slice(0, 5);
    return hotRank.value.filter(item => item.category === tab.name).slice(0, 5);
  });
});

const displayedGuessLike = computed(() => {
  const start = guessOffset.value;
  return guessLike.value.slice(start, start + 4);
});

function selectCategory(c: any) {
  activeCategory.value = c.id;
}

function onRankSwiperChange(e: any) {
  const idx = e.detail.current;
  if (rankTabs.value[idx]) {
    activeCategory.value = rankTabs.value[idx].id;
  }
}

function shuffleGuessLike() {
  const total = guessLike.value.length;
  if (total <= 4) return;
  guessOffset.value = (guessOffset.value + 4) % total;
}

onMounted(() => {
  isLoading.value = firstLoad.value;
  loadData();
});

onShow(() => {
  if (firstLoad.value) {
    isLoading.value = true;
    loadData();
  }
});

async function loadData() {
  try {
    const [b, c, t, r, g] = await Promise.all([
      fetchBanners().catch(() => []),
      fetchCategories().catch(() => []),
      fetchTodayPick().catch(() => []),
      fetchHotRank().catch(() => []),
      fetchGuessLike().catch(() => []),
    ]);
    banners.value = b;
    if (c.length) categories.value = [{ id: 0, name: '全部' }, ...c];
    todayPick.value = enrichBook((t || [])[0] || {});
    hotRank.value = (r || []).slice(0, 5).map(enrichBook);
    guessLike.value = (g || []).slice(0, 6).map(enrichBook);
    guessOffset.value = 0;
  } catch (e) {
    console.error('home load failed', e);
  } finally {
    isLoading.value = false;
    firstLoad.value = false;
  }
}

function goDetail(id: number) {
  if (!id) return;
  uni.navigateTo({ url: `/pages/detail/detail?id=${id}` });
}

function goSearch() {
  uni.navigateTo({ url: '/pages/search/search' });
}

function goCategory(c: any) {
  uni.navigateTo({ url: `/pages/category/category?id=${c.id}&name=${encodeURIComponent(c.name)}` });
}

function goStore() {
  uni.switchTab({ url: '/pages/store/store' });
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 0 32rpx 160rpx;
  background: #F8F4F0;
}

/* 骨架屏 */
.skeleton-page {
  padding: 24rpx 0;
}
.sk-search {
  height: 80rpx;
  background: #FFFFFF;
  border-radius: 48rpx;
  margin-bottom: 24rpx;
  animation: shimmer 1.5s infinite;
}
.sk-card {
  display: flex;
  gap: 24rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 40rpx;
}
.sk-cover-lg {
  width: 180rpx;
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
.sk-line.w50 { width: 50%; }
.sk-line.w30 { width: 30%; }
.sk-line.w80 { width: 80%; }
.sk-line.title { height: 32rpx; width: 40%; margin-bottom: 20rpx; }
.sk-btn-sm {
  height: 56rpx;
  width: 60%;
  background: #E8E2D8;
  border-radius: 48rpx;
  margin-top: 8rpx;
  animation: shimmer 1.5s infinite;
}
.sk-section { margin-bottom: 40rpx; }
.sk-rank {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 16rpx 24rpx;
}
.sk-rank-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F5F0EA;
}
.sk-num {
  width: 36rpx;
  height: 36rpx;
  background: #E8E2D8;
  border-radius: 50%;
  animation: shimmer 1.5s infinite;
}
.sk-cover-sm {
  width: 80rpx;
  aspect-ratio: 2/3;
  border-radius: 8rpx;
  background: #E8E2D8;
  animation: shimmer 1.5s infinite;
}
.sk-rank-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.sk-tags {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;
}
.sk-tag {
  width: 120rpx;
  height: 56rpx;
  background: #E8E2D8;
  border-radius: 48rpx;
  animation: shimmer 1.5s infinite;
}
.sk-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}
.sk-grid-item {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 20rpx;
}
.sk-cover-md {
  width: 100%;
  aspect-ratio: 3/4;
  border-radius: 12rpx;
  background: #E8E2D8;
  margin-bottom: 12rpx;
  animation: shimmer 1.5s infinite;
}
.sk-grid-text {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
@keyframes shimmer {
  0% { background: #E8E2D8; }
  50% { background: #F0EBE3; }
  100% { background: #E8E2D8; }
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: #FFFFFF;
  border-radius: 48rpx;
  padding: 20rpx 32rpx;
  margin: 24rpx 0;
  border: 1rpx solid rgba(163, 74, 46, 0.08);
  box-shadow: 0 2rpx 12rpx rgba(163, 74, 46, 0.04);
}
.search-icon-svg {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
}
.search-placeholder {
  font-size: 28rpx;
  color: #AAAAAA;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 引用卡片滑动 */
.quote-swiper {
  height: 280rpx;
  margin-bottom: 40rpx;
}
.quote-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 40rpx;
  height: 100%;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4rpx 24rpx rgba(163, 74, 46, 0.04);
  border: 1rpx solid rgba(163, 74, 46, 0.06);
}
.quote-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.quote-icon {
  width: 48rpx;
  height: 48rpx;
  opacity: 0.5;
}
.quote-text {
  font-size: 36rpx;
  font-weight: 600;
  color: #1C1C19;
  font-family: 'Noto Serif SC', serif;
  line-height: 1.4;
}
.quote-author {
  font-size: 24rpx;
  color: #55423D;
  font-family: 'Noto Sans SC', sans-serif;
}
.quote-glow {
  position: absolute;
  right: -40rpx;
  bottom: -40rpx;
  width: 200rpx;
  height: 200rpx;
  background: rgba(163, 74, 46, 0.04);
  border-radius: 50%;
  filter: blur(40rpx);
}

/* 通用区块 */
.section {
  margin-bottom: 48rpx;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24rpx;
}
.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1C1C19;
  font-family: 'Noto Serif SC', serif;
}
.section-more {
  display: flex;
  align-items: center;
  gap: 4rpx;
}
.section-more text {
  font-size: 24rpx;
  color: #A34A2E;
  font-family: 'Noto Sans SC', sans-serif;
}
.section-more svg {
  width: 24rpx;
  height: 24rpx;
}

/* 每日推荐 */
.featured-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx;
  display: flex;
  gap: 24rpx;
  align-items: flex-start;
  box-shadow: 0 4rpx 24rpx rgba(163, 74, 46, 0.04);
  border: 1rpx solid rgba(163, 74, 46, 0.06);
}
.featured-cover {
  width: 180rpx;
  aspect-ratio: 2 / 3;
  border-radius: 16rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.1);
}
.featured-cover-img {
  width: 100%;
  height: 100%;
}
.featured-cover-text {
  font-size: 56rpx;
  color: #fff;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
}
.featured-rating {
  position: absolute;
  bottom: 8rpx;
  left: 8rpx;
  background: rgba(0,0,0,0.6);
  backdrop-filter: blur(8rpx);
  padding: 2rpx 10rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  gap: 4rpx;
}
.featured-rating text {
  font-size: 18rpx;
  color: #FFD700;
  font-family: 'Noto Serif SC', serif;
}
.rating-star {
  color: #FFD700;
  font-size: 18rpx;
}
.featured-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  overflow: hidden;
  padding-top: 4rpx;
}
.featured-meta {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}
.featured-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1C1C19;
  font-family: 'Noto Serif SC', serif;
  line-height: 1.2;
}
.featured-author {
  font-size: 24rpx;
  color: #55423D;
  font-family: 'Noto Sans SC', sans-serif;
}
.featured-summary {
  font-size: 24rpx;
  color: #645D55;
  font-family: 'Noto Serif SC', serif;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.featured-btn {
  background: #A34A2E;
  border-radius: 48rpx;
  padding: 16rpx 0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 4rpx;
  box-shadow: 0 4rpx 16rpx rgba(163, 74, 46, 0.2);
}
.featured-btn text {
  font-size: 26rpx;
  color: #FFFFFF;
  font-weight: 600;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 热门榜单 */
.rank-swiper {
  height: 800rpx;
}
.rank-list {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 16rpx 24rpx;
  box-shadow: 0 4rpx 24rpx rgba(163, 74, 46, 0.04);
  border: 1rpx solid rgba(163, 74, 46, 0.06);
}
@keyframes slideFadeIn {
  from {
    opacity: 0;
    transform: translateX(30rpx);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
.rank-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F5F0EA;
}
.rank-item:last-child {
  border-bottom: none;
}
.rank-num {
  width: 36rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 700;
  color: #AAAAAA;
  font-family: 'Noto Serif SC', serif;
}
.rank-num.top3 {
  color: #A34A2E;
}
.rank-cover {
  width: 80rpx;
  aspect-ratio: 2 / 3;
  border-radius: 8rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.rank-cover-img {
  width: 100%;
  height: 100%;
}
.rank-cover-text {
  font-size: 28rpx;
  color: #fff;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
}
.rank-info {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}
.rank-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #1C1C19;
  font-family: 'Noto Sans SC', sans-serif;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rank-author {
  font-size: 22rpx;
  color: #55423D;
  font-family: 'Noto Sans SC', sans-serif;
}
.rank-score {
  background: rgba(163, 74, 46, 0.08);
  padding: 4rpx 12rpx;
  border-radius: 24rpx;
}
.rank-score text {
  font-size: 22rpx;
  color: #A34A2E;
  font-weight: 600;
  font-family: 'Noto Serif SC', serif;
}
.rank-empty {
  padding: 48rpx 0;
  text-align: center;
}
.rank-empty text {
  font-size: 26rpx;
  color: #AAAAAA;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 分类标签 - 横向滚动 */
.category-scroll {
  margin-bottom: 20rpx;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}
.category-scroll::-webkit-scrollbar {
  display: none;
}
.category-list {
  display: inline-flex;
  gap: 16rpx;
  padding: 0 4rpx;
  white-space: nowrap;
  min-width: 100%;
}
.category-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12rpx 28rpx;
  border-radius: 48rpx;
  background: #FFFFFF;
  border: 1rpx solid rgba(163, 74, 46, 0.1);
  box-shadow: 0 2rpx 8rpx rgba(163, 74, 46, 0.04);
  flex-shrink: 0;
  transition: all 0.2s ease;
  font-size: 26rpx;
  color: #1C1C19;
  font-family: 'Noto Sans SC', sans-serif;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}
.category-tag.active {
  background: #A34A2E;
  border-color: #A34A2E;
  color: #FFFFFF;
}

/* 猜你喜欢 */
.refresh-icon {
  width: 24rpx;
  height: 24rpx;
}
.guess-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.guess-card {
  display: flex;
  gap: 20rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
  border: 1rpx solid rgba(163, 74, 46, 0.06);
}
.guess-card-cover {
  width: 140rpx;
  aspect-ratio: 2 / 3;
  border-radius: 12rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #F1EDE9;
}
.guess-card-cover-img {
  width: 100%;
  height: 100%;
}
.guess-card-cover-text {
  font-size: 48rpx;
  color: #fff;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
}
.guess-card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  overflow: hidden;
  min-width: 0;
}
.guess-card-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1C1C19;
  font-family: 'Noto Sans SC', sans-serif;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.guess-card-author {
  font-size: 22rpx;
  color: #55423D;
  font-family: 'Noto Sans SC', sans-serif;
}
.guess-card-summary {
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
.guess-card-footer {
  margin-top: auto;
  padding-top: 4rpx;
}
.guess-card-status {
  display: inline-block;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-family: 'Noto Sans SC', sans-serif;
}
.guess-card-status.ongoing {
  background: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}
.guess-card-status.completed {
  background: rgba(163, 74, 46, 0.1);
  color: #A34A2E;
}
</style>
