export type Platform =
  | 'h5'
  | 'mp-weixin'
  | 'mp-bilibili'
  | 'mp-alipay'
  | 'mp-toutiao'
  | 'mp-qq'
  | 'app'
  | 'unknown';

export function detectPlatform(): Platform {
  // #ifdef H5
  return 'h5';
  // #endif

  // #ifdef MP-WEIXIN
  return 'mp-weixin';
  // #endif

  // #ifdef MP-BILIBILI
  return 'mp-bilibili';
  // #endif

  // #ifdef MP-ALIPAY
  return 'mp-alipay';
  // #endif

  // #ifdef MP-TOUTIAO
  return 'mp-toutiao';
  // #endif

  // #ifdef MP-QQ
  return 'mp-qq';
  // #endif

  // #ifdef APP-PLUS
  return 'app';
  // #endif

  try {
    const info = uni.getSystemInfoSync() as any;
    if (info.uniPlatform) {
      const p = info.uniPlatform;
      if (p === 'web' || p === 'h5') return 'h5';
      if (p === 'mp-weixin') return 'mp-weixin';
      if (p === 'mp-bilibili') return 'mp-bilibili';
      if (p === 'mp-alipay') return 'mp-alipay';
      if (p === 'mp-toutiao') return 'mp-toutiao';
      if (p === 'mp-qq') return 'mp-qq';
      if (p === 'app') return 'app';
    }
    if (info.hostName) {
      const h = info.hostName.toLowerCase();
      if (h.includes('weixin')) return 'mp-weixin';
      if (h.includes('bilibili')) return 'mp-bilibili';
    }
  } catch (e) {}

  return 'unknown';
}

export function isMiniProgram(): boolean {
  const p = detectPlatform();
  return p.startsWith('mp-');
}
