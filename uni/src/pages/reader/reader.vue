<template>
  <view class="reader-page" :class="{ 'dark-mode': isDark }" :style="pageStyle">
    <!-- 顶部栏 -->
    <view class="reader-top" v-if="showMenu" @click.stop>
      <text class="back-btn" @click="goBack">←</text>
      <text class="chapter-title">{{ currentTitle }}</text>
      <text class="menu-btn" @click="showCatalog = true">☰</text>
    </view>

    <!-- 内容区 -->
    <view
      id="reader-content"
      class="content-scroll"
      @scroll="onContentScroll"
      :style="scrollStyle"
    >
      <view v-if="loadingPrev" class="loading-bar">
        <text>正在加载上一章...</text>
      </view>

      <view
        v-for="section in sections"
        :key="section.chapterId"
        class="section-block"
        :id="'ch-' + section.chapterId"
      >
        <text class="chapter-heading" :style="headingStyle">{{ section.title }}</text>
        <view
          class="paragraph"
          v-for="(p, i) in section.paragraphs"
          :key="i"
          :style="paragraphWrapStyle"
        >
          <text :style="textStyle">{{ p }}</text>
        </view>
      </view>

      <view v-if="loadingNext" class="loading-bar">
        <text>正在加载下一章...</text>
      </view>

      <view style="height: 200rpx" />
    </view>

    <!-- 点击区域 -->
    <view class="tap-overlay" v-if="tapOverlayVisible">
      <view class="tap-zone tap-left" @click="onTapLeft" />
      <view class="tap-zone tap-center" @click="onTapCenter" />
      <view class="tap-zone tap-right" @click="onTapRight" />
    </view>

    <!-- 底部栏 -->
    <view class="reader-bottom" v-if="showMenu" @click.stop>
      <view class="bottom-item" @click="openSettings">
        <text class="bottom-icon">Aa</text>
        <text class="bottom-label">设置</text>
      </view>
      <view class="bottom-item" @click="showCatalog = true">
        <text class="bottom-icon">☰</text>
        <text class="bottom-label">目录</text>
      </view>
      <view class="bottom-item" @click="toggleDarkQuick">
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

    <!-- 全屏设置面板 -->
    <view class="settings-fullscreen" v-if="showSettingsPanel" @click.stop :class="{ 'dark-mode': isDark }" :style="settingsPanelStyle">
      <view class="settings-header">
        <text class="settings-title" :class="{ 'dark-text': isDark }">阅读设置</text>
        <text class="settings-close-btn" :class="{ 'dark-text': isDark }" @click="showSettingsPanel = false">✕</text>
      </view>

      <view class="settings-tabs">
        <view
          class="settings-tab"
          v-for="tab in settingsTabs"
          :key="tab.key"
          :class="{ active: activeSettingsTab === tab.key, 'dark-mode': isDark }"
          @click="activeSettingsTab = tab.key"
        >
          <text :class="{ 'dark-text': isDark }">{{ tab.label }}</text>
        </view>
      </view>

      <scroll-view class="settings-body-scroll" scroll-y>
        <!-- 显示 Tab -->
        <view v-if="activeSettingsTab === 'display'" class="settings-body">
          <view class="setting-group">
            <text class="setting-group-title">字号大小</text>
            <view class="font-size-control">
              <text class="size-btn" @click="adjustFontSize(-1)">A-</text>
              <text class="size-value">{{ config.fontSize }}</text>
              <text class="size-btn" @click="adjustFontSize(1)">A+</text>
            </view>
          </view>

          <view class="setting-group">
            <text class="setting-group-title">行距</text>
            <view class="btn-group">
              <text
                v-for="h in lineHeightOptions"
                :key="h"
                class="group-btn"
                :class="{ active: config.lineHeight === h }"
                @click="setLineHeight(h)"
              >{{ h === 140 ? '紧凑' : h === 160 ? '标准' : h === 180 ? '宽松' : '极宽' }}</text>
            </view>
          </view>

          <view class="setting-group">
            <text class="setting-group-title">段距</text>
            <view class="btn-group">
              <text
                v-for="s in paragraphSpacingOptions"
                :key="s"
                class="group-btn"
                :class="{ active: config.paragraphSpacing === s }"
                @click="setParagraphSpacing(s)"
              >{{ s }}rpx</text>
            </view>
          </view>

          <view class="setting-group">
            <text class="setting-group-title">字体</text>
            <view class="btn-group">
              <text
                v-for="f in fontFamilyOptions"
                :key="f.value"
                class="group-btn"
                :class="{ active: config.fontFamily === f.value }"
                @click="setFontFamily(f.value)"
              >{{ f.label }}</text>
            </view>
          </view>
        </view>

        <!-- 主题 Tab -->
        <view v-if="activeSettingsTab === 'theme'" class="settings-body">
          <view class="setting-group">
            <text class="setting-group-title">背景主题</text>
            <view class="theme-options">
              <view
                class="theme-option"
                v-for="t in themeOptions"
                :key="t.key"
                :class="{ active: config.theme === t.key }"
                :style="{ background: t.bg }"
                @click="setTheme(t.key)"
              >
                <text :style="{ color: t.text }">{{ t.label }}</text>
              </view>
            </view>
          </view>

          <view class="setting-group">
            <text class="setting-group-title">屏幕亮度 {{ config.brightness }}%</text>
            <view class="brightness-control">
              <text class="brightness-btn" @click="adjustBrightness(-10)">-</text>
              <view class="brightness-bar">
                <view class="brightness-fill" :style="{ width: config.brightness + '%' }" />
              </view>
              <text class="brightness-btn" @click="adjustBrightness(10)">+</text>
            </view>
          </view>
        </view>

        <!-- 翻页 Tab -->
        <view v-if="activeSettingsTab === 'paging'" class="settings-body">
          <view class="setting-group">
            <text class="setting-group-title">翻页方式</text>
            <view class="btn-group">
              <text
                class="group-btn"
                :class="{ active: config.pagingMode === 'scroll' }"
                @click="setPagingMode('scroll')"
              >滚动阅读</text>
              <text
                class="group-btn"
                :class="{ active: config.pagingMode === 'tap' }"
                @click="setPagingMode('tap')"
              >点击翻页</text>
            </view>
          </view>
          <view class="setting-hint" v-if="config.pagingMode === 'tap'">
            <text>点击屏幕左侧翻上页，右侧翻下页，中间呼出菜单</text>
          </view>
        </view>
      </scroll-view>
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
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchChapters, fetchChapterContent, saveProgress, checkUnlock, unlockByAd, buyChapter as apiBuyChapter, type Chapter } from '@/api/book';
import { fetchReaderConfig, saveReaderConfig } from '@/api/user';

const bookId = ref(0);
const chapterId = ref(0);
const chapters = ref<Chapter[]>([]);
const sections = ref<Array<{ chapterId: number; title: string; paragraphs: string[] }>>([]);
const isLoading = ref(false);
const loadingNext = ref(false);
const loadingPrev = ref(false);
const showMenu = ref(false);
const showCatalog = ref(false);
const showSettingsPanel = ref(false);
const showAdUnlock = ref(false);
const currentScrollTop = ref(0);
const activeSettingsTab = ref('display');
const unlockedChapters = ref<number[]>([]);

// 阅读配置
const config = ref({
  fontSize: 18,
  lineHeight: 160,
  theme: 'light',
  brightness: 100,
  paragraphSpacing: 24,
  fontFamily: 'serif',
  pagingMode: 'scroll',
});

const isDark = computed(() => config.value.theme === 'dark');

const pageStyle = computed(() => {
  const map: Record<string, string> = {
    light: '#F5F0EA',
    white: '#FFFFFF',
    green: '#E8F5E9',
    dark: '#1A1A2E',
  };
  return { backgroundColor: map[config.value.theme] || '#F5F0EA' };
});

const settingsPanelStyle = computed(() => {
  const map: Record<string, string> = {
    light: '#F5F0EA',
    white: '#FFFFFF',
    green: '#E8F5E9',
    dark: '#1A1A2E',
  };
  return { backgroundColor: map[config.value.theme] || '#F5F0EA' };
});

const scrollStyle = computed(() => {
  return { filter: `brightness(${config.value.brightness}%)` };
});

const textStyle = computed(() => {
  const fontMap: Record<string, string> = {
    serif: "'Noto Serif SC', serif",
    sans: "'Noto Sans SC', sans-serif",
  };
  return {
    fontSize: config.value.fontSize * 2 + 'rpx',
    lineHeight: config.value.lineHeight / 100,
    color: isDark.value ? '#CCCCCC' : '#2C2C2C',
    fontFamily: fontMap[config.value.fontFamily] || fontMap.serif,
  };
});

const headingStyle = computed(() => ({
  color: isDark.value ? '#EEEEEE' : '#2C2C2C',
  fontFamily: config.value.fontFamily === 'serif' ? "'Noto Serif SC', serif" : "'Noto Sans SC', sans-serif",
}));

const paragraphWrapStyle = computed(() => ({
  marginBottom: config.value.paragraphSpacing + 'rpx',
}));

const currentTitle = computed(() => {
  const ch = chapters.value.find(c => c.id === chapterId.value);
  return ch?.title || '';
});

const tapOverlayVisible = computed(() => {
  return !showMenu.value && !showSettingsPanel.value && !showCatalog.value && !showAdUnlock.value;
});

const chapterPrice = computed(() => {
  const ch = chapters.value.find(c => c.id === chapterId.value);
  return ch?.price || 10;
});

// 选项常量
const settingsTabs = [
  { key: 'display', label: '显示' },
  { key: 'theme', label: '主题' },
  { key: 'paging', label: '翻页' },
];
const lineHeightOptions = [140, 160, 180, 200];
const paragraphSpacingOptions = [0, 12, 24, 36];
const fontFamilyOptions = [
  { value: 'serif', label: '宋体' },
  { value: 'sans', label: '黑体' },
];
const themeOptions = [
  { key: 'light', label: '羊皮纸', bg: '#F5F0EA', text: '#2C2C2C' },
  { key: 'white', label: '纯白', bg: '#FFFFFF', text: '#2C2C2C' },
  { key: 'green', label: '护眼', bg: '#E8F5E9', text: '#2C2C2C' },
  { key: 'dark', label: '夜间', bg: '#1A1A2E', text: '#CCCCCC' },
];

onLoad(async (opts: any) => {
  bookId.value = parseInt(opts?.bookId || '1');
  chapterId.value = parseInt(opts?.chapterId || '1');
  await loadConfig();
  await loadChapters();
  await loadCurrentChapter();
});

// 加载配置
async function loadConfig() {
  try {
    const data = await fetchReaderConfig();
    if (data) {
      config.value = { ...config.value, ...data };
    }
  } catch (e) {
    const local = uni.getStorageSync('reader_config');
    if (local) {
      try {
        const parsed = JSON.parse(local);
        config.value = { ...config.value, ...parsed };
      } catch {}
    }
  }
}

// 保存配置（防抖）
let saveTimer: ReturnType<typeof setTimeout> | null = null;
function debouncedSaveConfig() {
  if (saveTimer) clearTimeout(saveTimer);
  saveTimer = setTimeout(() => {
    const token = uni.getStorageSync('token');
    if (token) {
      saveReaderConfig({
        fontSize: config.value.fontSize,
        lineHeight: config.value.lineHeight,
        theme: config.value.theme,
        brightness: config.value.brightness,
        paragraphSpacing: config.value.paragraphSpacing,
        fontFamily: config.value.fontFamily,
        pagingMode: config.value.pagingMode,
      }).catch(() => {});
    }
    uni.setStorageSync('reader_config', JSON.stringify(config.value));
  }, 800);
}

async function loadChapters() {
  try {
    const list = await fetchChapters(bookId.value);
    chapters.value = list || [];
  } catch (e) {
    console.error('load chapters failed', e);
  }
}

async function loadCurrentChapter() {
  isLoading.value = true;
  try {
    const ch = chapters.value.find(c => c.id === chapterId.value);
    if (!ch) return;

    if (!ch.isFree && !isUnlocked(ch.id)) {
      showAdUnlock.value = true;
      isLoading.value = false;
      return;
    }

    const data = await fetchChapterContent(bookId.value, chapterId.value);
    sections.value = [{
      chapterId: ch.id,
      title: data?.title || ch.title,
      paragraphs: (data?.content || '').split('\n').filter((p: string) => p.trim()),
    }];
    const el = getScrollEl();
    if (el) el.scrollTop = 0;
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

function getNextChapter() {
  const idx = chapters.value.findIndex(c => c.id === chapterId.value);
  return idx >= 0 && idx < chapters.value.length - 1 ? chapters.value[idx + 1] : null;
}

function getPrevChapter() {
  const idx = chapters.value.findIndex(c => c.id === chapterId.value);
  return idx > 0 ? chapters.value[idx - 1] : null;
}

function getScrollEl() {
  return document.getElementById('reader-content');
}

function onContentScroll(e: any) {
  const el = e.target;
  currentScrollTop.value = el.scrollTop;

  if (config.value.pagingMode !== 'scroll') return;

  // 接近底部，自动加载下一章
  if (!loadingNext.value) {
    const distanceToBottom = el.scrollHeight - el.scrollTop - el.clientHeight;
    if (distanceToBottom < 500) {
      autoLoadNext();
    }
  }

  // 接近顶部，自动加载上一章
  if (!loadingPrev.value && el.scrollTop < 100) {
    autoLoadPrev();
  }
}

async function autoLoadNext() {
  const nextCh = getNextChapter();
  if (!nextCh) return;
  if (sections.value.some(s => s.chapterId === nextCh.id)) return;
  if (!nextCh.isFree && !isUnlocked(nextCh.id)) return;

  loadingNext.value = true;
  try {
    const data = await fetchChapterContent(bookId.value, nextCh.id);
    sections.value.push({
      chapterId: nextCh.id,
      title: data?.title || nextCh.title,
      paragraphs: (data?.content || '').split('\n').filter((p: string) => p.trim()),
    });
    chapterId.value = nextCh.id;
    saveProgress(bookId.value, nextCh.id, 0).catch(() => {});
  } catch (e) {
    console.error('auto load next failed', e);
  } finally {
    loadingNext.value = false;
  }
}

async function autoLoadPrev() {
  const prevCh = getPrevChapter();
  if (!prevCh) return;
  if (sections.value.some(s => s.chapterId === prevCh.id)) return;
  if (!prevCh.isFree && !isUnlocked(prevCh.id)) return;

  loadingPrev.value = true;
  try {
    const data = await fetchChapterContent(bookId.value, prevCh.id);
    const newSection = {
      chapterId: prevCh.id,
      title: data?.title || prevCh.title,
      paragraphs: (data?.content || '').split('\n').filter((p: string) => p.trim()),
    };

    const el = getScrollEl();
    const oldScrollTop = el?.scrollTop || 0;
    sections.value.unshift(newSection);

    await nextTick();
    // 估算新内容高度
    const lineH = config.value.fontSize * 2 * (config.value.lineHeight / 100);
    const estimatedHeight = newSection.paragraphs.length * lineH + 120;
    if (el) el.scrollTop = oldScrollTop + estimatedHeight;

    chapterId.value = prevCh.id;
  } catch (e) {
    console.error('auto load prev failed', e);
  } finally {
    loadingPrev.value = false;
  }
}

// 点击区域
function onTapLeft() {
  if (config.value.pagingMode === 'tap') {
    pageUp();
  }
}

function onTapCenter() {
  toggleMenu();
}

function onTapRight() {
  if (config.value.pagingMode === 'tap') {
    pageDown();
  }
}

function toggleMenu() {
  showMenu.value = !showMenu.value;
}

function openSettings() {
  showMenu.value = false;
  showSettingsPanel.value = true;
}

function pageUp() {
  const el = getScrollEl();
  if (!el) return;
  const pageHeight = (uni.getSystemInfoSync().windowHeight || 600) * 0.85;
  el.scrollTop = Math.max(0, el.scrollTop - pageHeight);
}

function pageDown() {
  const el = getScrollEl();
  if (!el) return;
  const pageHeight = (uni.getSystemInfoSync().windowHeight || 600) * 0.85;
  el.scrollTop = el.scrollTop + pageHeight;
  setTimeout(() => autoLoadNext(), 300);
}

function goBack() {
  uni.navigateBack();
}

async function selectChapter(ch: Chapter) {
  showCatalog.value = false;
  chapterId.value = ch.id;
  await loadCurrentChapter();
}

function toggleDarkQuick() {
  config.value.theme = isDark.value ? 'light' : 'dark';
  debouncedSaveConfig();
}

// 设置项调整
function adjustFontSize(delta: number) {
  const newSize = config.value.fontSize + delta;
  if (newSize >= 14 && newSize <= 24) {
    config.value.fontSize = newSize;
    debouncedSaveConfig();
  }
}

function setLineHeight(v: number) {
  config.value.lineHeight = v;
  debouncedSaveConfig();
}

function setParagraphSpacing(v: number) {
  config.value.paragraphSpacing = v;
  debouncedSaveConfig();
}

function setFontFamily(v: string) {
  config.value.fontFamily = v;
  debouncedSaveConfig();
}

function setTheme(v: string) {
  config.value.theme = v;
  debouncedSaveConfig();
}

function adjustBrightness(delta: number) {
  const newVal = config.value.brightness + delta;
  if (newVal >= 50 && newVal <= 150) {
    config.value.brightness = newVal;
    debouncedSaveConfig();
  }
}

function setPagingMode(v: string) {
  config.value.pagingMode = v;
  debouncedSaveConfig();
}

// 广告解锁
async function watchAd() {
  uni.showLoading({ title: '加载广告...' });
  setTimeout(async () => {
    uni.hideLoading();
    try {
      const adToken = 'mock_ad_token_' + Date.now();
      await unlockByAd(bookId.value, chapterId.value, adToken);
      unlockedChapters.value.push(chapterId.value);
      showAdUnlock.value = false;
      uni.showToast({ title: '解锁成功', icon: 'none' });
      await loadCurrentChapter();
    } catch (e) {
      uni.showToast({ title: '解锁失败', icon: 'none' });
    }
  }, 2000);
}

async function buyChapter() {
  try {
    await apiBuyChapter(bookId.value, chapterId.value);
    unlockedChapters.value.push(chapterId.value);
    showAdUnlock.value = false;
    uni.showToast({ title: '购买成功', icon: 'none' });
    await loadCurrentChapter();
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
  position: relative;
  overflow: hidden;
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
  transition: transform 0.3s ease;
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
  padding: 0 40rpx;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  position: relative;
  z-index: 1;
}
.section-block {
  padding: 40rpx 0;
}
.chapter-heading {
  font-size: 40rpx;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
  margin-bottom: 40rpx;
  display: block;
}
.paragraph {
  text-indent: 2em;
}
.loading-bar {
  text-align: center;
  padding: 32rpx 0;
}
.loading-bar text {
  font-size: 24rpx;
  color: #AAAAAA;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 点击区域 - pointer-events: none 避免拦截滚动 */
.tap-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 5;
  display: flex;
  flex-direction: row;
  pointer-events: none;
}
.tap-zone {
  height: 100%;
  pointer-events: auto;
}
.tap-left {
  width: 30%;
}
.tap-center {
  width: 40%;
}
.tap-right {
  width: 30%;
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
  transition: transform 0.3s ease;
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
  background: rgba(163, 74, 46, 0.1);
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

/* 点击区域 - 不拦截滚动 */
.tap-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 5;
  display: flex;
  flex-direction: row;
  pointer-events: none;
}
.tap-zone {
  height: 100%;
  pointer-events: auto;
}
.tap-left {
  width: 30%;
}
.tap-center {
  width: 40%;
}
.tap-right {
  width: 30%;
}

/* 全屏设置面板 */
.settings-fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 200;
  display: flex;
  flex-direction: column;
  padding: 48rpx 32rpx;
}
.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}
.settings-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #2C2C2C;
  font-family: 'Noto Serif SC', serif;
}
.settings-title.dark-text {
  color: #EEEEEE;
}
.settings-close-btn {
  font-size: 32rpx;
  color: #888888;
  padding: 8rpx;
}
.settings-close-btn.dark-text {
  color: #AAAAAA;
}
.settings-tabs {
  display: flex;
  justify-content: center;
  gap: 16rpx;
  margin-bottom: 32rpx;
  border-bottom: 1rpx solid rgba(163, 74, 46, 0.15);
  padding-bottom: 16rpx;
}
.dark-mode .settings-tabs {
  border-bottom-color: rgba(255,255,255,0.1);
}
.settings-tab {
  padding: 12rpx 32rpx;
  border-radius: 48rpx;
  background: rgba(163, 74, 46, 0.08);
}
.dark-mode .settings-tab {
  background: rgba(255,255,255,0.08);
}
.settings-tab text {
  font-size: 28rpx;
  color: #645D55;
  font-family: 'Noto Sans SC', sans-serif;
}
.dark-mode .settings-tab text {
  color: #AAAAAA;
}
.settings-tab.active {
  background: #A34A2E;
}
.settings-tab.active text {
  color: #FFFFFF;
}
.dark-mode .settings-tab.active {
  background: rgba(163, 74, 46, 0.9);
}
.settings-body-scroll {
  flex: 1;
}
.settings-body {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
  padding-bottom: 40rpx;
}
.setting-group-title {
  font-size: 26rpx;
  color: #645D55;
  font-family: 'Noto Sans SC', sans-serif;
  margin-bottom: 16rpx;
  display: block;
}
.dark-mode .setting-group-title {
  color: #AAAAAA;
}
.font-size-control {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32rpx;
}
.size-btn {
  font-size: 32rpx;
  color: #2C2C2C;
  padding: 12rpx 24rpx;
  background: rgba(163, 74, 46, 0.08);
  border-radius: 12rpx;
}
.dark-mode .size-btn {
  color: #EEEEEE;
  background: rgba(255,255,255,0.1);
}
.size-value {
  font-size: 36rpx;
  color: #2C2C2C;
  font-weight: 600;
  min-width: 80rpx;
  text-align: center;
}
.dark-mode .size-value {
  color: #EEEEEE;
}
.btn-group {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}
.group-btn {
  padding: 12rpx 28rpx;
  border-radius: 48rpx;
  background: rgba(163, 74, 46, 0.08);
  font-size: 26rpx;
  color: #645D55;
  font-family: 'Noto Sans SC', sans-serif;
}
.dark-mode .group-btn {
  background: rgba(255,255,255,0.1);
  color: #CCCCCC;
}
.group-btn.active {
  background: #A34A2E;
  color: #FFFFFF;
}
.dark-mode .group-btn.active {
  background: rgba(163, 74, 46, 0.9);
}
.theme-options {
  display: flex;
  gap: 16rpx;
}
.theme-option {
  width: 140rpx;
  height: 100rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid transparent;
}
.theme-option.active {
  border-color: #A34A2E;
}
.theme-option text {
  font-size: 24rpx;
  font-family: 'Noto Sans SC', sans-serif;
}
.brightness-control {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.brightness-btn {
  font-size: 32rpx;
  color: #2C2C2C;
  padding: 8rpx 16rpx;
  background: rgba(163, 74, 46, 0.08);
  border-radius: 12rpx;
}
.dark-mode .brightness-btn {
  color: #EEEEEE;
  background: rgba(255,255,255,0.1);
}
.brightness-bar {
  flex: 1;
  height: 8rpx;
  background: rgba(163, 74, 46, 0.15);
  border-radius: 4rpx;
  overflow: hidden;
}
.dark-mode .brightness-bar {
  background: rgba(255,255,255,0.2);
}
.brightness-fill {
  height: 100%;
  background: #A34A2E;
  border-radius: 4rpx;
  transition: width 0.2s ease;
}
.setting-hint {
  padding: 16rpx;
  background: rgba(163, 74, 46, 0.06);
  border-radius: 12rpx;
}
.dark-mode .setting-hint {
  background: rgba(255,255,255,0.05);
}
.setting-hint text {
  font-size: 24rpx;
  color: #888888;
  font-family: 'Noto Sans SC', sans-serif;
}
.dark-mode .setting-hint text {
  color: #888888;
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
  background: #A34A2E;
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
