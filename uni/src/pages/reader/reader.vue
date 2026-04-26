<template>
  <view class="reader-page" :class="[themeClass, { 'dark-mode': isDark }]" :style="pageStyle">
    <!-- 左上角常驻信息 -->
    <view class="reader-info" @click.stop>
      <text class="back-btn" @click="goBack">←</text>
      <text class="chapter-title">{{ currentTitle }}</text>
    </view>

    <!-- 顶部菜单栏（点击屏幕中央触发） -->
    <view class="reader-top" :class="{ 'menu-hidden': !showMenu, 'menu-visible': showMenu }" @click.stop>
      <text class="shelf-btn" :class="{ 'in-shelf': isInShelf }" @click="toggleShelf">{{ isInShelf ? '已加书架' : '加入书架' }}</text>
    </view>

    <!-- 内容区 -->
    <view
      id="reader-content"
      class="content-scroll"
      @touchstart="onTouchStart"
      @touchmove="onTouchMove"
      @touchend="onTouchEnd"
      :style="scrollStyle"
    >
      <!-- 下拉加载指示器 -->
      <view v-if="pullState !== 'idle'" class="pull-indicator" :style="{ height: pullOffset + 'px' }">
        <view class="pull-spinner" :class="{ 'pull-spinner-active': pullState === 'loading' }">
          <svg class="spinner" viewBox="0 0 50 50">
            <circle cx="25" cy="25" r="20" fill="none" stroke="#A34A2E" stroke-width="3" stroke-linecap="round" stroke-dasharray="30, 200" />
          </svg>
        </view>
      </view>

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
        :class="{ locked: section.locked }"
        :id="'ch-' + section.chapterId"
        @click="section.locked ? openUnlock(section.chapterId) : null"
      >
        <text class="chapter-heading" :style="headingStyle">{{ section.title }}</text>
        <view v-if="section.locked" class="locked-block">
          <text class="locked-icon">🔒</text>
          <text class="locked-title">本章为付费章节</text>
          <text class="locked-sub">点击解锁后继续阅读</text>
        </view>
        <template v-else>
          <view
            class="paragraph"
            v-for="(p, i) in section.paragraphs"
            :key="i"
            :style="paragraphWrapStyle"
          >
            <text :style="textStyle">{{ p }}</text>
          </view>
        </template>
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
    <view class="reader-bottom" :class="{ 'menu-hidden': !showMenu, 'menu-visible': showMenu }" @click.stop>
      <view class="bottom-item" @click="showCatalog = true">
        <text class="bottom-icon">☰</text>
        <text class="bottom-label">目录</text>
      </view>
      <view class="bottom-item" @click="toggleDarkQuick">
        <text class="bottom-icon">{{ isDark ? '☀' : '☾' }}</text>
        <text class="bottom-label">{{ isDark ? '日间' : '夜间' }}</text>
      </view>
      <view class="bottom-item" @click="openSettings">
        <text class="bottom-icon">Aa</text>
        <text class="bottom-label">设置</text>
      </view>
    </view>

    <!-- 目录弹窗 -->
    <view class="catalog-mask" :class="{ 'mask-hidden': !showCatalog, 'mask-visible': showCatalog }" @click="showCatalog = false">
      <view class="catalog-sheet" @click.stop :class="{ 'dark-mode': isDark }">
        <!-- 拖拽指示条 -->
        <view class="sheet-drag-handle" />

        <!-- 头部：标题 + 排序切换 + 关闭 -->
        <view class="catalog-header">
          <view class="catalog-header-left">
            <text class="catalog-title">章节目录</text>
            <text class="catalog-count">共 {{ chapters.length }} 章</text>
          </view>
          <view class="catalog-header-right">
            <view class="catalog-sort-btn" @click="toggleCatalogSort">
              <text class="sort-icon">{{ catalogDesc ? '↓' : '↑' }}</text>
              <text class="sort-label">{{ catalogDesc ? '倒序' : '正序' }}</text>
            </view>
            <text class="catalog-close" @click="showCatalog = false">✕</text>
          </view>
        </view>

        <!-- 分页标签 -->
        <scroll-view v-if="catalogPages.length > 1" class="catalog-page-tabs" scroll-x show-scrollbar="false">
          <view
            v-for="(page, idx) in catalogPages"
            :key="idx"
            class="catalog-page-tab"
            :class="{ active: currentCatalogPage === idx }"
            @click="currentCatalogPage = idx"
          >
            <text>{{ page.label }}</text>
          </view>
        </scroll-view>

        <!-- 章节列表 -->
        <scroll-view class="catalog-list" scroll-y @touchmove.stop @catchtouchmove.stop>
          <view
            v-for="ch in displayedCatalogChapters"
            :key="ch.id"
            class="catalog-item"
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
import { fetchBook, fetchChapters, fetchChapterContent, saveProgress, checkUnlock, unlockByAd, buyChapter as apiBuyChapter, checkShelf, addToShelf, removeFromShelf, type Chapter } from '@/api/book';
import { fetchReaderConfig, saveReaderConfig } from '@/api/user';
import { adComplete } from '@/api/ad';

const bookId = ref(0);
const chapterId = ref(0);
const bookWordCount = ref(0);
const bookTitle = ref('');
const chapters = ref<Chapter[]>([]);
const sections = ref<Array<{ chapterId: number; title: string; paragraphs: string[]; locked?: boolean }>>([]);
const isLoading = ref(false);
const loadingNext = ref(false);
const loadingPrev = ref(false);
const showMenu = ref(false);
const showCatalog = ref(false);
const showSettingsPanel = ref(false);
const showAdUnlock = ref(false);
const currentScrollTop = ref(0);
const isInShelf = ref(false);
const activeSettingsTab = ref('display');
const unlockedChapters = ref<number[]>([]);
const pullOffset = ref(0);
const pullState = ref<'idle' | 'pulling' | 'loading'>('idle');

// 目录相关
const catalogDesc = ref(false);
const currentCatalogPage = ref(0);
const CATALOG_PAGE_SIZE = 50;

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

const themeClass = computed(() => {
  const map: Record<string, string> = {
    light: 'theme-light',
    white: 'theme-white',
    green: 'theme-green',
    dark: 'theme-dark',
  };
  return map[config.value.theme] || 'theme-light';
});

const panelBg = computed(() => {
  const map: Record<string, string> = {
    light: '#F5F0EA',
    white: '#FFFFFF',
    green: '#E8F5E9',
    dark: '#1A1A2E',
  };
  return map[config.value.theme] || '#F5F0EA';
});

const textColor = computed(() => {
  return isDark.value ? 'rgba(204, 204, 204, 0.6)' : 'rgba(44, 44, 44, 0.5)';
});

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
  return ch?.title || bookTitle.value || '加载中...';
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

// 目录分页计算
const catalogPages = computed(() => {
  const total = chapters.value.length;
  if (total <= CATALOG_PAGE_SIZE) return [];
  const pages = [];
  for (let i = 0; i < total; i += CATALOG_PAGE_SIZE) {
    const start = i + 1;
    const end = Math.min(i + CATALOG_PAGE_SIZE, total);
    pages.push({ label: `${start}-${end}章`, start: i, end });
  }
  return pages;
});

// 目录显示的章节（排序+分页后）
const displayedCatalogChapters = computed(() => {
  let list = [...chapters.value];
  if (catalogDesc.value) {
    list = list.reverse();
  }
  if (catalogPages.value.length > 0) {
    const page = catalogPages.value[currentCatalogPage.value];
    if (page) {
      list = list.slice(page.start, page.end);
    }
  }
  return list;
});

function toggleCatalogSort() {
  catalogDesc.value = !catalogDesc.value;
  currentCatalogPage.value = 0;
}

onLoad(async (opts: any) => {
  bookId.value = parseInt(opts?.bookId || '1');
  chapterId.value = parseInt(opts?.chapterId || '1');
  loadUnlockedFromStorage();
  await loadConfig();
  checkShelfStatus();
  try {
    const book = await fetchBook(bookId.value);
    bookWordCount.value = book?.wordCount || 0;
    bookTitle.value = book?.title || '';
  } catch {}
  await loadChapters();
  await loadCurrentChapter();
});

function loadUnlockedFromStorage() {
  const key = `unlocked_${bookId.value}`;
  const cached = uni.getStorageSync(key);
  if (cached) {
    try {
      unlockedChapters.value = JSON.parse(cached);
    } catch {}
  }
}

function saveUnlockedToStorage() {
  const key = `unlocked_${bookId.value}`;
  uni.setStorageSync(key, JSON.stringify(unlockedChapters.value));
}

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
      sections.value = [{
        chapterId: ch.id,
        title: ch.title,
        paragraphs: [],
        locked: true,
      }];
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
    saveProgress(bookId.value, chapterId.value, calculateBookProgress(chapterId.value, 0)).catch(() => {});
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

function calculateBookProgress(currentChapterId: number, chapterProgress: number): number {
  let totalWords = bookWordCount.value;
  if (totalWords === 0) {
    totalWords = chapters.value.reduce((sum, ch) => sum + (ch.wordCount || 0), 0);
  }
  if (totalWords === 0) return 0;

  const sorted = [...chapters.value].sort((a, b) => (a.order || 0) - (b.order || 0));
  const idx = sorted.findIndex(c => c.id === currentChapterId);
  if (idx < 0) return 0;

  let readWords = 0;
  for (let i = 0; i < sorted.length; i++) {
    if (i < idx) {
      readWords += sorted[i].wordCount || 0;
    } else if (i === idx) {
      readWords += (sorted[i].wordCount || 0) * (chapterProgress / 100);
      break;
    }
  }
  return Math.min(100, Math.round((readWords / totalWords) * 100));
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

// 滚动监听 + 进度保存（防抖）
let saveProgressTimer: ReturnType<typeof setTimeout> | null = null;
let currentReadingChapterId = 0;
let currentReadingProgress = 0;

function onScrollHandler() {
  const el = getScrollEl();
  if (!el) return;
  currentScrollTop.value = el.scrollTop;

  // 找到当前视口中间位置对应的 section
  const sectionEls = el.querySelectorAll('.section-block');
  const containerRect = el.getBoundingClientRect();
  const midPoint = containerRect.top + containerRect.height / 2;

  for (const sectionEl of sectionEls) {
    const rect = (sectionEl as HTMLElement).getBoundingClientRect();
    if (rect.top <= midPoint && rect.bottom >= midPoint) {
      const cid = parseInt((sectionEl as HTMLElement).id.replace('ch-', ''));
      const sectionHeight = (sectionEl as HTMLElement).offsetHeight;
      const progress = sectionHeight > 0
        ? Math.min(100, Math.max(0, Math.round(((midPoint - rect.top) / sectionHeight) * 100)))
        : 0;

      currentReadingChapterId = cid;
      currentReadingProgress = progress;

      if (saveProgressTimer) clearTimeout(saveProgressTimer);
      saveProgressTimer = setTimeout(() => {
        saveProgress(bookId.value, cid, calculateBookProgress(cid, progress)).catch(() => {});
      }, 1000);
      break;
    }
  }
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
  if (saveProgressTimer) clearTimeout(saveProgressTimer);
  if (currentReadingChapterId > 0) {
    saveProgress(bookId.value, currentReadingChapterId, calculateBookProgress(currentReadingChapterId, currentReadingProgress)).catch(() => {});
  }
});

// 触摸点击区域识别
let touchStartX = 0;
let touchStartY = 0;
let touchStartTime = 0;

function onTouchStart(e: any) {
  touchStartX = e.touches[0].clientX;
  touchStartY = e.touches[0].clientY;
  touchStartTime = Date.now();
  pullState.value = 'idle';
  pullOffset.value = 0;
}

function onTouchMove(e: any) {
  const el = getScrollEl();
  if (!el || el.scrollTop > 0) {
    pullState.value = 'idle';
    pullOffset.value = 0;
    return;
  }
  const dy = e.touches[0].clientY - touchStartY;
  if (dy > 0) {
    pullOffset.value = Math.min(dy * 0.4, 120);
    // 下拉超过阈值直接触发加载，不需要松手
    if (pullState.value !== 'loading' && pullOffset.value >= 60) {
      pullState.value = 'loading';
      pullOffset.value = 50; // 固定高度，显示加载中
      autoLoadPrev();
    } else if (pullState.value !== 'loading') {
      pullState.value = 'pulling';
    }
  } else {
    pullOffset.value = 0;
    pullState.value = 'idle';
  }
}

function onTouchEnd(e: any) {
  const dx = e.changedTouches[0].clientX - touchStartX;
  const dy = e.changedTouches[0].clientY - touchStartY;
  const dt = Date.now() - touchStartTime;

  // 如果正在加载中，忽略点击判断
  if (pullState.value === 'loading') {
    return;
  }

  pullState.value = 'idle';
  pullOffset.value = 0;

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

  // 付费未解锁：添加锁定占位
  if (!nextCh.isFree && !isUnlocked(nextCh.id)) {
    sections.value.push({
      chapterId: nextCh.id,
      title: nextCh.title,
      paragraphs: [],
      locked: true,
    });
    return;
  }

  loadingNext.value = true;
  try {
    const data = await fetchChapterContent(bookId.value, nextCh.id);
    sections.value.push({
      chapterId: nextCh.id,
      title: data?.title || nextCh.title,
      paragraphs: (data?.content || '').split('\n').filter((p: string) => p.trim()),
    });
    chapterId.value = nextCh.id;
    saveProgress(bookId.value, nextCh.id, calculateBookProgress(nextCh.id, 0)).catch(() => {});
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

    sections.value.unshift(newSection);

    await nextTick();
    // prepend 后，原来的第一个 section 变成了第二个，滚动到它的新 offsetTop 即可保持文字位置不变
    const el = getScrollEl();
    const sectionEls = el?.querySelectorAll('.section-block');
    if (el && sectionEls && sectionEls.length > 1) {
      el.scrollTop = (sectionEls[1] as HTMLElement).offsetTop;
    }

    // 加载上一章时不修改 chapterId，用户当前仍在阅读原来的章节
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

function openUnlock(cid: number) {
  chapterId.value = cid;
  showAdUnlock.value = true;
}

async function loadLockedChapter(cid: number) {
  try {
    const data = await fetchChapterContent(bookId.value, cid);
    const idx = sections.value.findIndex(s => s.chapterId === cid);
    if (idx >= 0) {
      sections.value.splice(idx, 1, {
        chapterId: cid,
        title: data?.title || sections.value[idx].title,
        paragraphs: (data?.content || '').split('\n').filter((p: string) => p.trim()),
      });
    }
    saveProgress(bookId.value, cid, calculateBookProgress(cid, 0)).catch(() => {});
    await nextTick();
    setupObservers();
    await checkAndLoadMore();
  } catch (e) {
    console.error('load locked chapter failed', e);
  }
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

const shelfLoading = ref(false);

async function toggleShelf() {
  if (shelfLoading.value) return;
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
  shelfLoading.value = true;
  try {
    if (isInShelf.value) {
      await removeFromShelf(bookId.value);
      isInShelf.value = false;
      uni.showToast({ title: '已移除书架', icon: 'none' });
    } else {
      await addToShelf(bookId.value);
      isInShelf.value = true;
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
  } finally {
    shelfLoading.value = false;
  }
}

async function checkShelfStatus() {
  const token = uni.getStorageSync('token');
  if (!token) {
    isInShelf.value = false;
    return;
  }
  try {
    const res = await checkShelf(bookId.value);
    isInShelf.value = res?.inShelf || false;
  } catch {
    isInShelf.value = false;
  }
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
      const tokenRes = await adComplete();
      const adToken = tokenRes?.token;
      if (!adToken) {
        uni.showToast({ title: '今日广告次数已用完', icon: 'none' });
        return;
      }
      const unlockRes = await unlockByAd(chapterId.value, adToken);
      if (unlockRes?.error) {
        uni.showToast({ title: '解锁失败', icon: 'none' });
        return;
      }
      unlockedChapters.value.push(chapterId.value);
      saveUnlockedToStorage();
      showAdUnlock.value = false;
      uni.showToast({ title: '解锁成功', icon: 'none' });
      await loadLockedChapter(chapterId.value);
    } catch (e) {
      uni.showToast({ title: '解锁失败', icon: 'none' });
    }
  }, 2000);
}

async function buyChapter() {
  try {
    await apiBuyChapter(bookId.value, chapterId.value);
    unlockedChapters.value.push(chapterId.value);
    saveUnlockedToStorage();
    showAdUnlock.value = false;
    uni.showToast({ title: '购买成功', icon: 'none' });
    await loadLockedChapter(chapterId.value);
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

/* 左上角常驻信息 */
.reader-info {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 50rpx 24rpx 20rpx;
  background: v-bind(panelBg);
  pointer-events: none;
}
.reader-info .back-btn {
  position: absolute;
  left: 24rpx;
  pointer-events: auto;
  font-size: 36rpx;
  color: v-bind(textColor);
  padding: 8rpx;
}
.reader-info .chapter-title {
  font-size: 26rpx;
  color: v-bind(textColor);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 60%;
}

/* 顶部栏 */
.reader-top {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 24rpx;
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
  background: rgba(245, 240, 234, 0.98);
  backdrop-filter: blur(10px);
  z-index: 200;
  border-bottom: 1rpx solid rgba(0,0,0,0.05);
  transition: transform 0.35s cubic-bezier(0.32, 0.72, 0, 1), opacity 0.35s ease;
  box-sizing: border-box;
}
.reader-top.menu-hidden {
  transform: translateY(-100%);
  opacity: 0;
  pointer-events: none;
}
.reader-top.menu-visible {
  transform: translateY(0);
  opacity: 1;
  pointer-events: auto;
}
.dark-mode .reader-top {
  background: rgba(26, 26, 46, 0.95);
  border-bottom-color: #333;
}
/* 纯白主题 */
.reader-page.theme-white .reader-top {
  background: rgba(255, 255, 255, 0.98);
  border-bottom-color: rgba(0,0,0,0.08);
}
/* 护眼主题 */
.reader-page.theme-green .reader-top {
  background: rgba(232, 245, 233, 0.98);
  border-bottom-color: rgba(0,0,0,0.05);
}
.shelf-btn {
  font-size: 24rpx;
  color: #FFFFFF;
  background: #A34A2E;
  padding: 8rpx 24rpx;
  border-radius: 24rpx;
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  height: 44rpx;
  box-sizing: border-box;
}
.shelf-btn.in-shelf {
  background: #999999;
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
  padding: 120rpx 40rpx 0;
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
  transition: transform 0.35s cubic-bezier(0.32, 0.72, 0, 1), opacity 0.35s ease;
}
.reader-bottom.menu-hidden {
  transform: translateY(100%);
  opacity: 0;
  pointer-events: none;
}
.reader-bottom.menu-visible {
  transform: translateY(0);
  opacity: 1;
  pointer-events: auto;
}
.dark-mode .reader-bottom {
  background: rgba(26, 26, 46, 0.95);
  border-top-color: #333;
}
/* 纯白主题 */
.reader-page.theme-white .reader-bottom {
  background: rgba(255, 255, 255, 0.95);
  border-top-color: #E8E8E8;
}
/* 护眼主题 */
.reader-page.theme-green .reader-bottom {
  background: rgba(232, 245, 233, 0.95);
  border-top-color: #C8E6C9;
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
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  transition: opacity 0.3s ease;
  touch-action: none;
  -webkit-touch-callout: none;
}
.catalog-mask.mask-hidden {
  opacity: 0;
  pointer-events: none;
}
.catalog-mask.mask-visible {
  opacity: 1;
  pointer-events: auto;
}
.catalog-sheet {
  background: #FFFFFF;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 80vh;
  min-height: 40vh;
  display: flex;
  flex-direction: column;
  padding: 16rpx 0 32rpx;
  transition: transform 0.35s cubic-bezier(0.32, 0.72, 0, 1);
  width: 100%;
  touch-action: pan-y;
  -webkit-overflow-scrolling: touch;
}
.catalog-mask.mask-hidden .catalog-sheet {
  transform: translateY(100%);
}
.catalog-mask.mask-visible .catalog-sheet {
  transform: translateY(0);
}
.dark-mode .catalog-sheet {
  background: #1A1A2E;
}
/* 羊皮纸主题 */
.theme-light .catalog-sheet {
  background: #F5F0EA;
}
/* 纯白主题 */
.theme-white .catalog-sheet {
  background: #FFFFFF;
}
/* 护眼主题 */
.theme-green .catalog-sheet {
  background: #E8F5E9;
}
.catalog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 32rpx 20rpx;
  border-bottom: 1rpx solid rgba(0,0,0,0.06);
}
.catalog-header-left {
  display: flex;
  align-items: baseline;
  gap: 16rpx;
}
.catalog-header-right {
  display: flex;
  align-items: center;
  gap: 24rpx;
}
.catalog-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #2C2C2C;
  font-family: 'Noto Serif SC', serif;
}
.dark-mode .catalog-title {
  color: #EEEEEE;
}
.catalog-count {
  font-size: 24rpx;
  color: #888888;
  font-family: 'Noto Sans SC', sans-serif;
}
.dark-mode .catalog-count {
  color: #AAAAAA;
}
.catalog-sort-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  border-radius: 24rpx;
  background: rgba(163, 74, 46, 0.08);
}
.dark-mode .catalog-sort-btn {
  background: rgba(255,255,255,0.08);
}
.sort-icon {
  font-size: 22rpx;
  color: #A34A2E;
}
.sort-label {
  font-size: 22rpx;
  color: #645D55;
  font-family: 'Noto Sans SC', sans-serif;
}
.dark-mode .sort-label {
  color: #AAAAAA;
}
.catalog-close {
  font-size: 32rpx;
  color: #888888;
  padding: 8rpx;
}

/* 分页标签 */
.catalog-page-tabs {
  white-space: nowrap;
  padding: 16rpx 24rpx 8rpx;
  border-bottom: 1rpx solid rgba(0,0,0,0.04);
}
.catalog-page-tab {
  display: inline-flex;
  padding: 10rpx 24rpx;
  border-radius: 24rpx;
  margin-right: 12rpx;
  background: rgba(0,0,0,0.04);
}
.catalog-page-tab text {
  font-size: 24rpx;
  color: #666666;
  font-family: 'Noto Sans SC', sans-serif;
}
.dark-mode .catalog-page-tab text {
  color: #AAAAAA;
}
.catalog-page-tab.active {
  background: #A34A2E;
}
.catalog-page-tab.active text {
  color: #FFFFFF;
}

.catalog-list {
  flex: 1;
  padding: 8rpx 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  touch-action: pan-y;
}
.catalog-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid rgba(0,0,0,0.04);
}
.catalog-item.active {
  background: rgba(163, 74, 46, 0.08);
}
.catalog-item.locked {
  opacity: 0.5;
}
.catalog-num {
  font-size: 24rpx;
  color: #888888;
  width: 56rpx;
  font-family: 'Noto Serif SC', serif;
  text-align: center;
  flex-shrink: 0;
}
.catalog-name {
  flex: 1;
  font-size: 28rpx;
  color: #2C2C2C;
  font-family: 'Noto Sans SC', sans-serif;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dark-mode .catalog-name {
  color: #CCCCCC;
}
.catalog-lock {
  font-size: 24rpx;
  flex-shrink: 0;
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
/* 下拉加载指示器 */
.pull-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: height 0.15s ease;
}
.pull-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.6;
  transition: opacity 0.2s ease;
}
.pull-spinner-active {
  opacity: 1;
}
.pull-spinner .spinner {
  width: 32rpx;
  height: 32rpx;
  animation: rotate 0.8s linear infinite;
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
/* 羊皮纸主题（默认） */
.theme-light .settings-sheet {
  background: #F5F0EA;
}
/* 纯白主题 */
.theme-white .settings-sheet {
  background: #FFFFFF;
}
/* 护眼主题 */
.theme-green .settings-sheet {
  background: #E8F5E9;
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
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.paging-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  min-width: 0;
}
.paging-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #2C2C2C;
  font-family: 'Noto Sans SC', sans-serif;
  line-height: 1.4;
}
.paging-title.dark-text {
  color: #EEEEEE;
}
.paging-desc {
  font-size: 24rpx;
  color: #888888;
  font-family: 'Noto Sans SC', sans-serif;
  line-height: 1.4;
}
.paging-desc.dark-text {
  color: #AAAAAA;
}
.paging-check {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: #A34A2E;
  flex-shrink: 0;
}

/* 下拉提示 */
.pull-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: height 0.1s ease;
}
.pull-hint-text {
  font-size: 24rpx;
  color: #A34A2E;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 锁定章节占位 */
.section-block.locked {
  padding: 48rpx 0;
  text-align: center;
}
.locked-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 48rpx 32rpx;
  border-radius: 20rpx;
  background: rgba(163, 74, 46, 0.06);
  border: 2rpx dashed #D4C4B8;
}
.dark-mode .locked-block {
  background: rgba(255,255,255,0.06);
  border-color: #444;
}
.locked-icon {
  font-size: 48rpx;
}
.locked-title {
  font-size: 28rpx;
  color: #2C2C2C;
  font-family: 'Noto Sans SC', sans-serif;
  font-weight: 600;
}
.dark-mode .locked-title {
  color: #CCCCCC;
}
.locked-sub {
  font-size: 24rpx;
  color: #888888;
  font-family: 'Noto Sans SC', sans-serif;
}
.dark-mode .locked-sub {
  color: #AAAAAA;
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
