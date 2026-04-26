<template>
  <view class="page" :style="{ backgroundColor: '#F8F4F0' }">
    <!-- Loading skeleton -->
    <view v-if="isLoading" class="skeleton-page">
      <view class="sk-header"></view>
      <view class="sk-user-card">
        <view class="sk-avatar"></view>
        <view class="sk-user-info">
          <view class="sk-line w50"></view>
          <view class="sk-line w70"></view>
        </view>
      </view>
      <view class="sk-assets">
        <view class="sk-asset" v-for="i in 3" :key="i">
          <view class="sk-line w60"></view>
          <view class="sk-line w40"></view>
        </view>
      </view>
      <view class="sk-menu">
        <view class="sk-menu-item" v-for="i in 5" :key="i">
          <view class="sk-line w80"></view>
        </view>
      </view>
    </view>

    <view v-else>
      <!-- Header -->
      <view class="header">
        <text class="header-label">我的</text>
        <text class="header-num">004</text>
      </view>

      <!-- User card -->
      <view class="user-card">
        <view class="avatar" :style="{ backgroundColor: '#A34A2E' }">
          <text class="avatar-text">{{ avatarLetter }}</text>
        </view>
        <view class="user-info">
          <text class="nickname">{{ displayName }}</text>
          <text class="read-time">累计阅读 {{ displayReadTime }} 分钟</text>
        </view>
        <view class="login-btn" v-if="!user.isLoggedIn" @click="handleLogin">
          <text>登录</text>
        </view>
        <view class="logout-btn" v-else @click="handleLogout">
          <text>退出</text>
        </view>
      </view>

      <!-- Assets -->
      <view class="assets">
        <view class="asset-item" @click="goRecharge">
          <text class="asset-num">{{ displayCoins }}</text>
          <text class="asset-label">金币</text>
        </view>
        <view class="asset-divider"></view>
        <view class="asset-item">
          <text class="asset-num">{{ records.length }}</text>
          <text class="asset-label">书架</text>
        </view>
        <view class="asset-divider"></view>
        <view class="asset-item">
          <text class="asset-num">{{ displayReadTime }}</text>
          <text class="asset-label">分钟</text>
        </view>
      </view>

      <!-- Menu -->
      <view class="menu">
        <view class="menu-item" v-for="item in menuList" :key="item.label" @click="item.action">
          <view class="menu-left">
            <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="#A34A2E" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path v-if="item.icon === 'history'" d="M12 8v4l3 3M3.05 11a9 9 0 1 1 2.54 5.36"/>
              <path v-else-if="item.icon === 'wallet'" d="M20 12V8H6a2 2 0 0 1-2-2c0-1.1.9-2 2-2h12v4"/>
              <path v-else-if="item.icon === 'settings'" d="M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z"/>
              <path v-else-if="item.icon === 'help'" d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"/>
              <path v-else d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            </svg>
            <text class="menu-label">{{ item.label }}</text>
          </view>
          <svg class="menu-arrow" viewBox="0 0 24 24" fill="none" stroke="#CCCCCC" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 18l6-6-6-6"/>
          </svg>
        </view>
      </view>

      <Folio :num="4" />
    </view>
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { fetchBalance, fetchShelf, h5Login } from '@/api/book';
import Folio from '@/components/Folio.vue';
import CustomTabBar from '@/components/CustomTabBar.vue';

const user = ref({ isLoggedIn: false, name: '', coins: 0, readTime: 0 });
const records = ref([]);
const isLoading = ref(true);
const firstLoad = ref(true);

const displayName = computed(() => user.value.name || '游客');
const avatarLetter = computed(() => displayName.value.charAt(0).toUpperCase());
const displayCoins = computed(() => user.value.coins || 0);
const displayReadTime = computed(() => user.value.readTime || 0);

const menuList = [
  { label: '阅读历史', icon: 'history', action: () => uni.navigateTo({ url: '/pages/history/history' }) },
  { label: '充值中心', icon: 'wallet', action: () => uni.navigateTo({ url: '/pages/recharge/recharge' }) },
  { label: '阅读设置', icon: 'settings', action: () => uni.navigateTo({ url: '/pages/settings/settings' }) },
  { label: '帮助反馈', icon: 'help', action: () => uni.navigateTo({ url: '/pages/help/help' }) },
  { label: '关于我们', icon: 'about', action: () => uni.navigateTo({ url: '/pages/about/about' }) },
];

onMounted(() => {
  isLoading.value = firstLoad.value;
  const token = uni.getStorageSync('token');
  if (token) user.value.isLoggedIn = true;
  loadData();
});

async function loadData() {
  try {
    const [balance, shelf] = await Promise.all([
      fetchBalance().catch(() => ({ balance: 0 })),
      fetchShelf().catch(() => []),
    ]);
    user.value.coins = balance.balance;
    records.value = shelf;
  } catch (e) {
    console.error('me load failed', e);
  } finally {
    isLoading.value = false;
    firstLoad.value = false;
  }
}

async function handleLogin() {
  try {
    const res = await h5Login();
    if (res.token) {
      uni.setStorageSync('token', res.token);
      user.value.isLoggedIn = true;
      user.value.name = res.user?.nickname || '读者';
      user.value.coins = res.user?.coins || 0;
      uni.showToast({ title: '登录成功', icon: 'success' });
      loadData();
    }
  } catch (e: any) {
    uni.showToast({ title: '登录失败: ' + (e.message || ''), icon: 'none' });
  }
}
function handleLogout() {
  uni.removeStorageSync('token');
  user.value.isLoggedIn = false;
  user.value.name = '';
  user.value.coins = 0;
}
function goRecharge() {
  uni.navigateTo({ url: '/pages/recharge/recharge' });
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 0 32rpx 160rpx;
  background: #F8F4F0;
}

/* Skeleton */
.skeleton-page {
  padding: 24rpx 0;
}
.sk-header {
  height: 48rpx;
  width: 120rpx;
  background: #E8E2D8;
  border-radius: 8rpx;
  margin-bottom: 24rpx;
  animation: shimmer 1.5s infinite;
}
.sk-user-card {
  display: flex;
  gap: 24rpx;
  align-items: center;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}
.sk-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: #E8E2D8;
  animation: shimmer 1.5s infinite;
}
.sk-user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.sk-line {
  height: 24rpx;
  background: #E8E2D8;
  border-radius: 8rpx;
  animation: shimmer 1.5s infinite;
}
.sk-line.w50 { width: 50%; }
.sk-line.w70 { width: 70%; }
.sk-line.w60 { width: 60%; }
.sk-line.w40 { width: 40%; }
.sk-line.w80 { width: 80%; }
.sk-assets {
  display: flex;
  justify-content: space-around;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}
.sk-asset {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  align-items: center;
}
.sk-menu {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 8rpx 24rpx;
}
.sk-menu-item {
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F5F0EA;
}
.sk-menu-item:last-child {
  border-bottom: none;
}
@keyframes shimmer {
  0% { background: #E8E2D8; }
  50% { background: #F0EBE3; }
  100% { background: #E8E2D8; }
}

/* Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding: 48rpx 0 24rpx;
}
.header-label {
  font-size: 40rpx;
  font-weight: 700;
  color: #1C1C19;
  font-family: 'Noto Serif SC', serif;
}
.header-num {
  font-size: 24rpx;
  color: #AAAAAA;
  font-family: 'Noto Serif SC', serif;
}

/* User card */
.user-card {
  display: flex;
  gap: 24rpx;
  align-items: center;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 24rpx rgba(163, 74, 46, 0.04);
  border: 1rpx solid rgba(163, 74, 46, 0.06);
}
.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.avatar-text {
  font-size: 40rpx;
  color: #FFFFFF;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
}
.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.nickname {
  font-size: 32rpx;
  font-weight: 600;
  color: #1C1C19;
  font-family: 'Noto Sans SC', sans-serif;
}
.read-time {
  font-size: 24rpx;
  color: #645D55;
  font-family: 'Noto Sans SC', sans-serif;
}
.login-btn, .logout-btn {
  padding: 12rpx 32rpx;
  border-radius: 48rpx;
  border: 1rpx solid #A34A2E;
}
.login-btn text, .logout-btn text {
  font-size: 24rpx;
  color: #A34A2E;
  font-family: 'Noto Sans SC', sans-serif;
}

/* Assets */
.assets {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 24rpx rgba(163, 74, 46, 0.04);
  border: 1rpx solid rgba(163, 74, 46, 0.06);
}
.asset-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}
.asset-num {
  font-size: 40rpx;
  font-weight: 700;
  color: #A34A2E;
  font-family: 'Noto Serif SC', serif;
}
.asset-label {
  font-size: 24rpx;
  color: #645D55;
  font-family: 'Noto Sans SC', sans-serif;
}
.asset-divider {
  width: 1rpx;
  height: 60rpx;
  background: #E8E2D8;
}

/* Menu */
.menu {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 8rpx 24rpx;
  box-shadow: 0 4rpx 24rpx rgba(163, 74, 46, 0.04);
  border: 1rpx solid rgba(163, 74, 46, 0.06);
}
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F5F0EA;
}
.menu-item:last-child {
  border-bottom: none;
}
.menu-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.menu-icon {
  width: 32rpx;
  height: 32rpx;
}
.menu-label {
  font-size: 28rpx;
  color: #1C1C19;
  font-family: 'Noto Sans SC', sans-serif;
}
.menu-arrow {
  width: 24rpx;
  height: 24rpx;
}
</style>
