#!/usr/bin/env python3
"""
从豆瓣获取真实书籍封面
"""
import requests
import time
import subprocess
import urllib.parse

BASE_URL = "http://localhost:3001"
DOUBAN_URL = "https://book.douban.com/j/subject_suggest"

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
    "Referer": "https://book.douban.com/",
}

def get_cover(title):
    """从豆瓣获取书籍封面URL"""
    url = f"{DOUBAN_URL}?q={requests.utils.quote(title)}"
    try:
        r = requests.get(url, headers=HEADERS, timeout=15)
        data = r.json()
        if data:
            # 精确匹配
            for item in data:
                if title in item.get('title', ''):
                    pic = item.get('pic', '')
                    if pic:
                        return pic.replace('/s/', '/l/')
            # 模糊匹配取第一个
            pic = data[0].get('pic', '')
            if pic:
                return pic.replace('/s/', '/l/')
    except Exception as e:
        print(f"    豆瓣查询失败: {e}")
    return None

def main():
    # 获取所有书籍
    resp = requests.get(f"{BASE_URL}/api/books", timeout=10)
    books = resp.json()
    print(f"获取到 {len(books)} 本书\n")

    success_count = 0
    fail_count = 0

    for book in books:
        title = book["title"]
        book_id = book["id"]
        print(f"  📖 查询: {title}...", end=" ")

        cover_url = get_cover(title)
        if cover_url:
            # 更新数据库
            result = subprocess.run(
                ['psql', '-U', 'postgres', '-d', 'bookstore', '-c',
                 f"UPDATE \"Book\" SET cover = '{cover_url}' WHERE id = {book_id};"],
                capture_output=True, text=True
            )
            if "UPDATE 1" in result.stdout:
                print(f"✅ {cover_url[:60]}...")
                success_count += 1
            else:
                print(f"❌ DB更新失败")
                fail_count += 1
        else:
            # 保留 picsum 占位图
            seed = f"seed/{urllib.parse.quote(title)[:8]}"
            fallback = f"https://picsum.photos/seed/{seed}/180/240"
            result = subprocess.run(
                ['psql', '-U', 'postgres', '-d', 'bookstore', '-c',
                 f"UPDATE \"Book\" SET cover = '{fallback}' WHERE id = {book_id};"],
                capture_output=True, text=True
            )
            print(f"⚠️ 无封面，使用占位图")
            fail_count += 1

        time.sleep(0.5)  # 避免被豆瓣封

    print(f"\n完成！成功: {success_count}, 占位: {fail_count}")

    # 验证
    resp = requests.get(f"{BASE_URL}/api/books", timeout=10)
    books = resp.json()
    print(f"\n验证封面:")
    for b in books[:5]:
        cover = b.get('cover', '')
        src = "豆瓣" if 'doubanio' in cover else "占位"
        print(f"  {b['title']} [{src}]: {cover[:70]}...")
    if len(books) > 5:
        print(f"  ... 还有 {len(books)-5} 本")


if __name__ == "__main__":
    main()