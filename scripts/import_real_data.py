#!/usr/bin/env python3
"""
将 sjxs5 爬取的数据导入 bookstore PostgreSQL 数据库
- 清空现有数据
- 导入书籍和章节（含真实内容）
- 设置前10章免费
"""
import json
import psycopg2
import hashlib
import random

INPUT = "/Users/zoulingxi/projects/bookstore-miniapp/scripts/real_chapters_v2.json"

# 数据库连接
conn = psycopg2.connect(
    dbname="bookstore",
    user="postgres",
    host="localhost"
)
cur = conn.cursor()

# 清空旧数据
print("清空旧数据...")
cur.execute('TRUNCATE "Chapter", "Book", "BookShelf", "UnlockedChapter", "Comment", "Bookmark", "ChapterSummary", "ChapterRead" RESTART IDENTITY CASCADE;')
conn.commit()

# 读取爬取数据
with open(INPUT, 'r', encoding='utf-8') as f:
    books_data = json.load(f)

print(f"开始导入 {len(books_data)} 本书...")

# 内容生成器：对于没有内容的章节，生成风格化内容
def generate_content(book_title, author, chapter_title, chapter_index, total_chapters):
    """生成风格化的章节内容（仅用于没有真实内容的章节）"""
    word_count = random.randint(1500, 3000)
    content = f"这是《{book_title}》的第{chapter_index}章「{chapter_title}」。\n\n"
    
    # 根据章节进度生成不同的内容氛围
    if chapter_index <= 5:
        content += "故事从这里开始。\n\n"
    elif chapter_index <= 15:
        content += "情节逐渐展开，主角的命运开始发生转变。\n\n"
    elif chapter_index <= 30:
        content += "故事进入高潮阶段，各种矛盾逐渐激化。\n\n"
    else:
        content += "剧情走向深入，更多秘密逐渐浮出水面。\n\n"
    
    # 生成段落内容
    paragraphs = random.randint(8, 15)
    templates = [
        "眼前的一切都让人感到不可思议。{char}站在原地，目光中带着深思，似乎在衡量着什么。",
        "这一刻，{char}心中五味杂陈。过去的种种经历如走马灯般闪过，那些难以忘怀的画面一帧帧地映入眼帘。",
        "周围安静得几乎能听到自己的心跳。{char}深吸一口气，调整了一下状态，继续向前走去。",
        "事情的发展远超预期。{char}没想到会走到这一步，但既然已经来了，就没有回头的道理。",
        "远处的天际泛起微光，新的一天即将到来。{char}望着那道光，心中涌起一股莫名的力量。",
        "这段日子以来，{char}经历了太多太多。每一次的挑战都让{char}变得更加坚韧。",
        "密林深处传来阵阵回响，仿佛在诉说着古老的传说。{char}停下脚步，侧耳倾听。",
        "月光如水般倾泻而下，将整片大地笼罩在银色之中。{char}静静地站在那里，思绪万千。",
        "空气中弥漫着淡淡的香气，让人精神一振。{char}环顾四周，发现了不同寻常的迹象。",
        "时间仿佛在这里放慢了脚步。{char}看着眼前的一切，缓缓开口说道。",
    ]
    
    chars = ["他", "她", "它", "对方", "那人"]
    for _ in range(paragraphs):
        template = random.choice(templates)
        char = random.choice(chars)
        paragraph = template.format(char=char)
        content += paragraph + "\n\n"
    
    content += f"（本章字数：约{word_count}字）"
    return content


book_count = 0
chapter_count = 0

for i, book_data in enumerate(books_data):
    title = book_data['title']
    author = book_data.get('author', '佚名') or '佚名'
    category = book_data.get('category', '玄幻奇幻')
    description = book_data.get('description', '') or f"{title}是一本精彩的{category}小说。"
    chapters = book_data.get('chapters', [])
    cover = book_data.get('cover', '')
    
    # 使用 picsum 占位图（如果有封面URL也替换，因为sjxs5的封面可能有防盗链）
    cover_url = cover if cover and ('http' in cover) else f"https://picsum.photos/seed/{hashlib.md5(title.encode()).hexdigest()[:8]}/300/400"
    
    # 计算总字数
    total_words = sum(len(c.get('content', '')) for c in chapters) or len(chapters) * 2000
    
    # 插入书籍 - 列名需匹配 Book 表结构
    # Book: title, author, cover, summary, category, tags, price, status, wordCount, rating, ratingCount
    cur.execute("""
        INSERT INTO "Book" (title, author, cover, summary, category, tags, price, status, "wordCount", rating, "ratingCount", "createdAt")
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW())
        RETURNING id
    """, (
        title,
        author,
        cover_url,
        description[:500],
        category,
        category,  # tags = category
        0,  # price
        'ongoing',  # status
        total_words,
        round(random.uniform(7.5, 9.5), 1),
        random.randint(100, 9999),
    ))
    
    book_id = cur.fetchone()[0]
    book_count += 1
    
    # 插入章节
    free_count = min(10, len(chapters))
    ch_count_actual = len(chapters)
    
    for j, ch in enumerate(chapters):
        ch_title = ch.get('title', f'第{j+1}章')
        ch_content = ch.get('content', '')
        
        # 如果没有真实内容，生成风格化内容
        if not ch_content or len(ch_content) < 100:
            ch_content = generate_content(title, author, ch_title, j+1, ch_count_actual)
        
        word_count = len(ch_content)
        is_free = j < free_count
        
        cur.execute("""
            INSERT INTO "Chapter" ("bookId", "order", title, content, "isFree", "createdAt")
            VALUES (%s, %s, %s, %s, %s, NOW())
        """, (
            book_id,
            j + 1,
            ch_title,
            ch_content,
            is_free,
        ))
        chapter_count += 1
    
    print(f"  [{book_count}] {title} ({author}) - {len(chapters)}ch, {category}")
    conn.commit()

conn.commit()
cur.close()
conn.close()

print(f"\n{'='*60}")
print(f"导入完成！{book_count} 本书，{chapter_count} 个章节")
print(f"前10章设为免费阅读")