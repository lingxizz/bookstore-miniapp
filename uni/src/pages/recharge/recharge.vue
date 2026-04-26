<template>
  <view class="page" :style="{ backgroundColor: '#F5F0EA' }">
    <!-- 顶部导航 -->
    <view class="top-bar">
      <view class="nav-back" @click="uni.navigateBack()">
        <text class="back-arrow">←</text>
        <text class="back-text">返回</text>
      </view>
      <text class="nav-title">充值中心</text>
      <view style="width: 80rpx" />
    </view>

    <!-- 余额卡片 -->
    <view class="balance-card">
      <text class="balance-label">当前余额</text>
      <view class="balance-row">
        <text class="balance-num">{{ balance }}</text>
        <text class="balance-unit">金币</text>
      </view>
    </view>

    <!-- 充值选项 -->
    <view class="section">
      <text class="section-title">选择充值金额</text>
      <view class="recharge-grid">
        <view
          class="recharge-item"
          v-for="item in rechargeOptions"
          :key="item.id"
          :class="{ active: selectedId === item.id, hot: item.hot }"
          @click="selectedId = item.id"
        >
          <text class="recharge-coins">{{ item.coins }}金币</text>
          <text class="recharge-price">¥{{ item.price }}</text>
          <text class="recharge-tag" v-if="item.bonus">+{{ item.bonus }}赠</text>
        </view>
      </view>
    </view>

    <!-- 支付方式 -->
    <view class="section">
      <text class="section-title">支付方式</text>
      <view class="pay-options">
        <view class="pay-item" :class="{ active: payMethod === 'wechat' }" @click="payMethod = 'wechat'">
          <text class="pay-icon">💚</text>
          <text class="pay-name">微信支付</text>
          <view class="pay-check" :class="{ checked: payMethod === 'wechat' }">
            <text v-if="payMethod === 'wechat'">✓</text>
          </view>
        </view>
        <view class="pay-item" :class="{ active: payMethod === 'alipay' }" @click="payMethod = 'alipay'">
          <text class="pay-icon">💙</text>
          <text class="pay-name">支付宝</text>
          <view class="pay-check" :class="{ checked: payMethod === 'alipay' }">
            <text v-if="payMethod === 'alipay'">✓</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 充值按钮 -->
    <view class="recharge-btn" @click="doRecharge">
      <text>立即充值 ¥{{ selectedPrice }}</text>
    </view>

    <!-- 说明 -->
    <view class="notice">
      <text>充值说明：1元=10金币，充值后不可退款</text>
    </view>

    <Folio :num="5" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { fetchBalance, recharge } from '@/api/book';
import Folio from '@/components/Folio.vue';

const balance = ref(0);
const selectedId = ref(2);
const payMethod = ref('wechat');

const rechargeOptions = [
  { id: 1, coins: 100, price: 10, bonus: 0 },
  { id: 2, coins: 300, price: 30, bonus: 30, hot: true },
  { id: 3, coins: 500, price: 50, bonus: 80 },
  { id: 4, coins: 1000, price: 100, bonus: 200 },
  { id: 5, coins: 2000, price: 200, bonus: 500 },
  { id: 6, coins: 5000, price: 500, bonus: 1500 },
];

const selectedPrice = computed(() => {
  const item = rechargeOptions.find(o => o.id === selectedId.value);
  return item?.price || 0;
});

onMounted(() => {
  fetchBalance().then((res: any) => {
    balance.value = res.balance || 0;
  }).catch(() => {});
});

async function doRecharge() {
  const item = rechargeOptions.find(o => o.id === selectedId.value);
  if (!item) return;

  uni.showLoading({ title: '支付中...' });
  try {
    // 模拟支付
    setTimeout(async () => {
      uni.hideLoading();
      try {
        await recharge(item.price);
        balance.value += item.coins + item.bonus;
        uni.showToast({ title: `充值成功 +${item.coins + item.bonus}金币`, icon: 'none' });
      } catch (e) {
        uni.showToast({ title: '充值失败', icon: 'none' });
      }
    }, 1500);
  } catch (e) {
    uni.hideLoading();
    uni.showToast({ title: '支付失败', icon: 'none' });
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 0 32rpx 120rpx;
  background: #F5F0EA;
}

/* 顶部导航 */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0 16rpx;
}
.nav-back {
  display: flex;
  align-items: center;
  gap: 8rpx;
  width: 120rpx;
}
.back-arrow {
  font-size: 36rpx;
  color: #E8A23E;
  font-weight: 600;
}
.back-text {
  font-size: 28rpx;
  color: #888888;
  font-family: 'Noto Sans SC', sans-serif;
}
.nav-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #2C2C2C;
  font-family: 'Noto Serif SC', serif;
}

/* 余额卡片 */
.balance-card {
  background: #E8A23E;
  border-radius: 16rpx;
  padding: 40rpx;
  margin: 16rpx 0 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.balance-label {
  font-size: 24rpx;
  color: rgba(255,255,255,0.8);
  font-family: 'Noto Sans SC', sans-serif;
  margin-bottom: 8rpx;
}
.balance-row {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}
.balance-num {
  font-size: 56rpx;
  font-weight: 700;
  color: #FFFFFF;
  font-family: 'Noto Serif SC', serif;
}
.balance-unit {
  font-size: 28rpx;
  color: rgba(255,255,255,0.8);
  font-family: 'Noto Sans SC', sans-serif;
}

/* 通用区块 */
.section {
  margin-bottom: 32rpx;
}
.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #2C2C2C;
  font-family: 'Noto Sans SC', sans-serif;
  margin-bottom: 16rpx;
}

/* 充值选项 */
.recharge-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}
.recharge-item {
  background: #FFFFFF;
  border-radius: 12rpx;
  padding: 24rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  border: 2rpx solid transparent;
  position: relative;
}
.recharge-item.active {
  border-color: #E8A23E;
  background: rgba(232,162,62,0.05);
}
.recharge-coins {
  font-size: 28rpx;
  font-weight: 600;
  color: #2C2C2C;
  font-family: 'Noto Sans SC', sans-serif;
}
.recharge-price {
  font-size: 24rpx;
  color: #E8A23E;
  font-family: 'Noto Serif SC', serif;
}
.recharge-tag {
  position: absolute;
  top: -2rpx;
  right: -2rpx;
  background: #FF5252;
  color: #fff;
  font-size: 18rpx;
  padding: 2rpx 8rpx;
  border-radius: 0 12rpx 0 8rpx;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 支付方式 */
.pay-options {
  background: #FFFFFF;
  border-radius: 12rpx;
  overflow: hidden;
}
.pay-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #F5F0EA;
}
.pay-item:last-child {
  border-bottom: none;
}
.pay-icon {
  font-size: 36rpx;
}
.pay-name {
  flex: 1;
  font-size: 28rpx;
  color: #2C2C2C;
  font-family: 'Noto Sans SC', sans-serif;
}
.pay-check {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 2rpx solid #E8E2D8;
  display: flex;
  align-items: center;
  justify-content: center;
}
.pay-check.checked {
  background: #E8A23E;
  border-color: #E8A23E;
}
.pay-check text {
  font-size: 20rpx;
  color: #fff;
}

/* 充值按钮 */
.recharge-btn {
  background: #E8A23E;
  border-radius: 48rpx;
  padding: 28rpx 0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 40rpx 0 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(232,162,62,0.3);
}
.recharge-btn text {
  font-size: 30rpx;
  color: #fff;
  font-weight: 600;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 说明 */
.notice {
  text-align: center;
  padding: 16rpx 0;
}
.notice text {
  font-size: 22rpx;
  color: #AAAAAA;
  font-family: 'Noto Sans SC', sans-serif;
}
</style>