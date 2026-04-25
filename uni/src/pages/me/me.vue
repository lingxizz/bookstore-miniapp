<template>
  <view class="page" :style="{ backgroundColor: COLORS.bg }">
    <view class="header">
      <text class="header-label">我的</text>
      <text class="header-num">004</text>
    </view>

    <scroll-view scroll-y class="content">
      <!-- 用户卡片 -->
      <view class="user-card">
        <view class="avatar" :style="{ backgroundColor: COLORS.accent }">
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

      <!-- 账户资产 -->
      <view class="assets">
        <view class="asset-item" @click="goRecharge">
          <text class="asset-num">{{ displayCoins }}</text>
          <text class="asset-label">金币</text>
        </view>
        <view class="asset-divider" />
        <view class="asset-item">
          <text class="asset-num">{{ displayVip }}</text>
          <text class="asset-label">会员</text>
        </view>
      </view>

      <!-- 终端组件 -->
      <Terminal />

      <!-- 菜单 -->
      <view class="menu">
        <view class="menu-item" @click="goRecharge">
          <text class="menu-icon">⚡</text>
          <text class="menu-title">充值</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item">
          <text class="menu-icon">⚙️</text>
          <text class="menu-title">阅读偏好</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item">
          <text class="menu-icon">ℹ️</text>
          <text class="menu-title">关于</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>

      <Folio :num="4" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { COLORS } from '@/utils/constants';
import { useUserStore } from '@/store/user';
import { doLogin, doLogout } from '@/utils/login';
import { fetchMe } from '@/api/user';
import Terminal from '@/components/Terminal.vue';
import Folio from '@/components/Folio.vue';

const user = useUserStore();

onMounted(async () => {
  user.loadFromStorage();
  const token = uni.getStorageSync('token');
  if (token) {
    try {
      const me = await fetchMe();
      if (me) {
        user.setProfile({
          ...(user.profile || {}),
          id: String(me.id),
          openid: me.openid,
          nickname: me.nickname || '用户',
          coins: me.coins,
          token,
        } as any);
      }
    } catch (e) {
      console.error('fetch me failed', e);
    }
  }
});

const displayName = computed(() => user.profile?.nickname || '游客');
const avatarLetter = computed(() => (displayName.value.charAt(0) || 'R').toUpperCase());
const displayCoins = computed(() => user.profile?.coins ?? 0);
const displayVip = computed(() => (user.profile?.vipStatus === 'vip' ? 'VIP' : '普通'));
const displayReadTime = computed(() => user.profile?.readTime ?? 0);

const handleLogin = async () => {
  await doLogin();
};

const handleLogout = () => {
  doLogout();
};

const goRecharge = () => {
  uni.navigateTo({ url: '/pages/recharge/recharge' });
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
  padding: 0 32rpx;
}
.user-card {
  display: flex;
  align-items: center;
  padding: 32rpx 0;
  border-bottom: 1rpx solid #E8E2D8;
}
.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}
.avatar-text {
  color: #fff;
  font-size: 40rpx;
  font-weight: 700;
}
.user-info {
  flex: 1;
}
.nickname {
  font-size: 32rpx;
  font-weight: 600;
  color: #2C2C2C;
  margin-bottom: 6rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 400rpx;
}
.read-time {
  font-size: 24rpx;
  color: #888888;
}
.login-btn {
  padding: 12rpx 32rpx;
  background: #E8A23E;
  border-radius: 32rpx;
}
.login-btn text {
  color: #fff;
  font-size: 26rpx;
  font-weight: 600;
}
.logout-btn {
  padding: 12rpx 32rpx;
  background: #FFFFFF;
  border: 1rpx solid #E8E2D8;
  border-radius: 32rpx;
}
.logout-btn text {
  color: #888888;
  font-size: 26rpx;
}
.assets {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 32rpx 0;
  border-bottom: 1rpx solid #E8E2D8;
}
.asset-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}
.asset-num {
  font-size: 36rpx;
  font-weight: 700;
  color: #E8A23E;
  margin-bottom: 6rpx;
}
.asset-label {
  font-size: 24rpx;
  color: #888888;
}
.asset-divider {
  width: 1rpx;
  height: 60rpx;
  background: #E8E2D8;
}
.menu {
  margin-top: 24rpx;
}
.menu-item {
  display: flex;
  align-items: center;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #E8E2D8;
}
.menu-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}
.menu-title {
  flex: 1;
  font-size: 28rpx;
  color: #2C2C2C;
}
.menu-arrow {
  font-size: 32rpx;
  color: #AAAAAA;
}
</style>