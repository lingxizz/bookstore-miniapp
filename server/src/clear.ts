import { prisma } from './db'

async function clear() {
  await prisma.$transaction([
    prisma.readingRecord.deleteMany(),
    prisma.unlockedChapter.deleteMany(),
    prisma.adToken.deleteMany(),
    prisma.adDailyLimit.deleteMany(),
    prisma.order.deleteMany(),
    prisma.chapter.deleteMany(),
    prisma.book.deleteMany(),
    prisma.user.deleteMany(),
  ])
  console.log('Cleared all data')
}

clear().catch(console.error).finally(() => prisma.$disconnect())
