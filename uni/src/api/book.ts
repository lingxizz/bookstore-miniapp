import { request } from '../utils/request';

export interface Book {
  id: number;
  title: string;
  author: string;
  summary: string;
  category: string;
  cover: string;
  rating: number;
  price: number;
  tags: string[];
  wordCount: number;
  chapterCount?: number;
  status: string;
  updateTime: string;
}

export interface Chapter {
  id: number;
  bookId: number;
  order: number;
  title: string;
  isFree: boolean;
  price: number;
  wordCount: number;
}

export interface ShelfItem {
  id: number;
  bookId: number;
  title: string;
  author: string;
  cover: string;
  wordCount: number;
  status: string;
  category: string;
  summary: string;
  rating: number;
  progress: number;
  readStatus: string;
  lastChapterId?: number;
  updateTime: string;
  lastReadAt?: string;
}

export interface Banner {
  id: number;
  image: string;
  title: string;
  link: string;
  type: 'book' | 'url';
}

export interface Category {
  id: number;
  name: string;
  icon: string;
  bookCount: number;
}

export interface RankItem {
  bookId: number;
  title: string;
  author: string;
  cover: string;
  category: string;
  rating: number;
  readCount: number;
  rank: number;
}

export interface AdUnlock {
  chapterId: number;
  unlockedUntil: string;
}

// 首页数据
export function fetchHomeData() {
  return request('/api/home', 'GET');
}

export function fetchBooks(): Promise<Book[]> {
  return request('/api/books', 'GET');
}

// 轮播图
export function fetchBanners(): Promise<Banner[]> {
  return request('/api/banners', 'GET');
}

// 分类列表
export function fetchCategories(): Promise<Category[]> {
  return request('/api/categories', 'GET');
}

// 今日推荐
export function fetchTodayPick(): Promise<Book[]> {
  return request('/api/books/today-pick', 'GET');
}

// 热门榜单
export function fetchHotRank(): Promise<RankItem[]> {
  return request('/api/books/hot-rank', 'GET');
}

// 猜你喜欢
export function fetchGuessLike(seed?: number): Promise<Book[]> {
  const query = seed ? `?seed=${seed}` : '';
  return request(`/api/books/guess-like${query}`, 'GET');
}

// 书城数据（分类+新书+完本+畅销）
export function fetchStoreData() {
  return request('/api/store', 'GET');
}

// 书城筛选（分类+状态+排序）
export function fetchBookFilter(params: { category?: string; status?: string; sort?: string }): Promise<Book[]> {
  const query = Object.entries(params)
    .filter(([_, v]) => v !== undefined && v !== '')
    .map(([k, v]) => `${k}=${encodeURIComponent(v!)}`)
    .join('&');
  return request(`/api/books/filter${query ? '?' + query : ''}`, 'GET');
}

// 新书速递
export function fetchNewReleases(category?: string): Promise<Book[]> {
  const query = category ? `?category=${encodeURIComponent(category)}` : '';
  return request(`/api/books/new-releases${query}`, 'GET');
}

// 完本精选
export function fetchCompleted(category?: string): Promise<Book[]> {
  const query = category ? `?category=${encodeURIComponent(category)}` : '';
  return request(`/api/books/completed${query}`, 'GET');
}

// 搜索
export function searchBooks(keyword: string): Promise<Book[]> {
  return request(`/api/books?q=${encodeURIComponent(keyword)}`, 'GET');
}

// 书籍详情
export function fetchBook(id: number): Promise<Book> {
  return request(`/api/books/${id}`, 'GET');
}

// 章节目录
export function fetchChapters(bookId: number): Promise<Chapter[]> {
  return request(`/api/books/${bookId}/chapters`, 'GET');
}

// 章节内容
export function fetchChapterContent(bookId: number, chapterId: number) {
  return request(`/api/books/${bookId}/chapters/${chapterId}`, 'GET');
}

// 书架列表
export function fetchShelf(): Promise<ShelfItem[]> {
  return request('/api/shelf', 'GET');
}

// 检查是否在书架
export function checkShelf(bookId: number): Promise<{ inShelf: boolean }> {
  return request(`/api/books/${bookId}/shelf`, 'GET');
}

// 加入书架
export function addToShelf(bookId: number) {
  return request(`/api/books/${bookId}/shelf`, 'POST');
}

// 移除书架
export function removeFromShelf(bookId: number) {
  return request(`/api/books/${bookId}/shelf`, 'DELETE');
}

// 阅读进度
export function fetchProgress(bookId: number) {
  return request(`/api/books/${bookId}/progress`, 'GET');
}

// 保存进度
export function saveProgress(bookId: number, chapterId: number, progress: number) {
  return request(`/api/books/${bookId}/progress`, 'POST', { chapterId, progress });
}

// 广告解锁
export function unlockByAd(chapterId: number, adToken: string) {
  return request('/api/ad/unlock', 'POST', { chapterId, adToken });
}

// H5 登录
export function h5Login(nickname?: string): Promise<{ token: string; user: any }> {
  return request('/api/auth/login', 'POST', { code: 'h5_login', nickname: nickname ?? '', avatar: '' });
}

// 检查解锁状态
export function checkUnlock(bookId: number, chapterId: number): Promise<{ unlocked: boolean }> {
  return request(`/api/unlock/check?bookId=${bookId}&chapterId=${chapterId}`, 'GET');
}

// 金币余额
export function fetchBalance(): Promise<{ balance: number }> {
  return request('/api/users/balance', 'GET');
}

// 充值
export function recharge(amount: number) {
  return request('/api/recharge', 'POST', { amount });
}

// 购买章节
export function buyChapter(bookId: number, chapterId: number) {
  return request('/api/purchase', 'POST', { bookId, chapterId });
}
