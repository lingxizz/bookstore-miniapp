export interface Book {
  id: number;
  title: string;
  author: string;
  category: string;
  summary: string;
  freeChapters: number;
  chapterPrice: number;
  chapterCount: number;
  score: number;
  coverIdx: number;
}

export interface Chapter {
  id: number;
  bookId: number;
  title: string;
  content: string;
  isPremium: boolean;
}

export const BOOKS: Book[] = [
  { id:1, title:'长安十二时辰', author:'马伯庸', category:'历史', summary:'唐朝长安城上元节当日，死囚张小敬必须在十二时辰内拯救长安。', freeChapters:3, chapterPrice:15, chapterCount:120, score:9.2, coverIdx:0 },
  { id:2, title:'三体', author:'刘慈欣', category:'科幻', summary:'文化大革命期间一次秘密军事工程，向宇宙发出地球的第一声啼鸣。', freeChapters:5, chapterPrice:10, chapterCount:90, score:9.6, coverIdx:2 },
  { id:3, title:'活着', author:'余华', category:'文学', summary:'福贵的一生见证了二十世纪中国的苦难与坚韧。', freeChapters:2, chapterPrice:8, chapterCount:60, score:9.4, coverIdx:1 },
  { id:4, title:'明朝那些事儿', author:'当年明月', category:'历史', summary:'从朱元璋到崇祯，明朝二百七十六年的兴衰沉浮。', freeChapters:4, chapterPrice:12, chapterCount:200, score:9.0, coverIdx:3 },
  { id:5, title:'流浪地球', author:'刘慈欣', category:'科幻', summary:'太阳即将毁灭，人类带着地球逃离太阳系。', freeChapters:3, chapterPrice:10, chapterCount:80, score:9.1, coverIdx:6 },
  { id:6, title:'围城', author:'钱钟书', category:'文学', summary:'方鸿渐的人生困境：婚姻是一座围城。', freeChapters:2, chapterPrice:8, chapterCount:50, score:9.3, coverIdx:7 },
  { id:7, title:'白夜行', author:'东野圭吾', category:'悬疑', summary:'一个绝望的爱与救赎的故事。', freeChapters:3, chapterPrice:14, chapterCount:110, score:9.5, coverIdx:4 },
  { id:8, title:'人类简史', author:'尤瓦尔·赫拉利', category:'社科', summary:'从认知革命到农业革命，人类如何主宰地球。', freeChapters:4, chapterPrice:12, chapterCount:95, score:9.2, coverIdx:8 },
];

export function genChapters(book: Book): Chapter[] {
  const chapters: Chapter[] = [];
  const free = Math.min(book.freeChapters, 5);
  for (let i = 1; i <= Math.min(book.chapterCount, 12); i++) {
    const paragraphs: string[] = [];
    for (let p = 0; p < 3 + Math.floor(Math.random() * 4); p++) {
      paragraphs.push('这是第' + i + '章的内容段落。此处展示阅读器的排版效果。'.repeat(2 + Math.floor(Math.random() * 3)));
    }
    chapters.push({ id: i, bookId: book.id, title: '第' + i + '章', content: paragraphs.join('\n\n'), isPremium: !free || i > free });
  }
  return chapters;
}
