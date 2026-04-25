import { prisma } from './db'

async function seed() {
  const books = [
    { title: '长安十二时辰', author: '马伯庸', cover: 'https://placehold.co/200x280/B8451A/FFF?text=长安', summary: '唐天宝三载，上元节长安城危在旦夕，死囚张小敬挺身而出，在十二时辰内拯救长安。', category: '历史', tags: '历史,悬疑,古装', price: 0, status: 'finished', wordCount: 350000, rating: 9.2 },
    { title: '三体', author: '刘慈欣', cover: 'https://placehold.co/200x280/1A1A1A/FFF?text=三体', summary: '文化大革命如火如荼进行的同时，军方探寻外星文明的绝秘计划"红岸工程"取得了突破性进展。', category: '科幻', tags: '科幻,硬科幻,宇宙', price: 0, status: 'finished', wordCount: 890000, rating: 9.6 },
    { title: '活着', author: '余华', cover: 'https://placehold.co/200x280/5B3A29/FFF?text=活着', summary: '讲述了农村人福贵悲惨的人生遭遇。福贵的一生窄如手掌，可是不知道是否也宽若大地？', category: '文学', tags: '文学,现实主义,经典', price: 0, status: 'finished', wordCount: 132000, rating: 9.4 },
    { title: '明朝那些事儿', author: '当年明月', cover: 'https://placehold.co/200x280/D4A574/333?text=明朝', summary: '这一系列主要讲述的是从1344年到1644年这三百年间关于明朝的一些故事。', category: '历史', tags: '历史,明朝,通俗', price: 0, status: 'finished', wordCount: 1200000, rating: 9.5 },
    { title: '围城', author: '钱钟书', cover: 'https://placehold.co/200x280/2C3E50/FFF?text=围城', summary: '一部以讽刺知识分子和婚姻生活为主题的长篇小说，被誉为"新儒林外史"。', category: '文学', tags: '文学,讽刺,婚姻', price: 0, status: 'finished', wordCount: 253000, rating: 9.1 },
    { title: '白夜行', author: '东野圭吾', cover: 'https://placehold.co/200x280/000000/FFF?text=白夜行', summary: '1973年，大阪的一栋废弃建筑内发现了一具男尸，此后19年，嫌疑人之女雪穗与被害者之子桐原亮司走上截然不同的人生道路。', category: '悬疑', tags: '悬疑,推理,日本', price: 0, status: 'finished', wordCount: 350000, rating: 9.3 },
    { title: '人类简史', author: '尤瓦尔·赫拉利', cover: 'https://placehold.co/200x280/34495E/FFF?text=人类', summary: '从认知革命、农业革命到科学革命，重新审视人类历史的宏大叙事。', category: '社科', tags: '历史,人类学,科普', price: 0, status: 'finished', wordCount: 280000, rating: 9.0 },
    { title: '追风筝的人', author: '卡勒德·胡赛尼', cover: 'https://placehold.co/200x280/8E44AD/FFF?text=追风筝', summary: '12岁的阿富汗富家少爷阿米尔与仆人哈桑情同手足，然而，在一场风筝比赛后，发生了一件悲惨不堪的事。', category: '文学', tags: '文学,成长,阿富汗', price: 0, status: 'finished', wordCount: 220000, rating: 9.1 },
  ]

  for (const b of books) {
    await prisma.book.create({
      data: {
        ...b,
        chapters: {
          create: Array.from({ length: 10 }).map((_, i) => ({
            title: `第${i + 1}章`,
            content: `这是${b.title}的第${i + 1}章内容。\n\n（此处为示例文本，实际部署时可接入CMS或文件存储）\n\n${Array.from({ length: 20 }).map(() => '书页在指尖翻动，故事在脑海中展开。').join('')}`,
            order: i + 1,
            price: i < 3 ? 0 : 10,
            isFree: i < 3,
          })),
        },
      },
    })
  }
  console.log('Seeded', books.length, 'books')
}

seed().catch(console.error).finally(() => prisma.$disconnect())
