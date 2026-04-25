import { detectPlatform, type Platform } from './platform';
import { useUserStore } from '@/store/user';
import { login as apiLogin } from '@/api/user';

export interface LoginResult {
  token: string;
  openid: string;
  nickname?: string;
  avatar?: string;
}

export interface LoginAdapter {
  name: string;
  login(): Promise<LoginResult>;
  getUserInfo?(): Promise<Pick<LoginResult, 'nickname' | 'avatar'>>;
}

const weixinAdapter: LoginAdapter = {
  name: '微信小程序',
  async login() {
    const [err, res] = await uni.login({ provider: 'weixin' });
    if (err || !res?.code) throw new Error('微信登录失败');
    console.log('[微信登录] code:', res.code);
    const data = await apiLogin(res.code);
    uni.setStorageSync('token', data.token);
    return {
      token: data.token,
      openid: data.user.openid,
      nickname: data.user.nickname,
    };
  },
  async getUserInfo() {
    const [err, res] = await uni.getUserProfile({ desc: '用于完善用户资料' });
    if (err) throw err;
    return {
      nickname: res.userInfo.nickName || '微信用户',
      avatar: res.userInfo.avatarUrl || '',
    };
  },
};

const h5Adapter: LoginAdapter = {
  name: 'H5',
  async login() {
    console.log('[H5登录]');
    const code = 'h5_ip'; // 固定标记，让后端用 IP 作为唯一标识
    const data = await apiLogin(code);
    uni.setStorageSync('token', data.token);
    return {
      token: data.token,
      openid: data.user.openid,
      nickname: data.user.nickname,
    };
  },
};

const bilibiliAdapter: LoginAdapter = {
  name: 'B站小程序',
  async login() {
    console.log('[B站登录] 待实现');
    const code = 'bl_' + Date.now();
    const data = await apiLogin(code);
    uni.setStorageSync('token', data.token);
    return {
      token: data.token,
      openid: data.user.openid,
      nickname: data.user.nickname,
    };
  },
};

const adapters: Record<Platform, LoginAdapter> = {
  'mp-weixin': weixinAdapter,
  'h5': h5Adapter,
  'mp-bilibili': bilibiliAdapter,
  'mp-alipay': { name: '支付宝', login: async () => ({ token: '', openid: '' }) },
  'mp-toutiao': { name: '头条', login: async () => ({ token: '', openid: '' }) },
  'mp-qq': { name: 'QQ', login: async () => ({ token: '', openid: '' }) },
  'app': { name: 'App', login: async () => ({ token: '', openid: '' }) },
  'unknown': h5Adapter,
};

export async function doLogin(): Promise<boolean> {
  const platform = detectPlatform();
  const adapter = adapters[platform] || h5Adapter;
  const store = useUserStore();

  try {
    uni.showLoading({ title: '登录中...' });
    const result = await adapter.login();

    let nickname = result.nickname || '用户' + Math.floor(Math.random() * 10000);
    let avatar = result.avatar || '';

    if (adapter.getUserInfo) {
      try {
        const info = await adapter.getUserInfo();
        nickname = info.nickname || nickname;
        avatar = info.avatar || avatar;
      } catch (e) {
        console.warn('[登录] 获取用户信息失败', e);
      }
    }

    store.setProfile({
      id: result.openid,
      openid: result.openid,
      nickname,
      avatar,
      coins: 0,
      vipStatus: 'normal',
      readTime: 0,
      token: result.token,
    });

    uni.showToast({ title: '登录成功', icon: 'success' });
    return true;
  } catch (e: any) {
    uni.showToast({ title: e.message || '登录失败', icon: 'none' });
    return false;
  } finally {
    uni.hideLoading();
  }
}

export function doLogout() {
  const store = useUserStore();
  store.clear();
  uni.showToast({ title: '已退出', icon: 'none' });
}
