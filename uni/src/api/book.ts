import { get, post, del } from '@/utils/request'

export interface Book {
  id: number
  title: string
  author: string
  cover?: string
  summary?: string
  category?: string
  tags?: string
  price: number
  status: string
  wordCount: number
  rating: number
  ratingCount: number
  createdAt: string
}

export interface Chapter {
  id: number
  title: string
  order: number
  price: number
  isFree: boolean
}

export function fetchBooks(category?: string, q?: string) {
  return get<Book[]>('/api/books', { category, q })
}

export function fetchBook(id: number) {
  return get<Book>(`/api/books/${id}`)
}

export function fetchChapters(bookId: number) {
  return get<Chapter[]>(`/api/books/${bookId}/chapters`)
}

export function fetchChapter(id: number) {
  return get<any>(`/api/chapters/${id}?_t=${Date.now()}`)
}

export function unlockChapter(id: number) {
  return post(`/api/chapters/${id}/unlock`)
}

// ========== 书架 ==========
export function addToShelf(bookId: number) {
  return post(`/api/books/${bookId}/shelf`)
}

export function removeFromShelf(bookId: number) {
  return del(`/api/books/${bookId}/shelf`)
}

export function checkShelf(bookId: number) {
  return get<{ inShelf: boolean }>(`/api/books/${bookId}/shelf`)
}

export function fetchShelf() {
  return get<{ id: number; bookId: number; createdAt: string; book: Book }[]>('/api/shelf')
}

// ========== 章节已读 ==========
export function markChapterRead(chapterId: number) {
  return post(`/api/chapters/${chapterId}/read`)
}

export function fetchReadChapters(bookId: number) {
  return get<{ chapters: number[] }>(`/api/books/${bookId}/read-chapters`)
}

// ========== 阅读进度 ==========
export function fetchProgress(bookId: number) {
  return get<{ chapterId: number | null; progress: number }>(`/api/books/${bookId}/progress`)
}

export function saveProgress(bookId: number, chapterId: number, progress: number, paragraphIndex?: number) {
  return post(`/api/books/${bookId}/progress`, { chapterId, progress, paragraphIndex })
}
