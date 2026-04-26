<template>
  <view class="reader-page" :class="{ 'dark-mode': isDark }" @click="toggleMenu">
    <!-- 顶部栏 -->
    <view class="reader-top" v-if="showMenu" @click.stop>
      <text class="back-btn" @click="goBack">←</text>
      <text class="chapter-title">{{ chapterTitle }}</text>
      <text class="menu-btn" @click="showCatalog = true">☰</text>
    </view>

    <!-- 内容区 -->
    <scroll-view class="content-scroll" scroll-y :scroll-top="scrollTop" @scroll="onScroll">
      <view class="chapter-content">
        <text class="chapter-heading">{{ chapterTitle }}</text>
        <view class="paragraph" v-for="(p, i) in paragraphs" :key="i">
          <text :style="textStyle">{{ p }}</text>
        </view>
      </view>

      <!-- 章节底部操作 -->
      <view class="chapter-footer">
        <view class="footer-btn" @click.stop="prevChapter" v-if="hasPrev">
          <text>← 上一章</text>
        </view>
        <view class="footer-btn primary" @click.stop="nextChapter" v-if="hasNext">
          <text>下一章 →</text>
        </view>
      </view>

      <view style="height: 100rpx" />
    </scroll-view>

    <!-- 底部栏 -->
    <view class="reader-bottom" v-if="showMenu" @click.stop>
      <view class="bottom-item" @click="showSettings = true">
        <text class="bottom-icon">Aa</text>
        <text class="bottom-label">设置</text>
      </view>
      <view class="bottom-item" @click="showCatalog = true">
        <text class="bottom-icon">☰</text>
        <text class="bottom-label">目录</text>
      </view>
      <view class="bottom-item" @click="toggleDark">
        <text class="bottom-icon">{{ isDark ? '☀' : '☾' }}</text>
        <text class="bottom-label">{{ isDark ? '日间' : '夜间' }}</text>
      </view>
    </view>

    <!-- 目录弹窗 -->
    <view class="catalog-mask" v-if="showCatalog" @click="showCatalog = false">
      <view class="catalog-panel" @click.stop>
        <view class="catalog-header">
          <text class="catalog-title">章节目录</text>
          <text class="catalog-close" @click="showCatalog = false">✕</text>
        </view>
        <scroll-view class="catalog-list" scroll-y>
          <view
            class="catalog-item"
            v-for="ch in chapters"
            :key="ch.id"
            :class="{ active: ch.id === chapterId, locked: !ch.isFree && !isUnlocked(ch.id) }"
            @click="selectChapter(ch)"
          >
            <text class="catalog-num">{{ ch.order }}</text>
            <text class="catalog-name">{{ ch.title }}</text>
            <text class="catalog-lock" v-if="!ch.isFree && !isUnlocked(ch.id)">🔒</text>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 设置弹窗 -->
    <view class="settings-mask" v-if="showSettings" @click="showSettings = false">
      <view class="settings-panel" @click.stop>
        <view class="settings-header">
          <text class="settings-title">阅读设置</text>
          <text class="settings-close" @click="showSettings = false">✕</text>
        </view>
        <view class="settings-body">
          <view class="setting-row">
            <text class="setting-label">字体大小</text>
            <view class="font-size-btns">
              <text class="size-btn" :class="{ active: fontSize === 28 }" @click="fontSize = 28">小</text>
              <text class="size-btn" :class="{ active: fontSize === 32 }" @click="fontSize = 32">中</text>
              <text class="size-btn" :class="{ active: fontSize === 36 }" @click="fontSize = 36">大</text>
            </view>
          </view>
          <view class="setting-row">
            <text class="setting-label">行间距</text>
            <view class="font-size-btns">
              <text class="size-btn" :class="{ active: lineHeight === 1.6 }" @click="lineHeight = 1.6">紧凑</text>
              <text class="size-btn" :class="{ active: lineHeight === 1.8 }" @click="lineHeight = 1.8">适中</text>
              <text class="size-btn" :class="{ active: lineHeight === 2.0 }" @click="lineHeight = 2.0">宽松</text>
            </view>
          </view>
          <view class="setting-row">
            <text class="setting-label">背景</text>
            <view class="bg-options">
              <view class="bg-option" :class="{ active: bgType === 'paper' }" style="background: #F5F0EA" @click="bgType = 'paper'" />
              <view class="bg-option" :class="{ active: bgType === 'white' }" style="background: #FFFFFF" @click="bgType = 'white'" />
              <view class="bg-option" :class="{ active: bgType === 'green' }" style="background: #E8F5E9" @click="bgType = 'green'" />
              <view class="bg-option" :class="{ active: bgType === 'dark' }" style="background: #1A1A2E" @click="bgType = 'dark'" />
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 广告解锁弹窗 -->
    <view class="ad-mask" v-if="showAdUnlock" @click.stop>
      <view class="ad-panel">
        <text class="ad-title">本章需解锁</text>
        <text class="ad-sub">观看广告即可免费阅读本章</text>
        <view class="ad-btn" @click="watchAd">
          <text>🎬 看广告解锁</text>
        </view>
        <view class="ad-btn outline" @click="buyChapter">
          <text>💰 {{ chapterPrice }}金币购买</text>
        </view>
        <text class="ad-close" @click="showAdUnlock = false">暂不阅读</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchChapters, fetchChapterContent, saveProgress, checkUnlock, unlockByAd, buyChapter as apiBuyChapter, type Chapter } from '@/api/book';

const bookId = ref(0);
const chapterId = ref(0);
const chapters = ref<Chapter[]>([]);
const chapterTitle = ref('');
const paragraphs = ref<string[]>([]);
const isLoading = ref(false);
const showMenu = ref(false);
const showCatalog = ref(false);
const showSettings = ref(false);
const showAdUnlock = ref(false);
const scrollTop = ref(0);
const unlockedChapters = ref<number[]>([]);

// 阅读设置
const fontSize = ref(32);
const lineHeight = ref(1.8);
const bgType = ref('paper');
const isDark = computed(() => bgType.value === 'dark');

const textStyle = computed(() => ({
  fontSize: fontSize.value + 'rpx',
  lineHeight: lineHeight.value,
  color: isDark.value ? '#CCCCCC' : '#2C2C2C',
}));

const bgStyle = computed(() => {
  const map: Record<string, string> = {
    paper: '#F5F0EA',
    white: '#FFFFFF',
    green: '#E8F5E9',
    dark: '#1A1A2E',
  };
  return map[bgType.value] || '#F5F0EA';
});

const hasPrev = computed(() => {
  const idx = chapters.value.findIndex(c => c.id === chapterId.value);
  return idx > 0;
});

const hasNext = computed(() => {
  const idx = chapters.value.findIndex(c => c.id === chapterId.value);
  return idx < chapters.value.length - 1;
});

const chapterPrice = computed(() => {
  const ch = chapters.value.find(c => c.id === chapterId.value);
  return ch?.price || 10;
});

onLoad(async (opts: any) => {
  bookId.value = parseInt(opts?.bookId || '1');
  chapterId.value = parseInt(opts?.chapterId || '1');
  await loadChapters();
  await loadContent();
});

async function loadChapters() {
  try {
    const list = await fetchChapters(bookId.value);
    chapters.value = list || [];
  } catch (e) {
    console.error('load chapters failed', e);
  }
}

async function loadContent() {
  isLoading.value = true;
  try {
    const ch = chapters.value.find(c => c.id === chapterId.value);
    if (!ch) return;

    // 检查是否需要解锁
    if (!ch.isFree && !isUnlocked(ch.id)) {
      showAdUnlock.value = true;
      isLoading.value = false;
      return;
    }

    const data = await fetchChapterContent(bookId.value, chapterId.value);
    chapterTitle.value = ch.title;
    paragraphs.value = (data?.content || '').split('\n').filter((p: string) => p.trim());
    scrollTop.value = 0;

    // 保存进度
    saveProgress(bookId.value, chapterId.value, 0).catch(() => {});
  } catch (e) {
    console.error('load content failed', e);
  } finally {
    isLoading.value = false;
  }
}

function isUnlocked(cid: number) {
  return unlockedChapters.value.includes(cid);
}

function toggleMenu() {
  showMenu.value = !showMenu.value;
}

function toggleDark() {
  bgType.value = isDark.value ? 'paper' : 'dark';
}

function goBack() {
  uni.navigateBack();
}

async function prevChapter() {
  const idx = chapters.value.findIndex(c => c.id === chapterId.value);
  if (idx > 0) {
    chapterId.value = chapters.value[idx - 1].id;
    await loadContent();
  }
}

async function nextChapter() {
  const idx = chapters.value.findIndex(c => c.id === chapterId.value);
  if (idx < chapters.value.length - 1) {
    chapterId.value = chapters.value[idx + 1].id;
    await loadContent();
  }
}

async function selectChapter(ch: Chapter) {
  showCatalog.value = false;
  chapterId.value = ch.id;
  await loadContent();
}

function onScroll(e: any) {
  // 可在此保存阅读位置
}

// 广告解锁
async function watchAd() {
  // 模拟广告观看
  uni.showLoading({ title: '加载广告...' });
  setTimeout(async () => {
    uni.hideLoading();
    try {
      // 实际接入广告SDK时替换此处
      const adToken = 'mock_ad_token_' + Date.now();
      await unlockByAd(bookId.value, chapterId.value, adToken);
      unlockedChapters.value.push(chapterId.value);
      showAdUnlock.value = false;
      uni.showToast({ title: '解锁成功', icon: 'none' });
      await loadContent();
    } catch (e) {
      uni.showToast({ title: '解锁失败', icon: 'none' });
    }
  }, 2000);
}

// 金币购买
async function buyChapter() {
  try {
    await apiBuyChapter(bookId.value, chapterId.value);
    unlockedChapters.value.push(chapterId.value);
    showAdUnlock.value = false;
    uni.showToast({ title: '购买成功', icon: 'none' });
    await loadContent();
  } catch (e: any) {
    const msg = String(e?.message || e || '');
    if (msg.includes('余额') || msg.includes('insufficient')) {
      uni.showModal({
        title: '余额不足',
        content: '是否前往充值？',
        success: (res: any) => {
          if (res.confirm) uni.navigateTo({ url: '/pages/recharge/recharge' });
        }
      });
    } else {
      uni.showToast({ title: '购买失败', icon: 'none' });
    }
  }
}
</script>

<style scoped>
.reader-page {
  min-height: 100vh;
  background: #F5F0EA;
  position: relative;
}
.reader-page.dark-mode {
  background: #1A1A2E;
}

/* 顶部栏 */
.reader-top {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  background: rgba(245, 240, 234, 0.95);
  backdrop-filter: blur(10rpx);
  z-index: 50;
  border-bottom: 1rpx solid #E8E2D8;
}
.dark-mode .reader-top {
  background: rgba(26, 26, 46, 0.95);
  border-bottom-color: #333;
}
.back-btn, .menu-btn {
  font-size: 36rpx;
  color: #2C2C2C;
  padding: 8rpx;
}
.dark-mode .back-btn, .dark-mode .menu-btn {
  color: #CCCCCC;
}
.chapter-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #2C2C2C;
  font-family: 'Noto Sans SC', sans-serif;
}
.dark-mode .chapter-title {
  color: #CCCCCC;
}

/* 内容区 */
.content-scroll {
  height: 100vh;
  padding: 100rpx 32rpx 120rpx;
}
.chapter-content {
  padding-bottom: 40rpx;
}
.chapter-heading {
  font-size: 40rpx;
  font-weight: 700;
  color: #2C2C2C;
  font-family: 'Noto Serif SC', serif;
  margin-bottom: 40rpx;
  display: block;
}
.dark-mode .chapter-heading {
  color: #EEEEEE;
}
.paragraph {
  margin-bottom: 24rpx;
  text-indent: 2em;
}
.paragraph text {
  font-family: 'Noto Serif SC', serif;
}

/* 章节底部 */
.chapter-footer {
  display: flex;
  justify-content: space-between;
  padding: 40rpx 0;
  border-top: 1rpx solid #E8E2D8;
}
.dark-mode .chapter-footer {
  border-top-color: #333;
}
.footer-btn {
  padding: 16rpx 32rpx;
  border-radius: 48rpx;
  background: #FFFFFF;
  border: 1rpx solid #E8E2D8;
}
.dark-mode .footer-btn {
  background: #2A2A3E;
  border-color: #444;
}
.footer-btn text {
  font-size: 26rpx;
  color: #666666;
  font-family: 'Noto Sans SC', sans-serif;
}
.footer-btn.primary {
  background: #E8A23E;
  border-color: #E8A23E;
}
.footer-btn.primary text {
  color: #fff;
}

/* 底部栏 */
.reader-bottom {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 16rpx 0;
  background: rgba(245, 240, 234, 0.95);
  backdrop-filter: blur(10rpx);
  z-index: 50;
  border-top: 1rpx solid #E8E2D8;
}
.dark-mode .reader-bottom {
  background: rgba(26, 26, 46, 0.95);
  border-top-color: #333;
}
.bottom-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  padding: 8rpx 24rpx;
}
.bottom-icon {
  font-size: 28rpx;
  color: #666666;
}
.dark-mode .bottom-icon {
  color: #AAAAAA;
}
.bottom-label {
  font-size: 20rpx;
  color: #888888;
  font-family: 'Noto Sans SC', sans-serif;
}
.dark-mode .bottom-label {
  color: #888888;
}

/* 目录弹窗 */
.catalog-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 100;
}
.catalog-panel {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 80%;
  background: #FFFFFF;
  display: flex;
  flex-direction: column;
}
.dark-mode .catalog-panel {
  background: #1A1A2E;
}
.catalog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #E8E2D8;
}
.catalog-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #2C2C2C;
  font-family: 'Noto Serif SC', serif;
}
.catalog-close {
  font-size: 32rpx;
  color: #888888;
  padding: 8rpx;
}
.catalog-list {
  flex: 1;
  padding: 16rpx 0;
}
.catalog-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 32rpx;
  border-bottom: 1rpx solid #F5F0EA;
}
.catalog-item.active {
  background: rgba(232,162,62,0.1);
}
.catalog-item.locked {
  opacity: 0.5;
}
.catalog-num {
  font-size: 24rpx;
  color: #888888;
  width: 48rpx;
  font-family: 'Noto Serif SC', serif;
}
.catalog-name {
  flex: 1;
  font-size: 28rpx;
  color: #2C2C2C;
  font-family: 'Noto Sans SC', sans-serif;
}
.catalog-lock {
  font-size: 24rpx;
}

/* 设置弹窗 */
.settings-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 100;
  display: flex;
  align-items: flex-end;
}
.settings-panel {
  width: 100%;
  background: #FFFFFF;
  border-radius: 24rpx 24rpx 0 0;
  padding: 24rpx 32rpx 40rpx;
}
.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}
.settings-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #2C2C2C;
  font-family: 'Noto Serif SC', serif;
}
.settings-close {
  font-size: 32rpx;
  color: #888888;
}
.setting-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F5F0EA;
}
.setting-label {
  font-size: 28rpx;
  color: #2C2C2C;
  font-family: 'Noto Sans SC', sans-serif;
}
.font-size-btns {
  display: flex;
  gap: 12rpx;
}
.size-btn {
  padding: 8rpx 24rpx;
  border-radius: 48rpx;
  background: #F5F0EA;
  font-size: 24rpx;
  color: #666666;
  font-family: 'Noto Sans SC', sans-serif;
}
.size-btn.active {
  background: #E8A23E;
  color: #fff;
}
.bg-options {
  display: flex;
  gap: 16rpx;
}
.bg-option {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  border: 2rpx solid transparent;
}
.bg-option.active {
  border-color: #E8A23E;
}

/* 广告解锁弹窗 */
.ad-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.6);
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ad-panel {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 48rpx 40rpx;
  width: 80%;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.ad-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #2C2C2C;
  font-family: 'Noto Serif SC', serif;
  margin-bottom: 12rpx;
}
.ad-sub {
  font-size: 26rpx;
  color: #888888;
  font-family: 'Noto Sans SC', sans-serif;
  margin-bottom: 32rpx;
}
.ad-btn {
  width: 100%;
  padding: 24rpx 0;
  border-radius: 48rpx;
  background: #E8A23E;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}
.ad-btn.outline {
  background: #FFFFFF;
  border: 1rpx solid #E8E2D8;
}
.ad-btn text {
  font-size: 28rpx;
  color: #fff;
  font-weight: 600;
  font-family: 'Noto Sans SC', sans-serif;
}
.ad-btn.outline text {
  color: #2C2C2C;
}
.ad-close {
  font-size: 26rpx;
  color: #888888;
  font-family: 'Noto Sans SC', sans-serif;
  margin-top: 16rpx;
}
</style>