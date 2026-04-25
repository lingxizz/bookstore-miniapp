import { get, post } from '@/utils/request'

export function adComplete() {
  return post<{ token: string }>('/api/ad/complete', {})
}

export function adUnlock(chapterId: number, adToken: string) {
  return post<{ success: boolean; error?: string }>('/api/ad/unlock', { chapterId, adToken })
}
