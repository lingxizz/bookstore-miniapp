import fastify from 'fastify'
import cors from '@fastify/cors'
import jwt from '@fastify/jwt'
import dotenv from 'dotenv'
import crypto from 'crypto'
import { prisma } from './db'

dotenv.config()

const app = fastify({ logger: true })

app.register(cors, { origin: true })
app.register(jwt, { secret: process.env.JWT_SECRET || 'fallback-secret' })

// Auth hook
app.addHook('onRequest', async (request, reply) => {
  const whiteList = [
    { path: '/api/auth/login', methods: ['POST'] },
    { path: '/api/books', methods: ['GET'] },
    { path: '/api/books/', methods: ['GET'] },
    { path: '/api/chapters/', methods: ['GET'] },
    { path: '/api/comments/', methods: ['GET'] },
    { path: '/health', methods: ['GET'] },
  ]
  const isPublic = whiteList.some(w => request.url.startsWith(w.path) && w.methods.includes(request.method))
  try {
    await request.jwtVerify()
  } catch {
    if (!isPublic) {
      reply.status(401).send({ error: 'Unauthorized' })
    }
  }
})

// Login (mock WeChat)
app.post('/api/auth/login', async (request, reply) => {
  const body = request.body as any
  const code = body?.code || 'mock_code'

  // H5 登录用 IP 固定用户，避免每次刷新都创建新用户
  let openid
  if (code.startsWith('h5_')) {
    const forwarded = request.headers['x-forwarded-for']
    const clientIp = (typeof forwarded === 'string' ? forwarded.split(',')[0] : request.ip) || 'unknown'
    openid = `h5_${clientIp.trim()}`
  } else {
    openid = `openid_${code}`
  }

  let user = await prisma.user.findUnique({ where: { openid } })
  if (!user) {
    user = await prisma.user.create({
      data: {
        openid,
        nickname: body?.nickname || '读者',
        avatar: body?.avatar || '',
      }
    })
  }
  const token = app.jwt.sign({ userId: user.id, openid })
  return { token, user }
})

// Books list
app.get('/api/books', async (request) => {
  const { category, q } = request.query as any
  const where: any = {}
  if (category) where.category = category
  if (q) {
    where.OR = [
      { title: { contains: q } },
      { author: { contains: q } },
    ]
  }
  return prisma.book.findMany({ where, orderBy: { id: 'desc' } })
})

// Book detail
app.get('/api/books/:id', async (request) => {
  const { id } = request.params as any
  return prisma.book.findUnique({ where: { id: Number(id) } })
})

// Book chapters
app.get('/api/books/:id/chapters', async (request) => {
  const { id } = request.params as any
  return prisma.chapter.findMany({
    where: { bookId: Number(id) },
    orderBy: { order: 'asc' },
    select: { id: true, title: true, order: true, price: true, isFree: true },
  })
})

// Chapter detail
app.get('/api/chapters/:id', async (request, reply) => {
  const { id } = request.params as any
  const userId = (request.user as any)?.userId
  const chapter = await prisma.chapter.findUnique({
    where: { id: Number(id) },
    include: { book: { select: { title: true } } },
  })
  if (!chapter) return { error: 'Not found' }
  console.log('[fetchChapter] id=', id, 'userId=', userId, 'isFree=', chapter.isFree)
  if (!chapter.isFree) {
    if (!userId) {
      console.log('[fetchChapter] no userId -> locked')
      return { ...chapter, content: null, locked: true }
    }
    const unlocked = await prisma.unlockedChapter.findFirst({
      where: { userId, chapterId: chapter.id },
    })
    console.log('[fetchChapter] unlocked=', !!unlocked)
    if (!unlocked) {
      return { ...chapter, content: null, locked: true }
    }
  }
  return chapter
})

// Unlock chapter
app.post('/api/chapters/:id/unlock', async (request) => {
  const { id } = request.params as any
  const userId = (request.user as any).userId
  const chapter = await prisma.chapter.findUnique({ where: { id: Number(id) } })
  if (!chapter) return { error: 'Not found' }
  if (chapter.isFree) return { success: true }
  const user = await prisma.user.findUnique({ where: { id: userId } })
  if (!user || user.coins < chapter.price) return { error: 'Insufficient coins' }
  await prisma.$transaction([
    prisma.user.update({ where: { id: userId }, data: { coins: { decrement: chapter.price } } }),
    prisma.unlockedChapter.create({
      data: { userId, chapterId: chapter.id },
    }),
  ])
  return { success: true }
})

// Me
app.get('/api/users/me', async (request) => {
  const userId = (request.user as any).userId
  return prisma.user.findUnique({ where: { id: userId } })
})

// Create order
app.post('/api/orders', async (request) => {
  const userId = (request.user as any).userId
  const { amount, coinAmount } = request.body as any
  const order = await prisma.order.create({
    data: { userId, amount, coinAmount, status: 'pending' },
  })
  return order
})

// Pay order (mock)
app.post('/api/orders/:id/pay', async (request) => {
  const { id } = request.params as any
  const userId = (request.user as any).userId
  const order = await prisma.order.update({
    where: { id: Number(id), userId },
    data: { status: 'paid', paidAt: new Date() },
  })
  await prisma.user.update({
    where: { id: userId },
    data: { coins: { increment: order.coinAmount } },
  })
  return order
})

// Reading records
app.get('/api/reading-records', async (request) => {
  const userId = (request.user as any).userId
  return prisma.readingRecord.findMany({
    where: { userId },
    orderBy: { lastReadAt: 'desc' },
  })
})

app.post('/api/reading-records', async (request) => {
  const userId = (request.user as any).userId
  const { bookId, chapterId, progress } = request.body as any
  const existing = await prisma.readingRecord.findFirst({
    where: { userId, bookId: Number(bookId) },
  })
  if (existing) {
    return prisma.readingRecord.update({
      where: { id: existing.id },
      data: { chapterId: chapterId ? Number(chapterId) : existing.chapterId, progress, lastReadAt: new Date() },
    })
  }
  return prisma.readingRecord.create({
    data: { userId, bookId: Number(bookId), chapterId: chapterId ? Number(chapterId) : null, progress },
  })
})

// \u7b80\u5316\u7248\u8fdb\u5ea6\u63a5\u53e3
app.get('/api/books/:id/progress', async (request) => {
  const userId = (request.user as any)?.userId
  const { id } = request.params as any
  if (!userId) return { chapterId: null, progress: 0 }
  const rec = await prisma.readingRecord.findFirst({
    where: { userId, bookId: Number(id) },
  })
  return rec || { chapterId: null, progress: 0 }
})

app.post('/api/books/:id/progress', async (request) => {
  const userId = (request.user as any).userId
  const { id } = request.params as any
  const { chapterId, progress } = request.body as any
  const existing = await prisma.readingRecord.findFirst({
    where: { userId, bookId: Number(id) },
  })
  if (existing) {
    return prisma.readingRecord.update({
      where: { id: existing.id },
      data: { chapterId: Number(chapterId), progress, lastReadAt: new Date() },
    })
  }
  return prisma.readingRecord.create({
    data: { userId, bookId: Number(id), chapterId: Number(chapterId), progress },
  })
})

// \u7ae0\u8bc4\u63a5\u53e3\uff08\u7b2c\u4e8c\u6279\u9884\u70ed\uff09
app.get('/api/chapters/:id/comments', async (request) => {
  const { id } = request.params as any
  const { cursor, limit = '20' } = request.query as any
  const comments = await prisma.comment.findMany({
    where: { chapterId: Number(id), parentId: null },
    take: Number(limit) + 1,
    skip: cursor ? 1 : 0,
    cursor: cursor ? { id: Number(cursor) } : undefined,
    orderBy: { createdAt: 'desc' },
    include: {
      user: { select: { id: true, nickname: true, avatar: true } },
    },
  })
  let nextCursor = null
  if (comments.length > Number(limit)) {
    const next = comments.pop()
    nextCursor = next?.id
  }
  return { comments, nextCursor }
})

app.post('/api/chapters/:id/comments', async (request) => {
  const userId = (request.user as any).userId
  const { id } = request.params as any
  const { content, paragraphIndex } = request.body as any
  if (!content || content.trim().length === 0) return { error: 'Content required' }
  const chapter = await prisma.chapter.findUnique({ where: { id: Number(id) } })
  if (!chapter) return { error: 'Chapter not found' }
  const comment = await prisma.comment.create({
    data: {
      userId,
      bookId: chapter.bookId,
      chapterId: chapter.id,
      content: content.trim(),
      paragraphIndex: paragraphIndex != null ? Number(paragraphIndex) : null,
    },
    include: {
      user: { select: { id: true, nickname: true, avatar: true } },
    },
  })
  return comment
})

app.post('/api/comments/:id/like', async (request) => {
  const { id } = request.params as any
  const comment = await prisma.comment.update({
    where: { id: Number(id) },
    data: { likes: { increment: 1 } },
  })
  return { likes: comment.likes }
})

// AI \u603b\u7ed3\u63a5\u53e3\uff08\u7b2c\u56db\u6279\u9884\u70ed\uff09
app.get('/api/chapters/:id/summary', async (request) => {
  const { id } = request.params as any
  const summary = await prisma.chapterSummary.findUnique({
    where: { chapterId: Number(id) },
  })
  return summary || { content: null }
})

app.post('/api/chapters/:id/summary', async (request) => {
  const { id } = request.params as any
  const { content } = request.body as any
  const chapter = await prisma.chapter.findUnique({ where: { id: Number(id) } })
  if (!chapter) return { error: 'Not found' }
  const summary = await prisma.chapterSummary.upsert({
    where: { chapterId: Number(id) },
    update: { content },
    create: { chapterId: Number(id), content },
  })
  return summary
})

// Ad complete -> generate token
app.post('/api/ad/complete', async (request) => {
  const userId = (request.user as any).userId
  const today = new Date().toISOString().slice(0, 10)

  // check daily limit
  let limit = await prisma.adDailyLimit.findUnique({
    where: { userId_date: { userId, date: today } }
  })
  if (limit && limit.count >= 10) {
    return { error: 'Daily ad limit reached' }
  }

  const token = crypto.randomUUID()
  const expiresAt = new Date(Date.now() + 5 * 60 * 1000) // 5min

  await prisma.adToken.create({
    data: { token, userId, expiresAt }
  })

  return { token }
})

// Ad unlock chapter
app.post('/api/ad/unlock', async (request) => {
  const userId = (request.user as any).userId
  const { chapterId, adToken } = request.body as any

  // verify token
  const tokenRec = await prisma.adToken.findUnique({
    where: { token: adToken }
  })
  if (!tokenRec || tokenRec.used || tokenRec.userId !== userId || tokenRec.expiresAt < new Date()) {
    return { error: 'Invalid or expired ad token' }
  }

  const today = new Date().toISOString().slice(0, 10)

  // increment daily limit
  await prisma.adDailyLimit.upsert({
    where: { userId_date: { userId, date: today } },
    update: { count: { increment: 1 } },
    create: { userId, date: today, count: 1 }
  })

  // mark token used
  await prisma.adToken.update({
    where: { id: tokenRec.id },
    data: { used: true }
  })

  // unlock chapter
  try {
    await prisma.unlockedChapter.create({
      data: { userId, chapterId: Number(chapterId), unlockType: 'ad' }
    })
  } catch (e) {
    // already unlocked
  }

  return { success: true }
})

// ========== 书架 ==========
app.post('/api/books/:id/shelf', async (request) => {
  const userId = (request.user as any).userId
  const { id } = request.params as any
  try {
    await prisma.bookShelf.create({
      data: { userId, bookId: Number(id) }
    })
  } catch (e) {
    // already in shelf
  }
  return { success: true }
})

app.delete('/api/books/:id/shelf', async (request) => {
  const userId = (request.user as any).userId
  const { id } = request.params as any
  await prisma.bookShelf.deleteMany({
    where: { userId, bookId: Number(id) }
  })
  return { success: true }
})

app.get('/api/books/:id/shelf', async (request) => {
  const userId = (request.user as any)?.userId
  const { id } = request.params as any
  if (!userId) return { inShelf: false }
  const item = await prisma.bookShelf.findUnique({
    where: { userId_bookId: { userId, bookId: Number(id) } }
  })
  return { inShelf: !!item }
})

app.get('/api/shelf', async (request) => {
  const userId = (request.user as any).userId
  const items = await prisma.bookShelf.findMany({
    where: { userId },
    orderBy: { createdAt: 'desc' },
    include: { book: true }
  })
  return items
})

// ========== 阅读器配置 ==========
app.get('/api/users/reader-config', async (request) => {
  const userId = (request.user as any).userId
  const config = await prisma.readerConfig.findUnique({
    where: { userId }
  })
  return config || { fontSize: 18, lineHeight: 160, theme: 'light', brightness: 100 }
})

app.post('/api/users/reader-config', async (request) => {
  const userId = (request.user as any).userId
  const { fontSize, lineHeight, theme, brightness } = request.body as any
  const config = await prisma.readerConfig.upsert({
    where: { userId },
    update: { fontSize, lineHeight, theme, brightness },
    create: { userId, fontSize, lineHeight, theme, brightness }
  })
  return config
})

// ========== 章节已读 ==========
app.post('/api/chapters/:id/read', async (request) => {
  const userId = (request.user as any).userId
  const { id } = request.params as any
  try {
    await prisma.chapterRead.create({
      data: { userId, chapterId: Number(id) }
    })
  } catch (e) {
    // already read
  }
  return { success: true }
})

app.get('/api/books/:id/read-chapters', async (request) => {
  const userId = (request.user as any)?.userId
  const { id } = request.params as any
  if (!userId) return { chapters: [] }
  const chapters = await prisma.chapterRead.findMany({
    where: { userId, chapter: { bookId: Number(id) } },
    select: { chapterId: true }
  })
  return { chapters: chapters.map(c => c.chapterId) }
})

app.get('/health', async () => ({ status: 'ok' }));

const PORT = Number(process.env.PORT) || 3000
app.listen({ port: PORT, host: '0.0.0.0' }).then(() => {
  console.log(`Server running on http://localhost:${PORT}`)
})
