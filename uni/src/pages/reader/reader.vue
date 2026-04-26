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
      @touchstart="onTouchStart"
      @touchend="onTouchEnd"
      :style="scrollStyle"
    >
      <!-- 顶部哨兵：用于检测滚动到顶部时加载上一章 -->
      <view id="top-sentinel" style="height: 1px;" />

      <!-- 上一章加载中 -->
      <view v-if="loadingPrev" class="loading-spinner">
        <svg class="spinner" viewBox="0 0 50 50">
          <circle cx="25" cy="25" r="20" fill="none" stroke="#A34A2E" stroke-width="3" stroke-linecap="round" stroke-dasharray="30, 200" />
        </svg>
        <text>加载中</text>
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

      <!-- 下一章加载中 -->
      <view v-if="loadingNext" class="loading-spinner">
        <svg class="spinner" viewBox="0 0 50 50">
          <circle cx="25" cy="25" r="20" fill="none" stroke="#A34A2E" stroke-width="3" stroke-linecap="round" stroke-dasharray="30, 200" />
        </svg>
        <text>加载中</text>
      </view>

      <!-- 底部哨兵：用于检测滚动到底部时加载下一章 -->
      <view id="bottom-sentinel" style="height: 1px;" />

      <view style="height: 200rpx" />
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

    <!-- 底部设置面板 -->
    <view class="settings-mask" v-if="showSettingsPanel" @click="showSettingsPanel = false">
      <view class="settings-sheet" @click.stop :class="{ 'dark-mode': isDark }">
        <view class="sheet-drag-handle" />

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
            <view class="setting-card">
              <view class="setting-card-header">
                <text class="setting-card-title" :class="{ 'dark-text': isDark }">字号大小</text>
                <text class="setting-card-value" :class="{ 'dark-text': isDark }">{{ config.fontSize }}</text>
              </view>
              <view class="slider-row">
                <text class="slider-btn" :class="{ 'dark-text': isDark }" @click="adjustFontSize(-1)">A-</text>
                <view class="slider-track">
                  <view class="slider-fill" :style="{ width: ((config.fontSize - 14) / 10 * 100) + '%' }" />
                </view>
                <text class="slider-btn" :class="{ 'dark-text': isDark }" @click="adjustFontSize(1)">A+</text>
              </view>
            </view>

            <view class="setting-card">
              <text class="setting-card-title" :class="{ 'dark-text': isDark }">行距</text>
              <view class="btn-group">
                <text
                  v-for="h in lineHeightOptions"
                  :key="h"
                  class="group-btn"
                  :class="{ active: config.lineHeight === h, 'dark-mode': isDark }"
                  @click="setLineHeight(h)"
                >{{ h === 140 ? '紧凑' : h === 160 ? '标准' : h === 180 ? '宽松' : '极宽' }}</text>
              </view>
            </view>

            <view class="setting-card">
              <text class="setting-card-title" :class="{ 'dark-text': isDark }">段距</text>
              <view class="btn-group">
                <text
                  v-for="s in paragraphSpacingOptions"
                  :key="s"
                  class="group-btn"
                  :class="{ active: config.paragraphSpacing === s, 'dark-mode': isDark }"
                  @click="setParagraphSpacing(s)"
                >{{ s }}rpx</text>
              </view>
            </view>

            <view class="setting-card">
              <text class="setting-card-title" :class="{ 'dark-text': isDark }">字体</text>
              <view class="btn-group">
                <text
                  v-for="f in fontFamilyOptions"
                  :key="f.value"
                  class="group-btn"
                  :class="{ active: config.fontFamily === f.value, 'dark-mode': isDark }"
                  @click="setFontFamily(f.value)"
                >{{ f.label }}</text>
              </view>
            </view>
          </view>

          <!-- 主题 Tab -->
          <view v-if="activeSettingsTab === 'theme'" class="settings-body">
            <view class="setting-card">
              <text class="setting-card-title" :class="{ 'dark-text': isDark }">背景主题</text>
              <view class="theme-grid">
                <view
                  class="theme-grid-item"
                  v-for="t in themeOptions"
                  :key="t.key"
                  :class="{ active: config.theme === t.key }"
                  :style="{ background: t.bg }"
                  @click="setTheme(t.key)"
                >
                  <text :style="{ color: t.text }">{{ t.label }}</text>
                  <view v-if="config.theme === t.key" class="theme-check">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#A34A2E" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="20 6 9 17 4 12" />
                    </svg>
                  </view>
                </view>
              </view>
            </view>

            <view class="setting-card">
              <view class="setting-card-header">
                <text class="setting-card-title" :class="{ 'dark-text': isDark }">屏幕亮度</text>
                <text class="setting-card-value" :class="{ 'dark-text': isDark }">{{ config.brightness }}%</text>
              </view>
              <view class="slider-row">
                <text class="slider-btn" :class="{ 'dark-text': isDark }" @click="adjustBrightness(-10)">-</text>
                <view class="slider-track">
                  <view class="slider-fill" :style="{ width: ((config.brightness - 50) / 100 * 100) + '%' }" />
                </view>
                <text class="slider-btn" :class="{ 'dark-text': isDark }" @click="adjustBrightness(10)">+</text>
              </view>
            </view>
          </view>

          <!-- 翻页 Tab -->
          <view v-if="activeSettingsTab === 'paging'" class="settings-body">
            <view class="setting-card">
              <text class="setting-card-title" :class="{ 'dark-text': isDark }">翻页方式</text>
              <view class="paging-options">
                <view
                  class="paging-option"
                  :class="{ active: config.pagingMode === 'scroll', 'dark-mode': isDark }"
                  @click="setPagingMode('scroll')"
                >
                  <text class="paging-icon">↕</text>
                  <view class="paging-info">
                    <text class="paging-title" :class="{ 'dark-text': isDark }">滚动阅读</text>
                    <text class="paging-desc" :class="{ 'dark-text': isDark }">上下滑动连续阅读</text>
                  </view>
                  <view v-if="config.pagingMode === 'scroll'" class="paging-check" />
                </view>
                <view
                  class="paging-option"
                  :class="{ active: config.pagingMode === 'tap', 'dark-mode': isDark }"
                  @click="setPagingMode('tap')"
                >
                  <text class="paging-icon">⇦⇨</text>
                  <view class="paging-info">
                    <text class="paging-title" :class="{ 'dark-text': isDark }">点击翻页</text>
                    <text class="paging-desc" :class="{ 'dark-text': isDark }">点击左右两侧翻页</text>
                  </view>
                  <view v-if="config.pagingMode === 'tap'" class="paging-check" />
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
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
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
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

// IntersectionObserver
let bottomObserver: IntersectionObserver | null = null;
let topObserver: IntersectionObserver | null = null;
let scrollEl: HTMLElement | null = null;

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
    await nextTick();
    setupObservers();
    await checkAndLoadMore();
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
  if (!scrollEl) scrollEl = document.getElementById('reader-content') as HTMLElement;
  return scrollEl;
}

// 滚动监听
function onScrollHandler() {
  const el = getScrollEl();
  if (!el) return;
  currentScrollTop.value = el.scrollTop;
}

// IntersectionObserver 设置
function setupObservers() {
  const root = getScrollEl();
  if (!root) return;

  // 监听底部哨兵，提前 300px 触发
  const bottomSentinel = document.getElementById('bottom-sentinel');
  if (bottomSentinel) {
    if (bottomObserver) bottomObserver.disconnect();
    bottomObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting && !loadingNext.value) {
          autoLoadNext();
        }
      });
    }, { root, rootMargin: '0px 0px 300px 0px', threshold: 0 });
    bottomObserver.observe(bottomSentinel);
  }

  // 监听顶部哨兵，提前 200px 触发
  const topSentinel = document.getElementById('top-sentinel');
  if (topSentinel) {
    if (topObserver) topObserver.disconnect();
    topObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting && !loadingPrev.value) {
          autoLoadPrev();
        }
      });
    }, { root, rootMargin: '200px 0px 0px 0px', threshold: 0 });
    topObserver.observe(topSentinel);
  }
}

function teardownObservers() {
  if (bottomObserver) { bottomObserver.disconnect(); bottomObserver = null; }
  if (topObserver) { topObserver.disconnect(); topObserver = null; }
}

onMounted(() => {
  const el = getScrollEl();
  if (el) el.addEventListener('scroll', onScrollHandler, { passive: true });
  setupObservers();
});

onUnmounted(() => {
  const el = getScrollEl();
  if (el) el.removeEventListener('scroll', onScrollHandler);
  teardownObservers();
});

// 触摸点击区域识别
let touchStartX = 0;
let touchStartY = 0;
let touchStartTime = 0;

function onTouchStart(e: any) {
  touchStartX = e.touches[0].clientX;
  touchStartY = e.touches[0].clientY;
  touchStartTime = Date.now();
}

function onTouchEnd(e: any) {
  const dx = e.changedTouches[0].clientX - touchStartX;
  const dy = e.changedTouches[0].clientY - touchStartY;
  const dt = Date.now() - touchStartTime;

  // 下拉加载上一章：在顶部向下拉（dy > 60）
  if (dy > 60 && Math.abs(dx) < 40 && dt < 600) {
    const el = getScrollEl();
    if (el && el.scrollTop <= 2) {
      autoLoadPrev();
      return;
    }
  }

  // 移动距离大或时间长 = 滑动，不是点击
  if (Math.abs(dx) > 15 || Math.abs(dy) > 15 || dt > 350) return;

  const x = e.changedTouches[0].clientX;
  const width = uni.getSystemInfoSync().windowWidth || 375;
  const ratio = x / width;

  if (ratio < 0.28) {
    onTapLeft();
  } else if (ratio > 0.72) {
    onTapRight();
  } else {
    onTapCenter();
  }
}

// 内容不够一页时自动预加载
async function checkAndLoadMore() {
  await nextTick();
  const el = getScrollEl();
  if (!el) return;
  // 如果内容高度 <= 容器高度，没有滚动空间，主动加载下一章
  if (el.scrollHeight <= el.clientHeight + 20) {
    await autoLoadNext();
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
    await nextTick();
    setupObservers();
    await checkAndLoadMore();
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

/* 加载中 spinner */
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 32rpx 0;
}
.loading-spinner text {
  font-size: 24rpx;
  color: #AAAAAA;
  font-family: 'Noto Sans SC', sans-serif;
}
.spinner {
  width: 40rpx;
  height: 40rpx;
  animation: rotate 1.5s linear infinite;
}
@keyframes rotate {
  100% { transform: rotate(360deg); }
}

/* 底部设置面板 */
.settings-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 200;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}
.settings-sheet {
  background: #FFFFFF;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 75vh;
  min-height: 50vh;
  display: flex;
  flex-direction: column;
  padding: 16rpx 32rpx 48rpx;
  animation: slideUp 0.3s ease;
}
.dark-mode .settings-sheet {
  background: #1A1A2E;
}
@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
.sheet-drag-handle {
  width: 60rpx;
  height: 6rpx;
  background: #CCCCCC;
  border-radius: 3rpx;
  margin: 0 auto 16rpx;
}
.dark-mode .sheet-drag-handle {
  background: #555566;
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
  margin-bottom: 24rpx;
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
  gap: 20rpx;
  padding-bottom: 40rpx;
}

/* 设置卡片 */
.setting-card {
  background: #F8F4F0;
  border-radius: 20rpx;
  padding: 24rpx;
}
.dark-mode .setting-card {
  background: rgba(255,255,255,0.06);
}
.setting-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}
.setting-card-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #2C2C2C;
  font-family: 'Noto Sans SC', sans-serif;
}
.setting-card-title.dark-text {
  color: #EEEEEE;
}
.setting-card-value {
  font-size: 26rpx;
  color: #A34A2E;
  font-weight: 600;
  font-family: 'Noto Sans SC', sans-serif;
}

/* Slider */
.slider-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.slider-btn {
  font-size: 28rpx;
  color: #2C2C2C;
  padding: 8rpx 16rpx;
  font-family: 'Noto Sans SC', sans-serif;
}
.slider-btn.dark-text {
  color: #EEEEEE;
}
.slider-track {
  flex: 1;
  height: 8rpx;
  background: #E8E2D8;
  border-radius: 4rpx;
  overflow: hidden;
}
.dark-mode .slider-track {
  background: rgba(255,255,255,0.15);
}
.slider-fill {
  height: 100%;
  background: #A34A2E;
  border-radius: 4rpx;
  transition: width 0.2s ease;
}

/* 按钮组 */
.btn-group {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}
.group-btn {
  padding: 12rpx 28rpx;
  border-radius: 48rpx;
  background: #FFFFFF;
  border: 1rpx solid #E8E2D8;
  font-size: 26rpx;
  color: #645D55;
  font-family: 'Noto Sans SC', sans-serif;
}
.dark-mode .group-btn {
  background: rgba(255,255,255,0.08);
  border-color: rgba(255,255,255,0.1);
  color: #CCCCCC;
}
.group-btn.active {
  background: #A34A2E;
  border-color: #A34A2E;
  color: #FFFFFF;
}
.dark-mode .group-btn.active {
  background: rgba(163, 74, 46, 0.9);
}

/* 主题网格 */
.theme-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}
.theme-grid-item {
  aspect-ratio: 1;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  border: 2rpx solid transparent;
  position: relative;
}
.theme-grid-item.active {
  border-color: #A34A2E;
}
.theme-grid-item text {
  font-size: 22rpx;
  font-family: 'Noto Sans SC', sans-serif;
}
.theme-check {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 24rpx;
  height: 24rpx;
}

/* 翻页选项 */
.paging-options {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.paging-option {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  background: #FFFFFF;
  border: 2rpx solid #E8E2D8;
}
.dark-mode .paging-option {
  background: rgba(255,255,255,0.06);
  border-color: rgba(255,255,255,0.1);
}
.paging-option.active {
  border-color: #A34A2E;
  background: rgba(163, 74, 46, 0.04);
}
.paging-icon {
  font-size: 40rpx;
}
.paging-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}
.paging-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #2C2C2C;
  font-family: 'Noto Sans SC', sans-serif;
}
.paging-title.dark-text {
  color: #EEEEEE;
}
.paging-desc {
  font-size: 24rpx;
  color: #888888;
  font-family: 'Noto Sans SC', sans-serif;
}
.paging-desc.dark-text {
  color: #AAAAAA;
}
.paging-check {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: #A34A2E;
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
