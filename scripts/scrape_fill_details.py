#!/usr/bin/env python3
"""
补充爬取：从每本书的详情页获取作者名、分类、封面、简介
"""
from playwright.sync_api import sync_playwright
import json, time

INPUT = "/Users/zoulingxi/projects/bookstore-miniapp/scripts/real_chapters.json"
OUTPUT = "/Users/zoulingxi/projects/bookstore-miniapp/scripts/real_chapters_v2.json"

def flush_print(msg):
    print(msg, flush=True)


def get_book_details(page, url):
    """从书籍详情页获取作者、分类、封面、简介"""
    page.goto(url, wait_until="domcontentloaded", timeout=15000)
    time.sleep(0.5)
    
    data = page.evaluate("""() => {
        let author = '';
        let category = '';
        let cover = '';
        let description = '';
        
        // 封面
        const coverImg = document.querySelector('.book-img img, .cover img, .bookImg img, img[src*="cover"], .pic img, dt img');
        if (coverImg) cover = coverImg.src;
        
        // 作者 - 尝试多种方式
        // 1. 作者链接
        const authorLink = document.querySelector('a[href*="/author/"]');
        if (authorLink) author = authorLink.textContent.trim();
        
        // 2. 包含"作者"的文本
        if (!author) {
            document.querySelectorAll('span, p, div, dd, dt').forEach(el => {
                const text = el.textContent.trim();
                if (text.startsWith('作者') || text.startsWith('著：')) {
                    const m = text.match(/作者[：:·\\s]*([^\\s，,]+)/);
                    if (m) author = m[1];
                }
            });
        }
        
        // 3. dd标签中的作者信息
        if (!author) {
            document.querySelectorAll('dd').forEach(dd => {
                const text = dd.textContent.trim();
                if (text.length < 20 && !text.includes('章')) {
                    // 可能是作者名
                }
            });
        }
        
        // 分类
        const navLinks = document.querySelectorAll('a');
        for (const a of navLinks) {
            const href = a.href;
            const text = a.textContent.trim();
            if (href.includes('/sort') && text.length < 10) {
                category = text;
                break;
            }
        }
        
        // 简介
        const descSelectors = ['.intro', '.description', '.book-intro', '#bookintro', '.summary', '.book-desc'];
        for (const sel of descSelectors) {
            const el = document.querySelector(sel);
            if (el && el.textContent.trim().length > 20) {
                description = el.textContent.trim().substring(0, 500);
                break;
            }
        }
        if (!description) {
            const meta = document.querySelector('meta[name="description"]');
            if (meta) description = meta.content.trim().substring(0, 500);
        }
        
        return {author, category, cover, description};
    }""")
    
    return data


def main():
    with open(INPUT, 'r', encoding='utf-8') as f:
        books = json.load(f)
    
    flush_print(f"补充 {len(books)} 本书的详情信息...\n")
    
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        ctx = browser.new_context(
            user_agent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )
        page = ctx.new_page()
        page.set_default_timeout(15000)
        
        for i, book in enumerate(books):
            url = book.get('url', '')
            old_author = book.get('author', '')
            flush_print(f"[{i+1}/{len(books)}] {book['title']} (author={old_author})")
            
            if not url:
                flush_print(f"  ⚠️ 无URL，跳过")
                continue
            
            try:
                details = get_book_details(page, url)
                new_author = details.get('author', '')
                new_category = details.get('category', '')
                new_cover = details.get('cover', '')
                new_desc = details.get('description', '')
                
                if new_author:
                    book['author'] = new_author
                if new_category:
                    book['category'] = new_category
                if new_cover:
                    book['cover'] = new_cover
                if new_desc and not book.get('description'):
                    book['description'] = new_desc
                
                flush_print(f"  author={book['author']}, cat={book['category']}, cover={'✅' if new_cover else '❌'}, desc={'✅' if new_desc else '❌'}")
            except Exception as e:
                flush_print(f"  ❌ 错误: {e}")
            
            time.sleep(0.3)
        
        browser.close()
    
    # 保存
    with open(OUTPUT, 'w', encoding='utf-8') as f:
        json.dump(books, f, ensure_ascii=False, indent=2)
    
    # 统计
    authors_found = sum(1 for b in books if b.get('author') and b['author'] != '佚名')
    cats = {}
    for b in books:
        cat = b.get('category', 'unknown')
        cats[cat] = cats.get(cat, 0) + 1
    flush_print(f"\n完成！有作者的: {authors_found}/{len(books)}")
    flush_print(f"分类分布: {cats}")


if __name__ == "__main__":
    main()