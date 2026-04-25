<template>
  <view class="page" :style="{ backgroundColor: COLORS.bg }">
    <view class="nav-back" @click="uni.navigateBack()">
      <text class="back-arrow">←</text>
      <text class="back-text">返回</text>
    </view>

    <view class="header">
      <text class="header-label">充值</text>
      <text class="balance">当前金币: {{ displayCoins }}</text>
    </view>

    <view class="plans">
      <view
        v-for="plan in plans"
        :key="plan.id"
        class="plan-card"
        :class="{ selected: selectedPlan === plan.id }"
        @click="selectedPlan = plan.id"
      >
        <text class="plan-coins">{{ plan.coins }}</text>
        <text class="plan-unit">金币</text>
        <view class="plan-divider" />
        <text class="plan-price">¥{{ plan.price }}</text>
        <text v-if="plan.bonus" class="plan-bonus">赠{{ plan.bonus }}</text>
      </view>
    </view>

    <view class="pay-btn" :class="{ active: selectedPlan }" @click="pay">
      <text>立即支付</text>
    </view>

    <view class="tips">
      <text class="tip">1. 金币仅用于解锁付费章节</text>
      <text class="tip">2. 支付成功后金币将立即到账</text>
      <text class="tip">3. 金币不可退换、不可转让</text>
    </view>

    <Folio :num="6" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { COLORS } from '@/utils/constants';
import { useUserStore } from '@/store/user';
import { createOrder, payOrder, fetchMe } from '@/api/user';
import Folio from '@/components/Folio.vue';

const userStore = useUserStore();
const displayCoins = computed(() => userStore.profile?.coins ?? 0);

const plans = [
  { id: 1, coins: 100, price: 10, bonus: 0 },
  { id: 2, coins: 300, price: 28, bonus: 30 },
  { id: 3, coins: 500, price: 45, bonus: 80 },
  { id: 4, coins: 1000, price: 88, bonus: 200 },
];

const selectedPlan = ref<number | null>(null);

const pay = async () => {
  if (!selectedPlan.value) return;
  const plan = plans.find(p => p.id === selectedPlan.value);
  if (!plan) return;
  const totalCoins = plan.coins + (plan.bonus || 0);
  uni.showModal({
    title: '确认支付',
    content: `支付¥${plan.price}，获得 ${totalCoins} 金币`,
    success: async (res: any) => {
      if (res.confirm) {
        try {
          const order = await createOrder(plan.price, totalCoins);
          await payOrder(order.id);
          const me = await fetchMe();
          if (me) {
            userStore.setProfile({
              ...(userStore.profile || {}),
              id: String(me.id),
              openid: me.openid,
              nickname: me.nickname || '用户',
              coins: me.coins,
              token: uni.getStorageSync('token'),
            } as any);
          }
          uni.showToast({ title: '充值成功', icon: 'success' });
          selectedPlan.value = null;
        } catch (e) {
          uni.showToast({ title: '支付失败', icon: 'none' });
        }
      }
    }
  });
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 48rpx 32rpx 120rpx;
}
.nav-back {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 32rpx;
}
.back-arrow {
  font-size: 36rpx;
  color: #2C2C2C;
}
.back-text {
  font-size: 28rpx;
  color: #888888;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 32rpx;
}
.header-label {
  font-size: 40rpx;
  font-weight: 700;
  color: #2C2C2C;
  letter-spacing: 4rpx;
}
.balance {
  font-size: 24rpx;
  color: #888888;
}
.plans {
  display: flex;
  gap: 24rpx;
  margin-bottom: 48rpx;
  overflow-x: auto;
  padding-bottom: 12rpx;
}
.plan-card {
  min-width: 280rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 2rpx solid #E8E2D8;
  flex-shrink: 0;
}
.plan-card.selected {
  border-color: #E8A23E;
  background: rgba(232,162,62,0.1);
}
.plan-coins {
  font-size: 48rpx;
  font-weight: 700;
  color: #2C2C2C;
}
.plan-unit {
  font-size: 24rpx;
  color: #888888;
  margin-bottom: 16rpx;
}
.plan-divider {
  width: 100%;
  height: 1rpx;
  background: #E8E2D8;
  margin-bottom: 16rpx;
}
.plan-price {
  font-size: 32rpx;
  font-weight: 600;
  color: #E8A23E;
  margin-bottom: 6rpx;
}
.plan-bonus {
  font-size: 22rpx;
  color: #4CAF50;
  background: rgba(76,175,80,0.15);
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}
.pay-btn {
  padding: 28rpx 0;
  border-radius: 12rpx;
  background: #FFFFFF;
  border: 1rpx solid #E8E2D8;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
}
.pay-btn.active {
  background: #E8A23E;
  border-color: #E8A23E;
}
.pay-btn text {
  font-size: 30rpx;
  font-weight: 600;
  color: #AAAAAA;
}
.pay-btn.active text {
  color: #fff;
}
.tips {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.tip {
  font-size: 24rpx;
  color: #AAAAAA;
}
</style>