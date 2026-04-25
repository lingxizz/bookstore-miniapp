import { get, post } from '@/utils/request'

export interface User {
  id: number
  openid: string
  nickname?: string
  avatar?: string
  coins: number
}

export interface ReaderConfig {
  fontSize: number
  lineHeight: number
  theme: string
  brightness: number
}

export function login(code: string, nickname?: string, avatar?: string) {
  return post<{ token: string; user: User }>('/api/auth/login', { code, nickname, avatar })
}

export function fetchMe() {
  return get<User>('/api/users/me')
}

export function createOrder(amount: number, coinAmount: number) {
  return post<{ id: number; status: string }>('/api/orders', { amount, coinAmount })
}

export function payOrder(id: number) {
  return post(`/api/orders/${id}/pay`)
}

export function fetchReaderConfig() {
  return get<ReaderConfig>('/api/users/reader-config')
}

export function saveReaderConfig(config: Partial<ReaderConfig>) {
  return post<ReaderConfig>('/api/users/reader-config', config)
}
