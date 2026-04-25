import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export interface UserProfile {
  id: string;
  openid: string;
  nickname: string;
  avatar: string;
  coins: number;
  vipStatus: 'normal' | 'vip';
  readTime: number;
  token: string;
}

export const useUserStore = defineStore('user', () => {
  const profile = ref<UserProfile | null>(null);
  const isLoggedIn = computed(() => !!profile.value?.token);

  const setProfile = (p: UserProfile) => {
    profile.value = p;
    uni.setStorageSync('user_profile', JSON.stringify(p));
  };

  const loadFromStorage = () => {
    try {
      const raw = uni.getStorageSync('user_profile');
      if (raw) profile.value = JSON.parse(raw);
    } catch (e) {}
  };

  const clear = () => {
    profile.value = null;
    uni.removeStorageSync('user_profile');
  };

  const addCoins = (n: number) => {
    if (profile.value) {
      profile.value.coins += n;
      setProfile(profile.value);
    }
  };

  return { profile, isLoggedIn, setProfile, loadFromStorage, clear, addCoins };
});
