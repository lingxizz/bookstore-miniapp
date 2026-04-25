<template>
  <view class="page" :style="themeStyle">
    <!-- 顶部工具栏 -->
    <view class="top-bar" :class="{ show: showUI }" @click.stop>
      <text class="back" @click.stop="uni.navigateBack()">←</text>
      <text class="book-title">{{ currentBlockTitle || book.title }}</text>
      <view class="top-actions">
        <view class="shelf-btn" @click.stop="toggleShelf">
          <svg class="shelf-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
          </svg>
          <text class="shelf-text">{{ inShelf ? '已加入' : '加入书架' }}</text>
        </view>
        <text class="more" @click.stop="showSettings = !showSettings">⋮</text>
      </view>
    </view>

    <!-- 连续流阅读内容 -->
    <scroll-view
      scroll-y
      class="reader-content"
      :class="{ 'has-menu': showUI }"
      :scroll-top="scrollTop"
      :scroll-into-view="scrollIntoView"
      @scroll="onScroll"
      @scrolltoupper="onScrollToUpper"
      @scrolltolower="onScrollToLower"
      upper-threshold="200"
      lower-threshold="200"
    >
      <view class="reader-inner" v-if="blocks.length > 0">
        <view v-if="isLoadingPrev" class="loading-prev">
          <view class="loading-spinner"></view>
          <text class="loading-text">正在加载上一章...</text>
        </view>
        <view v-else-if="showPullHint" class="pull-hint">
          <text class="pull-hint-text">↑ 下拉加载上一章</text>
        </view>
        <view
          v-for="block in blocks"
          :key="block.id"
          :id="'ch-' + block.id"
          :data-id="block.id"
          class="chapter-block"
          :class="pageAnimClass"
        >
          <view v-if="block.locked" class="lock-overlay">
            <text class="lock-icon">🔒</text>
            <text class="lock-title">本章已锁定</text>
            <text class="lock-desc">完成广告观看即可解锁继续阅读</text>
            <view class="unlock-ad-btn" @click.stop="startAdUnlock(block.id)">
              <text class="unlock-ad-text">📱 看广告解锁</text>
            </view>
            <text class="lock-hint">解锁后可读完整内容</text>
          </view>

          <template v-else>
            <view class="chapter-title" :style="{ fontSize: fontSize + 4 + 'rpx' }">
              {{ block.title }}
            </view>
            <view
              class="chapter-body"
              :style="{
                fontSize: fontSize + 'rpx',
                lineHeight: lineHeight,
                opacity: contentOpacity,
                fontFamily: fontFamily === 'serif' ? 'Noto Serif SC, serif' : fontFamily === 'sans' ? 'Noto Sans SC, sans-serif' : fontFamily === 'mono' ? 'monospace' : 'inherit'
              }"
            >
              <text
                v-for="(p, i) in block.paragraphs"
                :key="i"
                class="para"
                :id="'para-' + block.id + '-' + i"
                :style="{ marginBottom: paragraphGap + 'rpx' }"
              >{{ p }}</text>
            </view>
            <view class="chapter-end">
              <text>— 本章完 —</text>
            </view>
          </template>
        </view>

        <!-- 加载更多 -->
        <view v-if="isLoadingMore" class="loading-more">
          <text class="loading-text">正在加载下一章...</text>
        </view>

        <!-- 全书完 -->
        <view v-if="allLoaded" class="book-end">
          <text>— 全书完 —</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部设置面板 -->
    <view class="bottom-settings" :class="{ show: showSettings && showUI }" @click.stop>
      <!-- 字号设置 -->
      <view class="setting-row">
        <text class="setting-label">字号</text>
        <view class="setting-control">
          <text class="font-btn" @click.stop="fontSize = Math.max(24, fontSize - 2)">A-</text>
          <text class="font-value">{{ Math.round(fontSize / 2) }}</text>
          <text class="font-btn" @click.stop="fontSize = Math.min(48, fontSize + 2)">A+</text>
        </view>
      </view>

      <!-- 行间距 -->
      <view class="setting-row">
        <text class="setting-label">行间距</text>
        <slider
          class="setting-slider"
          :value="lineHeight * 10"
          min="14"
          max="24"
          step="1"
          @change="e => lineHeight = e.detail.value / 10"
        />
      </view>

      <!-- 段间距 -->
      <view class="setting-row">
        <text class="setting-label">段间距</text>
        <slider
          class="setting-slider"
          :value="paragraphGap"
          min="0"
          max="40"
          step="4"
          @change="e => paragraphGap = e.detail.value"
        />
      </view>

      <!-- 亮度调节 -->
      <view class="setting-row">
        <text class="setting-label">亮度 {{ brightness }}%</text>
        <slider
          class="setting-slider"
          :value="brightness"
          min="30"
          max="150"
          step="5"
          @change="e => brightness = e.detail.value"
        />
      </view>

      <!-- 字体选择 -->
      <view class="setting-row font-row">
        <text class="setting-label">字体</text>
        <view class="font-options">
          <view
            v-for="f in fontList"
            :key="f.key"
            class="font-option"
            :class="{ active: fontFamily === f.key }"
            @click.stop="fontFamily = f.key"
          >
            <text class="font-label">{{ f.label }}</text>
          </view>
        </view>
      </view>

      <!-- 翻页动画 -->
      <view class="setting-row page-turn-row">
        <text class="setting-label">翻页</text>
        <view class="page-turn-options">
          <view
            v-for="p in pageTurnList"
            :key="p.key"
            class="page-turn-option"
            :class="{ active: pageTurn === p.key }"
            @click.stop="pageTurn = p.key"
          >
            <text class="page-turn-label">{{ p.label }}</text>
          </view>
        </view>
      </view>

      <!-- 主题 -->
      <view class="setting-row theme-row">
        <text class="setting-label">主题</text>
        <view class="theme-btns">
          <view
            v-for="t in themeList"
            :key="t.key"
            class="theme-btn"
            :class="{ active: theme === t.key }"
            :style="{ background: t.color }"
            @click.stop="theme = t.key"
          >
            <text class="theme-label" :style="{ color: t.key === 'light' ? '#333' : '#fff' }">{{ t.label }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部工具栏 -->
    <view class="bottom-bar" :class="{ show: showUI }" @click.stop>
      <view class="progress-section">
        <slider
          class="chapter-slider"
          :value="chapterProgress"
          min="0"
          max="100"
          @change="onProgressChange"
          block-size="12"
        />
        <text class="progress-text">{{ currentChapterIndex + 1 }} / {{ chapters.length }} 章</text>
      </view>
      <view class="bottom-actions">
        <view class="action-btn" @click.stop="prevChapter">
          <text class="action-icon">‹</text>
          <text class="action-text">上一章</text>
        </view>
        <view class="action-btn" @click.stop="showSettings = !showSettings">
          <text class="action-icon">Aa</text>
          <text class="action-text">设置</text>
        </view>
        <view class="action-btn" @click.stop="togglePlay">
          <text class="action-icon">{{ isPlaying ? '⏸' : '▶' }}</text>
          <text class="action-text">{{ isPlaying ? '暂停' : '听书' }}</text>
        </view>
        <view class="action-btn" @click.stop="showCatalog">
          <text class="action-icon">☰</text>
          <text class="action-text">目录</text>
        </view>
        <view class="action-btn" @click.stop="nextChapter">
          <text class="action-icon">›</text>
          <text class="action-text">下一章</text>
        </view>
      </view>
    </view>

    <!-- 广告播放弹窗 -->
    <view class="ad-modal" v-if="showAdModal" @click="closeAd">
      <view class="ad-content" @click.stop>
        <view class="ad-header">
          <text class="ad-title">广告播放中</text>
          <text class="ad-close" @click.stop="closeAd">×</text>
        </view>
        <view class="ad-body">
          <text class="ad-brand">🎮 刺激战场 - 全新S36赛季已开启</text>
          <text class="ad-desc">立即下载，领取限定武器皮肤！全新地图、新模式上线！</text>
          <view class="ad-progress-bar">
            <view class="ad-progress-fill" :style="{ width: adProgress + '%' }"></view>
          </view>
          <text class="ad-progress-text">{{ adProgress < 100 ? '广告播放中 ' + adProgress + '%' : '广告播放完成，解锁中...' }}</text>
        </view>
      </view>
    </view>

    <!-- 章节快速选择 -->
    <view class="catalog-modal" v-if="showCatalogPanel" @click="showCatalogPanel = false">
      <view class="catalog-content" @click.stop>
        <text class="catalog-title">章节目录</text>
        <scroll-view scroll-y class="catalog-list">
          <view
            v-for="ch in chapters"
            :key="ch.id"
            class="catalog-item"
            :class="{ active: ch.id === chapterId }"
            @click.stop="selectChapter(ch)"
          >
            <text class="catalog-item-title">{{ ch.title }}</text>
            <text v-if="!ch.isFree" class="catalog-lock">锁</text>
            <text v-else class="catalog-free">免费</text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, onUnmounted, watch } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchBook, fetchChapters, fetchChapter, checkShelf, addToShelf, removeFromShelf, markChapterRead, saveProgress, type Book, type Chapter } from '@/api/book';
import { fetchReaderConfig, saveReaderConfig } from '@/api/user';
import { adComplete, adUnlock } from '@/api/ad';

type ThemeKey = 'dark' | 'light' | 'sepia' | 'green';

const themeList = [
  { key: 'dark' as ThemeKey, label: '夜间', color: '#F5F0EA' },
  { key: 'light' as ThemeKey, label: '日间', color: '#F5F5F0' },
  { key: 'sepia' as ThemeKey, label: '羊皮纸', color: '#F4ECD8' },
  { key: 'green' as ThemeKey, label: '护眼', color: '#C7EDCC' },
];

const themeVars: Record<ThemeKey, Record<string, string>> = {
  dark: {
    '--page-bg': '#F5F0EA',
    '--text-primary': '#2C2C2C',
    '--text-title': '#2C2C2C',
    '--panel-bg': 'rgba(255,255,255,0.95)',
    '--border-color': '#E8E2D8',
    '--accent': '#D49435',
    '--text-secondary': '#888888',
    '--text-muted': '#AAAAAA',
  },
  light: {
    '--page-bg': '#FAF8F5',
    '--text-primary': '#333333',
    '--text-title': '#1a1a1a',
    '--panel-bg': 'rgba(255,255,255,0.95)',
    '--border-color': '#e0e0d8',
    '--accent': '#FF8C00',
    '--text-secondary': '#666666',
    '--text-muted': '#999999',
  },
  sepia: {
    '--page-bg': '#F4ECD8',
    '--text-primary': '#5B4B3A',
    '--text-title': '#3D2B1F',
    '--panel-bg': 'rgba(244,236,216,0.95)',
    '--border-color': '#D9CBA0',
    '--accent': '#E8A23E',
    '--text-secondary': '#7A6B5A',
    '--text-muted': '#A09080',
  },
  green: {
    '--page-bg': '#C7EDCC',
    '--text-primary': '#2F4F2F',
    '--text-title': '#1A3A1A',
    '--panel-bg': 'rgba(199,237,204,0.95)',
    '--border-color': '#A8D8B0',
    '--accent': '#4CAF50',
    '--text-secondary': '#4A7A4A',
    '--text-muted': '#6A9A6A',
  },
};

const book = ref<Partial<Book>>({});
const chapters = ref<Chapter[]>([]);
const chapterId = ref(1);
const fontSize = ref(32);
const theme = ref<ThemeKey>('dark');
const lineHeight = ref(1.8);
const paragraphGap = ref(24);
const brightness = ref(100);
const fontFamily = ref('system');
const pageTurn = ref('slide');

const fontList = [
  { key: 'system', label: '系统默认' },
  { key: 'serif', label: '思源宋体' },
  { key: 'sans', label: '思源黑体' },
  { key: 'mono', label: '等宽字体' },
];

const pageTurnList = [
  { key: 'slide', label: '滑动' },
  { key: 'cover', label: '覆盖' },
  { key: 'sim', label: '仿真' },
  { key: 'none', label: '无' },
];

const themeStyle = computed(() => ({
  ...themeVars[theme.value],
  ...(brightness.value !== 100 ? { filter: `brightness(${brightness.value}%)` } : {}),
}));

const showUI = ref(false);
const showSettings = ref(false);
const showCatalogPanel = ref(false);
const showAdModal = ref(false);
const adProgress = ref(0);
const adUnlocking = ref(false);
const isLoading = ref(false);
const contentOpacity = ref(1);
const scrollTop = ref(0);
const pageAnimClass = ref('');

/* ========== 连续流阅读核心状态 ========== */
interface ChapterBlock {
  id: number;
  title: string;
  content: string;
  locked: boolean;
  paragraphs: string[];
}

const blocks = ref<ChapterBlock[]>([]);
const currentVisibleChapterId = ref(1);
const isLoadingMore = ref(false);
const isLoadingPrev = ref(false);
const scrollIntoView = ref('');
const inShelf = ref(false);
const isPlaying = ref(false);
const readChapterIds = ref<Set<number>>(new Set());
let progressTimer: any = null;
let configSaveTimer: any = null;
const allLoaded = ref(false);
const pendingUnlockChapterId = ref<number | null>(null);
let adTimer: any = null;
const preloadCache = ref<Map<number, any>>(new Map());
let isRestoringScroll = false;
const currentParagraphIndex = ref(0);

/* ========== 计算属性 ========== */
const currentBlockTitle = computed(() => {
  const block = blocks.value.find(b => b.id === currentVisibleChapterId.value);
  return block?.title || '';
});

const currentChapterIndex = computed(() =>
  chapters.value.findIndex(c => c.id === currentVisibleChapterId.value)
);

const chapterProgress = computed(() => {
  if (chapters.value.length === 0) return 0;
  return Math.round(((currentChapterIndex.value + 1) / chapters.value.length) * 100);
});

const showPullHint = computed(() => {
  if (!blocks.value.length) return false;
  const firstId = blocks.value[0].id;
  const idx = chapters.value.findIndex(c => c.id === firstId);
  return idx > 0 && !isLoadingPrev.value;
});

/* ========== 生命周期 ========== */
onLoad(async (opts: any) => {
  const bid = parseInt(opts?.bookId || '1');
  const cid = parseInt(opts?.chapterId || '1');
  chapterId.value = cid;
  currentVisibleChapterId.value = cid;

  try {
    const [b, chs, shelfRes, config] = await Promise.all([
      fetchBook(bid),
      fetchChapters(bid),
      checkShelf(bid).catch(() => ({ inShelf: false })),
      fetchReaderConfig().catch(() => null),
    ]);
    if (b) book.value = b;
    if (chs) chapters.value = chs;
    inShelf.value = shelfRes.inShelf;

    // 加载用户配置
    if (config) {
      if (config.fontSize) fontSize.value = config.fontSize;
      if (config.lineHeight) lineHeight.value = config.lineHeight / 100;
      if (config.theme) theme.value = config.theme as ThemeKey;
      if (config.brightness) brightness.value = config.brightness;
    }

    // 如果 URL 明确指定了 chapterId（从目录/详情页主动跳转），优先使用它，不恢复进度
    let targetId = cid;
    let restoredParagraphIndex = 0;
    if (!opts?.chapterId) {
      const restored = await restoreProgress(bid);
      if (restored) {
        targetId = restored.chapterId;
        restoredParagraphIndex = restored.paragraphIndex;
      }
    }

    chapterId.value = targetId;
    currentVisibleChapterId.value = targetId;
    await loadChapter(targetId);

    // 恢复到具体段落
    if (restoredParagraphIndex > 0) {
      scrollToParagraph(targetId, restoredParagraphIndex);
    }

    // 启动进度上报定时器
    progressTimer = setInterval(() => {
      if (book.value.id && currentVisibleChapterId.value) {
        const container = getScrollContainer();
        const progress = container
          ? Math.min(1, Math.max(0, container.scrollTop / (container.scrollHeight - container.clientHeight)))
          : 0;
        saveProgress(book.value.id, currentVisibleChapterId.value, progress, currentParagraphIndex.value).catch(() => {});
      }
    }, 30000); // 30s 上报一次
  } catch (e) {
    console.error('fetch reader failed', e);
  }
});

onUnmounted(() => {
  if (progressTimer) clearInterval(progressTimer);
  if (configSaveTimer) clearTimeout(configSaveTimer);
});

// 监听配置变化，自动保存
watch([fontSize, lineHeight, theme, brightness], () => {
  debounceSaveConfig();
}, { deep: true });

// H5 下 scroll-view 内部 Vue @click 不生效，用原生监听器补充
// 注意：监听器绑定在 .reader-content 而非 .reader-inner，避免 contentKey 变化导致 inner 重建后事件丢失
onMounted(() => {
  // #ifdef H5
  setTimeout(() => {
    const content = document.querySelector('.reader-content');
    if (content) {
      content.addEventListener('click', (e) => {
        const target = e.target as HTMLElement;
        // 只处理点击在 reader-inner 内部的事件，且排除交互元素
        if (target.closest('.reader-inner') && !target.closest('.unlock-ad-btn')) {
          onPageClick(e as any);
        }
      });
    }
  }, 500);
  // #endif
});

/* ========== 章节加载 ========== */
function makeBlock(data: any): ChapterBlock {
  return {
    id: data.id,
    title: data.title || '加载中...',
    content: data.content || '',
    locked: data.locked,
    paragraphs: data.content ? data.content.split('\n\n') : [],
  };
}

async function loadChapter(id: number, direction: 'prev' | 'next' | 'init' = 'init') {
  isLoading.value = true;
  allLoaded.value = true; // 先阻止触底加载，避免空内容触发底部事件
  blocks.value = [];      // 清空内容，v-if 会链式销毁 reader-inner，彻底清除 scroll 状态
  contentOpacity.value = 0;

  try {
    // 等待 reader-inner 销毁，scroll-view 内部滚动状态彻底清除
    await nextTick();

    // 确保滚动归零
    scrollTop.value = 0;
    // #ifdef H5
    const container = getScrollContainer();
    if (container) container.scrollTop = 0;
    // #endif

    const data = await fetchChapter(id);
    blocks.value = [makeBlock(data)];
    chapterId.value = id;
    currentVisibleChapterId.value = id;

    // 等待新的 reader-inner 创建渲染
    await nextTick();

    // 再次确保滚动归零（新内容渲染后 scroll-view 可能自动滚动）
    scrollTop.value = direction === 'init' ? 2 : 0;
    // #ifdef H5
    if (container) container.scrollTop = direction === 'init' ? 2 : 0;
    // #endif

    allLoaded.value = false; // 恢复触底加载
    contentOpacity.value = 1;
    applyPageAnim(direction);
    saveReadingProgress();
    markCurrentChapterRead();

    // 初始化时加载下一章到连续流（仅限免费章节），让用户一进来就能连续滚动
    const idx = chapters.value.findIndex(c => c.id === id);
    if (idx >= 0 && idx < chapters.value.length - 1) {
      const nextCh = chapters.value[idx + 1];
      if (nextCh.isFree) {
        try {
          const nextData = await fetchChapter(nextCh.id);
          blocks.value.push(makeBlock(nextData));
        } catch (e) { /* ignore */ }
      } else {
        preloadChapter(nextCh.id);
      }
    }
  } catch (e) {
    console.error('load chapter failed', e);
  } finally {
    isLoading.value = false;
  }
}

async function preloadChapter(id: number) {
  if (preloadCache.value.has(id)) return;
  try {
    const data = await fetchChapter(id);
    preloadCache.value.set(id, data);
  } catch (e) { /* ignore */ }
}

function getCachedChapter(id: number) {
  return preloadCache.value.get(id);
}

function trimBlocks() {
  // 滑动窗口：以当前可视章为中心，最多保留 7 章
  const maxWindow = 7;
  if (blocks.value.length <= maxWindow) return;
  const centerIdx = blocks.value.findIndex(b => b.id === currentVisibleChapterId.value);
  if (centerIdx < 0) return;
  const halfAhead = 2;  // 当前章之前最多保留 2 章
  const halfBehind = 4; // 当前章之后最多保留 4 章
  let start = Math.max(0, centerIdx - halfAhead);
  let end = Math.min(blocks.value.length, start + maxWindow);
  if (end - start < maxWindow) {
    start = Math.max(0, end - maxWindow);
  }
  blocks.value = blocks.value.slice(start, end);
}

async function jumpToChapter(id: number, direction: 'prev' | 'next' | 'init' = 'init') {
  // 如果目标章节已在连续流中，直接滚动过去
  const existingIdx = blocks.value.findIndex(b => b.id === id);
  if (existingIdx >= 0) {
    scrollIntoView.value = '';
    await nextTick();
    scrollIntoView.value = 'ch-' + id;
    currentVisibleChapterId.value = id;
    chapterId.value = id;
    saveReadingProgress();
    markCurrentChapterRead();
    return;
  }
  // 不在当前窗口（跳得太远），回退到清空重载
  await loadChapter(id, direction);
}

function applyPageAnim(direction: 'prev' | 'next' | 'init') {
  if (pageTurn.value === 'none' || direction === 'init') {
    pageAnimClass.value = '';
    return;
  }
  const animMap: Record<string, Record<string, string>> = {
    slide: { next: 'anim-slide-left', prev: 'anim-slide-right' },
    cover: { next: 'anim-cover-in', prev: 'anim-cover-out' },
    sim: { next: 'anim-sim-right', prev: 'anim-sim-left' },
  };
  const targetClass = animMap[pageTurn.value]?.[direction] || '';
  // 先清空再赋值，确保 Vue 检测到变化从而触发 CSS 动画
  pageAnimClass.value = '';
  requestAnimationFrame(() => {
    pageAnimClass.value = targetClass;
  });
  // 800ms 后清除动画类，避免后续滚动触发重复动画
  setTimeout(() => { pageAnimClass.value = ''; }, 800);
}

/* ========== 连续流：触顶追加上一章 ========== */
async function onScrollToUpper() {
  if (isLoadingPrev.value || isLoadingMore.value) return;

  const firstBlock = blocks.value[0];
  if (!firstBlock) return;

  const idx = chapters.value.findIndex(c => c.id === firstBlock.id);
  if (idx <= 0) return; // 已经是第一章

  isLoadingPrev.value = true;
  const prevCh = chapters.value[idx - 1];

  try {
    const cached = getCachedChapter(prevCh.id);
    const data = cached || await fetchChapter(prevCh.id);

    // #ifdef H5
    const container = getScrollContainer();
    const oldScrollTop = container ? container.scrollTop : 0;
    // #endif

    blocks.value.unshift(makeBlock(data));

    // 等待 DOM 更新后恢复滚动位置（保持视觉不跳动）
    await nextTick();
    // #ifdef H5
    if (container) {
      const newFirstEl = container.querySelector('.chapter-block') as HTMLElement;
      if (newFirstEl) {
        const delta = newFirstEl.offsetHeight + 20; // 新章节高度 + 间距
        container.scrollTop = oldScrollTop + delta;
        scrollTop.value = container.scrollTop;
      }
    }
    // #endif

    // 预加载上上一章
    if (idx - 2 >= 0) {
      preloadChapter(chapters.value[idx - 2].id);
    }
  } catch (e) {
    console.error('load prev chapter failed', e);
  } finally {
    isLoadingPrev.value = false;
  }
}

/* ========== 连续流：触底追加下一章 ========== */
async function onScrollToLower() {
  if (isLoadingMore.value || isLoadingPrev.value || allLoaded.value) return;

  const lastBlock = blocks.value[blocks.value.length - 1];
  if (!lastBlock || lastBlock.locked) return;

  const idx = chapters.value.findIndex(c => c.id === lastBlock.id);
  if (idx < 0 || idx >= chapters.value.length - 1) {
    allLoaded.value = true;
    return;
  }

  isLoadingMore.value = true;
  const nextCh = chapters.value[idx + 1];

  try {
    const cached = getCachedChapter(nextCh.id);
    const data = cached || await fetchChapter(nextCh.id);
    blocks.value.push(makeBlock(data));

    // 预加载下下一章
    if (idx + 2 < chapters.value.length) {
      preloadChapter(chapters.value[idx + 2].id);
    }
  } catch (e) {
    console.error('append chapter failed', e);
  } finally {
    isLoadingMore.value = false;
    trimBlocks();
  }
}

/* ========== 滚动追踪当前章节 ========== */
let scrollTimer: any = null;

function onScroll(e: any) {
  if (isSmoothScrolling || isRestoringScroll) return; // 平滑滚动/位置恢复期间屏蔽，避免与 :scroll-top 绑定冲突
  scrollTop.value = e.detail.scrollTop;
  if (scrollTimer) clearTimeout(scrollTimer);
  scrollTimer = setTimeout(() => {
    updateVisibleChapter();
  }, 150);
}

function updateVisibleChapter() {
  if (blocks.value.length === 0) return;

  // #ifdef H5
  try {
    const container = getScrollContainer();
    if (!container) return;
    const st = container.scrollTop;
    const blockEls = document.querySelectorAll('.chapter-block');

    for (let i = blockEls.length - 1; i >= 0; i--) {
      const el = blockEls[i] as HTMLElement;
      if (el.offsetTop <= st + 200) {
        const id = parseInt(el.getAttribute('data-id') || '1');
        let changed = false;
        if (currentVisibleChapterId.value !== id) {
          currentVisibleChapterId.value = id;
          chapterId.value = id;
          changed = true;
        }
        // 计算当前章节内的段落索引
        const paraEls = el.querySelectorAll('.para');
        let pIdx = 0;
        for (let j = 0; j < paraEls.length; j++) {
          const pEl = paraEls[j] as HTMLElement;
          if (pEl.offsetTop + pEl.offsetHeight / 2 > st + 100) {
            pIdx = j;
            break;
          }
          pIdx = j;
        }
        if (currentParagraphIndex.value !== pIdx) {
          currentParagraphIndex.value = pIdx;
          changed = true;
        }
        if (changed) saveReadingProgress();
        break;
      }
    }
  } catch (e) {}
  // #endif
}

/* ========== 广告解锁 ========== */
async function startAdUnlock(blockChapterId?: number) {
  // 未登录用户先提示登录
  const token = uni.getStorageSync('token');
  if (!token) {
    uni.showModal({
      title: '需要登录',
      content: '解锁章节需要先登录账号',
      confirmText: '去登录',
      success: (res) => {
        if (res.confirm) uni.navigateTo({ url: '/pages/me/me' });
      },
    });
    return;
  }
  pendingUnlockChapterId.value = blockChapterId || chapterId.value;
  showAdModal.value = true;
  adProgress.value = 0;
  const total = 3000;
  const step = 100;
  let elapsed = 0;
  if (adTimer) clearInterval(adTimer);
  adTimer = setInterval(() => {
    elapsed += step;
    adProgress.value = Math.min(100, Math.floor((elapsed / total) * 100));
    if (elapsed >= total) {
      clearInterval(adTimer);
      adTimer = null;
      finishAd();
    }
  }, step);
}

async function finishAd() {
  adUnlocking.value = true;
  if (adTimer) { clearInterval(adTimer); adTimer = null; }
  try {
    const tokenRes = await adComplete();
    console.log('[finishAd] adComplete result:', JSON.stringify(tokenRes));
    if (!tokenRes.token) throw new Error('no token');
    const targetId = pendingUnlockChapterId.value || chapterId.value;
    const res = await adUnlock(targetId, tokenRes.token);
    console.log('[finishAd] adUnlock result:', JSON.stringify(res));
    if (res.success) {
      showAdModal.value = false;
      uni.showToast({ title: '解锁成功', icon: 'success' });

      // 同步更新 chapters 数组锁定状态（用 splice 确保响应式）
      const chIdx = chapters.value.findIndex(c => c.id === targetId);
      if (chIdx >= 0) {
        chapters.value.splice(chIdx, 1, { ...chapters.value[chIdx], isFree: true });
      }

      const idx = blocks.value.findIndex(b => b.id === targetId);
      console.log('[finishAd] targetId:', targetId, 'block idx:', idx);
      if (idx >= 0) {
        const data = await fetchChapter(targetId);
        console.log('[finishAd] fetchChapter result:', {id: data.id, locked: data.locked, hasContent: !!data.content});
        // 如果后端仍然返回 locked，说明 token 失效或后端异常
        if (data.locked) {
          throw new Error('chapter still locked after unlock');
        }
        blocks.value.splice(idx, 1, makeBlock(data));
      } else {
        await loadChapter(targetId);
      }
    } else {
      uni.showToast({ title: res.error || '解锁失败', icon: 'none' });
    }
  } catch (e: any) {
    console.error('[finishAd] error:', e);
    const msg = e.message || '';
    if (msg.includes('Unauthorized') || msg.includes('401') || msg.includes('403') || msg.includes('chapter still locked')) {
      uni.showModal({
        title: '登录已过期',
        content: '请重新登录后再试',
        showCancel: false,
        success: () => uni.navigateTo({ url: '/pages/me/me' })
      });
    } else {
      uni.showToast({ title: e.message || '解锁失败', icon: 'none' });
    }
  } finally {
    adUnlocking.value = false;
    pendingUnlockChapterId.value = null;
  }
}

function closeAd() {
  showAdModal.value = false;
  adProgress.value = 0;
  if (adTimer) { clearInterval(adTimer); adTimer = null; }
}

/* ========== UI 交互 ========== */
const toggleUI = () => {
  if (showSettings.value) {
    showSettings.value = false;
    return;
  }
  showUI.value = !showUI.value;
};

/* 左右点击翻页 */
function onPageClick(e: any) {
  // 如果弹窗或目录打开，不处理
  if (showAdModal.value || showCatalogPanel.value) return;

  // uni-app H5 点击事件可能有多种形式
  const x = e.detail?.x ?? e.clientX ?? e.pageX ?? 0;
  const { windowWidth } = uni.getSystemInfoSync();

  if (x < windowWidth * 0.3) {
    // 左侧 30% → 上一屏
    scrollByPage(-1);
  } else if (x > windowWidth * 0.7) {
    // 右侧 30% → 下一屏
    scrollByPage(1);
  } else {
    // 中间 40% → 切换菜单
    toggleUI();
  }
}

function getScrollContainer(): HTMLElement | null {
  // #ifdef H5
  const reader = document.querySelector('.reader-content');
  if (!reader) return null;
  return reader.querySelector('div[style*="overflow: hidden auto"]') as HTMLElement || null;
  // #endif
  // #ifndef H5
  return null;
  // #endif
}

let isSmoothScrolling = false;

function smoothScrollTo(container: HTMLElement, target: number, duration = 280) {
  if (isSmoothScrolling) return;
  isSmoothScrolling = true;
  const start = container.scrollTop;
  const distance = target - start;
  const startTime = performance.now();

  function step(currentTime: number) {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    // ease-out cubic
    const ease = 1 - Math.pow(1 - progress, 3);
    container.scrollTop = start + distance * ease;
    if (progress < 1) {
      requestAnimationFrame(step);
    } else {
      isSmoothScrolling = false;
      scrollTop.value = container.scrollTop;
    }
  }
  requestAnimationFrame(step);
}

function scrollToParagraph(chapterId: number, paragraphIndex: number) {
  // #ifdef H5
  try {
    const container = getScrollContainer();
    if (!container) return;
    const paraEl = document.getElementById('para-' + chapterId + '-' + paragraphIndex);
    if (paraEl) {
      const containerRect = container.getBoundingClientRect();
      const paraRect = paraEl.getBoundingClientRect();
      const target = container.scrollTop + paraRect.top - containerRect.top - 40;
      smoothScrollTo(container, Math.max(0, target), 300);
    }
  } catch (e) {}
  // #endif
}

function scrollByPage(direction: number) {
  // #ifdef H5
  try {
    const container = getScrollContainer();
    if (!container) return;

    if (direction > 0) {
      // 向下翻页
      const maxScroll = container.scrollHeight - container.clientHeight;
      if (container.scrollTop >= maxScroll - 20) {
        // 已经在底部，切换下一章（带动画）
        nextChapter();
      } else {
        smoothScrollTo(
          container,
          container.scrollTop + container.clientHeight * 0.98
        );
      }
    } else {
      // 向上翻页
      if (container.scrollTop <= 20) {
        // 已经在顶部，切换上一章（带动画）
        prevChapter();
      } else {
        smoothScrollTo(
          container,
          Math.max(0, container.scrollTop - container.clientHeight * 0.98)
        );
      }
    }
  } catch (e) {}
  // #endif
}

const prevChapter = () => {
  const idx = chapters.value.findIndex(c => c.id === currentVisibleChapterId.value);
  if (idx > 0) {
    const prev = chapters.value[idx - 1];
    jumpToChapter(prev.id, 'prev');
    if (idx - 2 >= 0) preloadChapter(chapters.value[idx - 2].id);
  }
};

const nextChapter = () => {
  const idx = chapters.value.findIndex(c => c.id === currentVisibleChapterId.value);
  if (idx >= 0 && idx < chapters.value.length - 1) {
    const next = chapters.value[idx + 1];
    jumpToChapter(next.id, 'next');
    if (idx + 2 < chapters.value.length) preloadChapter(chapters.value[idx + 2].id);
  }
};

const showCatalog = () => {
  showCatalogPanel.value = true;
};

const selectChapter = (ch: Chapter) => {
  showCatalogPanel.value = false;
  showUI.value = false;
  showSettings.value = false;
  const currentIdx = chapters.value.findIndex(c => c.id === currentVisibleChapterId.value);
  const targetIdx = chapters.value.findIndex(c => c.id === ch.id);
  const dir = targetIdx > currentIdx ? 'next' : targetIdx < currentIdx ? 'prev' : 'init';
  jumpToChapter(ch.id, dir);
};

const onProgressChange = (e: any) => {
  const pct = e.detail.value / 100;
  const idx = Math.round(pct * (chapters.value.length - 1));
  const ch = chapters.value[idx];
  if (ch) {
    jumpToChapter(ch.id);
  }
};

/* ========== 进度保存/恢复 ========== */
async function restoreProgress(bookId: number) {
  const token = uni.getStorageSync('token');
  if (!token) return null;
  try {
    const res: any = await uni.request({
      url: '/api/books/' + bookId + '/progress',
      header: { Authorization: 'Bearer ' + token },
    });
    const data = res.data;
    if (data && data.chapterId) {
      return { chapterId: data.chapterId, paragraphIndex: data.paragraphIndex || 0 };
    }
  } catch (e) {}
  return null;
}

async function saveReadingProgress() {
  const token = uni.getStorageSync('token');
  if (!token || !book.value.id) return;
  const bodyData = {
    chapterId: chapterId.value || 1,
    progress: (chapterProgress.value || 0) / 100,
    paragraphIndex: currentParagraphIndex.value || 0,
  };
  try {
    await uni.request({
      url: '/api/books/' + book.value.id + '/progress',
      method: 'POST',
      header: {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json'
      },
      data: bodyData,
    });
  } catch (e) {}
}

/* ========== 书架 ========== */
async function toggleShelf() {
  if (!book.value.id) return;
  const token = uni.getStorageSync('token');
  if (!token) {
    uni.showModal({
      title: '需要登录',
      content: '登录后即可加入书架',
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
}

/* ========== 配置自动保存 ========== */
function debounceSaveConfig() {
  if (configSaveTimer) clearTimeout(configSaveTimer);
  configSaveTimer = setTimeout(() => {
    saveReaderConfig({
      fontSize: fontSize.value,
      lineHeight: Math.round(lineHeight.value * 100),
      theme: theme.value,
      brightness: brightness.value,
    }).catch(() => {});
  }, 1000);
}

/* ========== 听书 ========== */
function togglePlay() {
  isPlaying.value = !isPlaying.value;
  if (isPlaying.value) {
    // 开始播放当前章节内容
    const currentBlock = blocks.value.find(b => b.id === currentVisibleChapterId.value);
    if (currentBlock) {
      startTTS(currentBlock.content);
    }
  } else {
    stopTTS();
  }
}

let ttsUtterance: SpeechSynthesisUtterance | null = null;
function startTTS(text: string) {
  // #ifdef H5
  if ('speechSynthesis' in window) {
    stopTTS();
    ttsUtterance = new SpeechSynthesisUtterance(text);
    ttsUtterance.lang = 'zh-CN';
    ttsUtterance.onend = () => { isPlaying.value = false; };
    window.speechSynthesis.speak(ttsUtterance);
  }
  // #endif
}
function stopTTS() {
  // #ifdef H5
  if ('speechSynthesis' in window) {
    window.speechSynthesis.cancel();
  }
  // #endif
}

/* ========== 章节已读 ========== */
function markCurrentChapterRead() {
  const cid = currentVisibleChapterId.value;
  if (cid && !readChapterIds.value.has(cid)) {
    readChapterIds.value.add(cid);
    markChapterRead(cid).catch(() => {});
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: var(--page-bg);
  color: var(--text-primary);
  transition: background 0.3s ease, color 0.3s ease;
}

/* 顶部工具栏 */
.top-bar {
  position: fixed;
  top: -120rpx;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 48rpx 32rpx 16rpx;
  padding-top: calc(48rpx + env(safe-area-inset-top));
  background: var(--panel-bg);
  backdrop-filter: blur(10rpx);
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transition: top 0.3s ease, opacity 0.3s ease, background 0.3s ease, visibility 0.3s ease;
  z-index: 100;
}
.top-bar.show {
  top: 0;
  opacity: 1;
  visibility: visible;
  pointer-events: auto;
}
.back {
  font-size: 36rpx;
  color: var(--text-primary);
  min-width: 60rpx;
}
.book-title {
  font-size: 28rpx;
  color: var(--text-primary);
  text-align: center;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0 16rpx;
}
.more {
  font-size: 36rpx;
  color: var(--text-primary);
  min-width: 60rpx;
  text-align: right;
}
.top-actions {
  display: flex;
  align-items: center;
  gap: 24rpx;
}
.shelf-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  color: var(--accent);
}
.shelf-icon {
  width: 32rpx;
  height: 32rpx;
}
.shelf-text {
  font-size: 20rpx;
  color: var(--accent);
  white-space: nowrap;
}

/* 阅读内容区 */
.reader-content {
  height: 100vh;
  padding: 32rpx;
  padding-top: calc(32rpx + env(safe-area-inset-top));
  padding-bottom: 40rpx;
  box-sizing: border-box;
  transition: padding-bottom 0.3s ease;
}
.reader-content.has-menu {
  padding-bottom: 320rpx;
}
.reader-inner {
  min-height: calc(100vh + 2px);
}
.chapter-block {
  /* 连续流章节块 */
}
.chapter-title {
  font-family: 'Newsreader', Georgia, 'Noto Serif SC', serif;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 32rpx;
  margin-top: 16rpx;
  text-align: center;
}
.chapter-body {
  color: var(--text-primary);
  transition: opacity 0.2s ease;
  word-break: break-word;
  overflow-wrap: break-word;
}
.para {
  display: block;
  text-indent: 2em;
  word-break: break-word;
  overflow-wrap: break-word;
}
.chapter-end {
  text-align: center;
  padding: 48rpx 0 32rpx;
  color: var(--text-muted);
  font-size: 24rpx;
  margin-bottom: 48rpx;
  border-bottom: 1rpx dashed var(--border-color);
}
.loading-more {
  text-align: center;
  padding: 32rpx;
  color: var(--text-muted);
  font-size: 26rpx;
}
.loading-prev {
  text-align: center;
  padding: 32rpx;
  color: var(--text-muted);
  font-size: 26rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}
.loading-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 3rpx solid var(--text-muted);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.loading-more {
  text-align: center;
  padding: 32rpx;
  color: var(--text-muted);
  font-size: 26rpx;
}
.pull-hint {
  text-align: center;
  padding: 24rpx 32rpx 8rpx;
  color: var(--text-muted);
  font-size: 24rpx;
  opacity: 0.6;
}
.book-end {
  text-align: center;
  padding: 64rpx 32rpx 120rpx;
  color: var(--text-muted);
  font-size: 28rpx;
}

/* 底部设置面板 */
.bottom-settings {
  position: fixed;
  bottom: -600rpx;
  left: 0;
  right: 0;
  background: var(--panel-bg);
  backdrop-filter: blur(20rpx);
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid var(--border-color);
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transition: bottom 0.3s ease, opacity 0.3s ease, visibility 0.3s ease;
  z-index: 101;
  max-height: 60vh;
  overflow-y: auto;
}
.bottom-settings.show {
  bottom: 140rpx;
  opacity: 1;
  visibility: visible;
  pointer-events: auto;
}
.setting-row {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  gap: 24rpx;
}
.setting-row.theme-row {
  padding-top: 24rpx;
}
.setting-label {
  font-size: 28rpx;
  color: var(--text-primary);
  min-width: 100rpx;
  flex-shrink: 0;
}
.setting-control {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.font-btn {
  padding: 12rpx 28rpx;
  background: var(--border-color);
  border-radius: 8rpx;
  font-size: 26rpx;
  color: var(--text-primary);
  min-width: 60rpx;
  text-align: center;
}
.font-value {
  font-size: 28rpx;
  color: var(--text-primary);
  min-width: 40rpx;
  text-align: center;
  font-weight: 500;
}
.setting-slider {
  flex: 1;
  margin: 0;
}
.theme-btns {
  display: flex;
  gap: 20rpx;
}
.font-options {
  display: flex;
  gap: 16rpx;
}
.font-option {
  padding: 12rpx 24rpx;
  border-radius: 12rpx;
  background: var(--page-bg);
  border: 2rpx solid var(--border-color);
}
.font-option.active {
  border-color: var(--accent);
  background: rgba(232,162,62,0.12);
}
.font-label {
  font-size: 24rpx;
  color: var(--text-primary);
}
.page-turn-options {
  display: flex;
  gap: 16rpx;
}
.page-turn-option {
  padding: 12rpx 24rpx;
  border-radius: 12rpx;
  background: var(--page-bg);
  border: 2rpx solid var(--border-color);
}
.page-turn-option.active {
  border-color: var(--accent);
  background: rgba(232,162,62,0.12);
}
.page-turn-label {
  font-size: 24rpx;
  color: var(--text-primary);
}
.theme-btn {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid transparent;
  transition: transform 0.2s ease;
}
.theme-btn.active {
  border-color: var(--accent);
  transform: scale(1.05);
}
.theme-label {
  font-size: 20rpx;
  font-weight: 600;
}

/* 底部工具栏 */
.bottom-bar {
  position: fixed;
  bottom: -200rpx;
  left: 0;
  right: 0;
  background: var(--panel-bg);
  backdrop-filter: blur(20rpx);
  padding: 16rpx 32rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid var(--border-color);
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transition: bottom 0.3s ease, opacity 0.3s ease, visibility 0.3s ease;
  z-index: 100;
}
.bottom-bar.show {
  bottom: 0;
  opacity: 1;
  visibility: visible;
  pointer-events: auto;
}
.progress-section {
  padding: 0 16rpx 16rpx;
}
.chapter-slider {
  margin: 0;
}
.progress-text {
  font-size: 22rpx;
  color: var(--text-muted);
  text-align: center;
  margin-top: 4rpx;
}
.bottom-actions {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding-top: 8rpx;
}
.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12rpx 24rpx;
  gap: 4rpx;
}
.action-icon {
  font-size: 32rpx;
  color: var(--text-primary);
}
.action-text {
  font-size: 22rpx;
  color: var(--text-secondary);
}

/* 锁定覆盖层 */
.lock-overlay {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 48rpx;
  min-height: 60vh;
}
.lock-icon {
  font-size: 80rpx;
  margin-bottom: 32rpx;
}
.lock-title {
  font-size: 36rpx;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 16rpx;
}
.lock-desc {
  font-size: 26rpx;
  color: var(--text-secondary);
  margin-bottom: 48rpx;
  text-align: center;
}
.unlock-ad-btn {
  background: var(--accent);
  padding: 24rpx 64rpx;
  border-radius: 48rpx;
  margin-bottom: 24rpx;
}
.unlock-ad-text {
  font-size: 30rpx;
  color: var(--text-title);
  font-weight: 600;
}
.lock-hint {
  font-size: 24rpx;
  color: var(--text-muted);
}

/* 广告弹窗 */
.ad-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
}
.ad-content {
  width: 80%;
  background: var(--panel-bg);
  border-radius: 16rpx;
  overflow: hidden;
}
.ad-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  background: var(--border-color);
}
.ad-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-title);
}
.ad-close {
  font-size: 40rpx;
  color: var(--text-secondary);
}
.ad-body {
  padding: 48rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}
.ad-brand {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-title);
  text-align: center;
}
.ad-desc {
  font-size: 26rpx;
  color: var(--text-primary);
  text-align: center;
  line-height: 1.5;
}
.ad-progress-bar {
  width: 100%;
  height: 12rpx;
  background: var(--border-color);
  border-radius: 6rpx;
  margin-top: 24rpx;
}
.ad-progress-fill {
  height: 100%;
  background: var(--accent);
  border-radius: 6rpx;
  transition: width 0.1s linear;
}
.ad-progress-text {
  font-size: 24rpx;
  color: var(--text-secondary);
  margin-top: 12rpx;
}

/* 目录弹窗 */
.catalog-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: flex-end;
  z-index: 150;
}
.catalog-content {
  width: 100%;
  max-height: 70vh;
  background: var(--panel-bg);
  border-radius: 32rpx 32rpx 0 0;
  padding: 32rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
}
.catalog-title {
  font-size: 36rpx;
  font-weight: 600;
  color: var(--text-title);
  margin-bottom: 24rpx;
  text-align: center;
}
.catalog-list {
  max-height: 55vh;
}
.catalog-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid var(--border-color);
}
.catalog-item-title {
  font-size: 30rpx;
  color: var(--text-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.catalog-lock {
  font-size: 24rpx;
  color: #E8A23E;
  padding: 4rpx 16rpx;
  background: rgba(232,162,62,0.12);
  border-radius: 8rpx;
}
.catalog-free {
  font-size: 24rpx;
  color: #4CAF50;
  padding: 4rpx 16rpx;
  background: rgba(76,175,80,0.15);
  border-radius: 8rpx;
}

/* ========== 翻页动画 ========== */

/* slide: 淡入位移 */
@keyframes slideLeftIn {
  from { opacity: 0; transform: translateX(60rpx); }
  to { opacity: 1; transform: translateX(0); }
}
@keyframes slideRightIn {
  from { opacity: 0; transform: translateX(-60rpx); }
  to { opacity: 1; transform: translateX(0); }
}
.anim-slide-left {
  animation: slideLeftIn 0.35s cubic-bezier(0.22, 1, 0.36, 1) both;
}
.anim-slide-right {
  animation: slideRightIn 0.35s cubic-bezier(0.22, 1, 0.36, 1) both;
}

/* cover: 覆盖翻页 */
@keyframes coverIn {
  from { opacity: 0; transform: translateX(100%); }
  to { opacity: 1; transform: translateX(0); }
}
@keyframes coverOut {
  from { opacity: 0; transform: translateX(-100%); }
  to { opacity: 1; transform: translateX(0); }
}
.anim-cover-in {
  animation: coverIn 0.4s cubic-bezier(0.22, 1, 0.36, 1) both;
}
.anim-cover-out {
  animation: coverOut 0.4s cubic-bezier(0.22, 1, 0.36, 1) both;
}

/* sim: 仿真翻页（3D 旋转） */
@keyframes simRight {
  from { opacity: 0; transform: perspective(1200rpx) rotateY(-25deg) translateX(80rpx); }
  to { opacity: 1; transform: perspective(1200rpx) rotateY(0) translateX(0); }
}
@keyframes simLeft {
  from { opacity: 0; transform: perspective(1200rpx) rotateY(25deg) translateX(-80rpx); }
  to { opacity: 1; transform: perspective(1200rpx) rotateY(0) translateX(0); }
}
.anim-sim-right {
  animation: simRight 0.45s cubic-bezier(0.22, 1, 0.36, 1) both;
}
.anim-sim-left {
  animation: simLeft 0.45s cubic-bezier(0.22, 1, 0.36, 1) both;
}
</style>
