const { PrismaClient } = require('@prisma/client');
const sqlite3 = require('sqlite3').verbose();

// PostgreSQL client
const pg = new PrismaClient({
  datasources: { db: { url: process.env.DATABASE_URL } }
});

// SQLite client
const sqlite = new sqlite3.Database('./prisma/dev.db');

function sqliteAll(sql) {
  return new Promise((resolve, reject) => {
    sqlite.all(sql, (err, rows) => {
      if (err) reject(err);
      else resolve(rows);
    });
  });
}

async function migrate() {
  console.log('Starting migration...');

  // 1. Users
  const users = await sqliteAll('SELECT * FROM User');
  for (const u of users) {
    await pg.user.upsert({
      where: { id: u.id },
      update: {},
      create: {
        id: u.id,
        openid: u.openid,
        unionid: u.unionid,
        nickname: u.nickname,
        avatar: u.avatar,
        phone: u.phone,
        coins: u.coins,
        createdAt: new Date(u.createdAt),
        updatedAt: new Date(u.updatedAt),
      }
    });
  }
  console.log(`Migrated ${users.length} users`);

  // 2. Books
  const books = await sqliteAll('SELECT * FROM Book');
  for (const b of books) {
    await pg.book.upsert({
      where: { id: b.id },
      update: {},
      create: {
        id: b.id,
        title: b.title,
        author: b.author,
        cover: b.cover,
        summary: b.summary,
        category: b.category,
        tags: b.tags,
        price: b.price,
        status: b.status,
        wordCount: b.wordCount,
        rating: b.rating,
        ratingCount: b.ratingCount,
        createdAt: new Date(b.createdAt),
      }
    });
  }
  console.log(`Migrated ${books.length} books`);

  // 3. Chapters
  const chapters = await sqliteAll('SELECT * FROM Chapter');
  for (const c of chapters) {
    await pg.chapter.upsert({
      where: { id: c.id },
      update: {},
      create: {
        id: c.id,
        bookId: c.bookId,
        title: c.title,
        content: c.content,
        order: c.order,
        price: c.price,
        isFree: Boolean(c.isFree),
        createdAt: new Date(c.createdAt),
      }
    });
  }
  console.log(`Migrated ${chapters.length} chapters`);

  // 4. UnlockedChapters
  const unlocked = await sqliteAll('SELECT * FROM UnlockedChapter');
  for (const u of unlocked) {
    await pg.unlockedChapter.upsert({
      where: { id: u.id },
      update: {},
      create: {
        id: u.id,
        userId: u.userId,
        chapterId: u.chapterId,
        unlockType: u.unlockType,
        unlockedAt: new Date(u.unlockedAt),
      }
    });
  }
  console.log(`Migrated ${unlocked.length} unlocked chapters`);

  // 5. AdTokens
  const tokens = await sqliteAll('SELECT * FROM AdToken');
  for (const t of tokens) {
    await pg.adToken.upsert({
      where: { id: t.id },
      update: {},
      create: {
        id: t.id,
        token: t.token,
        userId: t.userId,
        used: Boolean(t.used),
        expiresAt: new Date(t.expiresAt),
        createdAt: new Date(t.createdAt),
      }
    });
  }
  console.log(`Migrated ${tokens.length} ad tokens`);

  // 6. AdDailyLimits
  const limits = await sqliteAll('SELECT * FROM AdDailyLimit');
  for (const l of limits) {
    await pg.adDailyLimit.upsert({
      where: { id: l.id },
      update: {},
      create: {
        id: l.id,
        userId: l.userId,
        date: l.date,
        count: l.count,
        createdAt: new Date(l.createdAt),
      }
    });
  }
  console.log(`Migrated ${limits.length} ad daily limits`);

  // 7. Orders
  const orders = await sqliteAll('SELECT * FROM "Order"');
  for (const o of orders) {
    await pg.order.upsert({
      where: { id: o.id },
      update: {},
      create: {
        id: o.id,
        userId: o.userId,
        amount: o.amount,
        coinAmount: o.coinAmount,
        status: o.status,
        payMethod: o.payMethod,
        createdAt: new Date(o.createdAt),
        paidAt: o.paidAt ? new Date(o.paidAt) : null,
      }
    });
  }
  console.log(`Migrated ${orders.length} orders`);

  // 8. ReadingRecords
  const records = await sqliteAll('SELECT * FROM ReadingRecord');
  for (const r of records) {
    await pg.readingRecord.upsert({
      where: { id: r.id },
      update: {},
      create: {
        id: r.id,
        userId: r.userId,
        bookId: r.bookId,
        chapterId: r.chapterId,
        progress: r.progress,
        lastReadAt: new Date(r.lastReadAt),
      }
    });
  }
  console.log(`Migrated ${records.length} reading records`);

  // 9. Comments
  const comments = await sqliteAll('SELECT * FROM Comment');
  for (const c of comments) {
    await pg.comment.upsert({
      where: { id: c.id },
      update: {},
      create: {
        id: c.id,
        userId: c.userId,
        bookId: c.bookId,
        chapterId: c.chapterId,
        content: c.content,
        paragraphIndex: c.paragraphIndex,
        parentId: c.parentId,
        likes: c.likes,
        createdAt: new Date(c.createdAt),
      }
    });
  }
  console.log(`Migrated ${comments.length} comments`);

  // 10. Bookmarks
  const bookmarks = await sqliteAll('SELECT * FROM Bookmark');
  for (const b of bookmarks) {
    await pg.bookmark.upsert({
      where: { id: b.id },
      update: {},
      create: {
        id: b.id,
        userId: b.userId,
        chapterId: b.chapterId,
        paragraphIndex: b.paragraphIndex,
        note: b.note,
        createdAt: new Date(b.createdAt),
      }
    });
  }
  console.log(`Migrated ${bookmarks.length} bookmarks`);

  // 11. ChapterSummaries
  const summaries = await sqliteAll('SELECT * FROM ChapterSummary');
  for (const s of summaries) {
    await pg.chapterSummary.upsert({
      where: { id: s.id },
      update: {},
      create: {
        id: s.id,
        chapterId: s.chapterId,
        content: s.content,
        source: s.source,
        createdAt: new Date(s.createdAt),
      }
    });
  }
  console.log(`Migrated ${summaries.length} chapter summaries`);

  console.log('Migration complete!');
}

migrate().catch(console.error).finally(() => {
  sqlite.close();
  pg.$disconnect();
});
