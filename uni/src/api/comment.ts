import { get, post } from '@/utils/request'

export interface Comment {
  id: number
  content: string
  likes: number
  createdAt: string
  user: {
    id: number
    nickname: string
    avatar: string
  }
}

export function fetchComments(chapterId: number, cursor?: number, limit = 20) {
  return get<{ comments: Comment[]; nextCursor: number | null }>(`/api/chapters/${chapterId}/comments`, { cursor, limit })
}

export function postComment(chapterId: number, content: string, paragraphIndex?: number) {
  return post<Comment>(`/api/chapters/${chapterId}/comments`, { content, paragraphIndex })
}

export function likeComment(commentId: number) {
  return post<{ likes: number }>(`/api/comments/${commentId}/like`)
}

export function fetchSummary(chapterId: number) {
  return get<{ content: string | null }>(`/api/chapters/${chapterId}/summary`)
}
