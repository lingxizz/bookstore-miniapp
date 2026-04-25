#!/usr/bin/env python3
"""
为书籍生成封面图 URL 并更新到数据库
使用 picsum.photos 占位图服务（可靠的公开占位图）
"""

import requests
import json
import subprocess
import hashlib
import urllib.parse

BASE_URL = "http://localhost:3001"

# 书名到封面种子的映射（确保每本书封面稳定不变）
BOOK_SEEDS = {
    "斗破苍穹": "dpcq2024",
    "凡人修仙传": "frxx2024",
    "诡秘之主": "gmzz2024",
    "全职高手": "qzgs2024",
    "庆余年": "qyn2024",
    "遮天": "zt2024",
    "鬼吹灯": "gcd2024",
    "盗墓笔记": "dmbj2024",
    "大奉打更人": "dfdgr2024",
    "斗罗大陆": "dldl2024",
    "雪中悍刀行": "xzdhx2024",
    "三体": "santi2024",
    "明朝那些事儿": "mcnsx2024",
    "仙逆": "xianni2024",
    "吞噬星空": "tsxk2024",
    "赘婿": "zx2024",
    "剑来": "jianlai2024",
    "修真聊天群": "xzltq2024",
    "全球高武": "qqgw2024",
    "我有一座冒险屋": "wyymx2024",
}

def main():
    # 获取所有书籍
    resp = requests.get(f"{BASE_URL}/api/books", timeout=10)
    books = resp.json()
    print(f"获取到 {len(books)} 本书")

    # 为每本书生成封面URL
    updates = []
    for book in books:
        seed = BOOK_SEEDS.get(book["title"], hashlib.md5(book["title"].encode()).hexdigest()[:8])
        # 使用 picsum.photos 生成稳定的封面图 (180x240比例)
        cover_url = f"https://picsum.photos/seed/{seed}/180/240"
        updates.append((book["id"], book["title"], cover_url))

    # 通过 SQL 更新封面（因为没有专门的 API）
    print(f"\n更新封面:")
    for book_id, title, cover_url in updates:
        # 用 psql 直接更新
        result = subprocess.run(
            ['psql', '-U', 'postgres', '-d', 'bookstore', '-c',
             f"UPDATE \"Book\" SET cover = '{cover_url}' WHERE id = {book_id};"],
            capture_output=True, text=True
        )
        if "UPDATE 1" in result.stdout:
            print(f"  ✅ {title} => {cover_url}")
        else:
            print(f"  ❌ {title}: {result.stdout.strip()} {result.stderr.strip()}")

    print(f"\n封面更新完成！")

    # 验证
    resp = requests.get(f"{BASE_URL}/api/books", timeout=10)
    books = resp.json()
    print(f"\n验证封面:")
    for b in books[:5]:
        print(f"  {b['title']}: {b.get('cover', 'NO COVER')}")
    if len(books) > 5:
        print(f"  ... 还有 {len(books)-5} 本")


if __name__ == "__main__":
    main()