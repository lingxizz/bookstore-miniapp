#!/usr/bin/env python3
"""
从 sjxs5.com 爬取20本小说的真实章节名
用 Playwright 浏览器绕过反爬
"""

import json
import time
import sys
from pathlib import Path

try:
    from playwright.sync_api import sync_playwright
except ImportError:
    print("需要安装 playwright: pip install playwright && playwright install chromium")
    sys.exit(1)

BOOKS = [
    {"title": "斗破苍穹", "author": "天蚕土豆"},
    {"title": "凡人修仙传", "author": "忘语"},
    {"title": "诡秘之主", "author": "爱潜水的乌贼"},
    {"title": "全职高手", "author": "蝴蝶蓝"},
    {"title": "庆余年", "author": "猫腻"},
    {"title": "遮天", "author": "辰东"},
    {"title": "鬼吹灯", "author": "天下霸唱"},
    {"title": "盗墓笔记", "author": "南派三叔"},
    {"title": "大奉打更人", "author": "卖报小郎君"},
    {"title": "斗罗大陆", "author": "唐家三少"},
    {"title": "雪中悍刀行", "author": "烽火戏诸侯"},
    {"title": "三体", "author": "刘慈欣"},
    {"title": "明朝那些事儿", "author": "当年明月"},
    {"title": "仙逆", "author": "耳根"},
    {"title": "吞噬星空", "author": "我吃西红柿"},
    {"title": "赘婿", "author": "愤怒的香蕉"},
    {"title": "剑来", "author": "烽火戏诸侯"},
    {"title": "修真聊天群", "author": "圣骑士的传说"},
    {"title": "全球高武", "author": "老鹰吃小鸡"},
    {"title": "我有一座冒险屋", "author": "我会修空调"},
]

BASE_URL = "http://www.sjxs5.com"
OUTPUT_FILE = Path("/Users/zoulingxi/projects/bookstore-miniapp/scripts/real_chapters.json")
MAX_CHAPTERS = 50  # 每本书最多获取50章


def search_book(page, title, author):
    """搜索书籍，返回详情页URL"""
    search_url = f"{BASE_URL}/search.php?searchkey={title}"
    page.goto(search_url, wait_until="networkidle", timeout=15000)
    time.sleep(1)
    
    # 查找原著链接（书名完全匹配，作者匹配）
    links = page.evaluate("""(args) => {
        const title = args[0];
        const author = args[1];
        const results = [];
        document.querySelectorAll('a').forEach(a => {
            const text = a.textContent.trim();
            const href = a.href;
            if (text === title || (text.includes(title) && !text.includes('之'))) {
                // 找到匹配的书籍链接，检查是否是原著
                const parent = a.closest('dl, li, div, tr');
                if (parent) {
                    const parentText = parent.textContent;
                    if (parentText.includes(author)) {
                        results.push({text: text, href: href, author_match: true});
                    }
                }
                results.push({text: text, href: href, author_match: false});
            }
        });
        return results;
    }""", [title, author])
    
    # 优先选作者匹配的
    author_match = [r for r in links if r.get('author_match')]
    if author_match:
        return author_match[0]['href']
    
    # 其次选精确书名匹配（排除同人）
    exact = [r for r in links if r['text'] == title]
    if exact:
        return exact[0]['href']
    
    return None


def get_chapters(page, book_url, max_chapters=50):
    """从书籍详情页获取章节列表"""
    page.goto(book_url, wait_until="networkidle", timeout=15000)
    time.sleep(1)
    
    # 获取章节列表
    chapters = page.evaluate("""(max) => {
        const chapters = [];
        // 尝试多种选择器
        const selectors = [
            'dd a', 'li a', 'div.chapter a', 'div.listmain a', 
            '#list a', '.listmain a', 'table a',
            'a[href*="chapter"]', 'a[href*="ch"]'
        ];
        
        for (const sel of selectors) {
            const links = document.querySelectorAll(sel);
            if (links.length > 5) {
                links.forEach((a, i) => {
                    if (i >= max) return;
                    const title = a.textContent.trim();
                    if (title && title.length > 0) {
                        chapters.push({
                            title: title,
                            href: a.href,
                            order: i + 1
                        });
                    }
                });
                if (chapters.length > 5) break;
            }
        }
        
        // 如果以上选择器都没用，尝试更通用的方式
        if (chapters.length < 5) {
            const allLinks = document.querySelectorAll('a');
            const chapterLinks = [];
            allLinks.forEach(a => {
                const title = a.textContent.trim();
                const href = a.href;
                if (title.match(/^第.{1,5}章/) || title.match(/^第.{1,5}节/) || title.match(/^\d+[\.\s、]/)) {
                    chapterLinks.push({title, href, order: chapterLinks.length + 1});
                }
            });
            return chapterLinks.slice(0, max);
        }
        
        return chapters.slice(0, max);
    }""", max_chapters)
    
    return chapters


def get_chapter_content(page, chapter_url):
    """获取章节内容"""
    page.goto(chapter_url, wait_until="networkidle", timeout=15000)
    time.sleep(0.5)
    
    content = page.evaluate("""() => {
        // 尝试多种选择器获取正文
        const selectors = [
            '#booktxt', '#content', '#chaptercontent', '.chapter-content',
            '.book-content', '.read-content', '.content', '#BookText',
            '.text-content', 'article', '.chapter-content'
        ];
        
        for (const sel of selectors) {
            const el = document.querySelector(sel);
            if (el && el.textContent.trim().length > 100) {
                return el.textContent.trim();
            }
        }
        
        // 回退：找到最长的文本段落
        const divs = document.querySelectorAll('div');
        let maxLen = 0;
        let maxText = '';
        divs.forEach(div => {
            const text = div.textContent.trim();
            if (text.length > maxLen && text.length > 200) {
                maxLen = text.length;
                maxText = text;
            }
        });
        return maxText;
    }""")
    
    return content[:3000] if content else ""


def main():
    results = []
    
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(
            user_agent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )
        page = context.new_page()
        
        for i, book in enumerate(BOOKS):
            print(f"\n[{i+1}/20] 搜索: {book['title']} ({book['author']})")
            
            try:
                # 搜索书籍
                book_url = search_book(page, book['title'], book['author'])
                
                if not book_url:
                    print(f"  ❌ 未找到原著")
                    results.append({
                        "title": book['title'],
                        "author": book['author'],
                        "found": False,
                        "chapters": []
                    })
                    continue
                
                print(f"  ✅ 找到: {book_url}")
                
                # 获取章节列表
                chapters = get_chapters(page, book_url, MAX_CHAPTERS)
                print(f"  📖 章节数: {len(chapters)}")
                
                if len(chapters) > 0:
                    # 只获取章节名，不爬内容（太慢）
                    for ch in chapters[:5]:
                        print(f"     {ch['title']}")
                    
                    results.append({
                        "title": book['title'],
                        "author": book['author'],
                        "found": True,
                        "url": book_url,
                        "chapters": chapters
                    })
                else:
                    print(f"  ⚠️ 未找到章节列表")
                    results.append({
                        "title": book['title'],
                        "author": book['author'],
                        "found": True,
                        "url": book_url,
                        "chapters": []
                    })
                
            except Exception as e:
                print(f"  ❌ 错误: {e}")
                results.append({
                    "title": book['title'],
                    "author": book['author'],
                    "found": False,
                    "error": str(e),
                    "chapters": []
                })
            
            time.sleep(1)  # 请求间隔
        
        browser.close()
    
    # 保存结果
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    
    print(f"\n\n{'='*60}")
    found = sum(1 for r in results if r.get('found'))
    total_chapters = sum(len(r.get('chapters', [])) for r in results)
    print(f"完成！找到 {found}/20 本书，共 {total_chapters} 个章节")
    print(f"结果保存到: {OUTPUT_FILE}")


if __name__ == "__main__":
    main()