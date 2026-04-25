#!/usr/bin/env python3
"""
从 sjxs5.com 爬取书籍 v5 - 更高效更稳定
分两步: 1) 从首页/分类页获取书单 2) 逐本爬取章节列表(含前10章内容)
"""
from playwright.sync_api import sync_playwright
import json, time, sys, re

BASE_URL = "http://www.sjxs5.com"
MAX_CHAPTERS = 50
OUTPUT = "/Users/zoulingxi/projects/bookstore-miniapp/scripts/real_chapters.json"

# 我们需要覆盖的分类
CATEGORIES = {
    "玄幻奇幻": "http://www.sjxs5.com/sort7/",
    "武侠仙侠": "http://www.sjxs5.com/sort9/",
    "都市生活": "http://www.sjxs5.com/sort3/",
    "悬疑惊悚": "http://www.sjxs5.com/sort11/",
    "科幻": "http://www.sjxs5.com/sort12/",
    "历史": "http://www.sjxs5.com/sort6/",
}

def flush_print(msg):
    print(msg, flush=True)


def extract_books_from_page(page, url, category):
    """从分类页/首页提取书籍"""
    page.goto(url, wait_until="domcontentloaded", timeout=15000)
    time.sleep(1)
    
    books = page.evaluate("""(args) => {
        const url = args[0];
        const category = args[1];
        const results = [];
        const seen = new Set();
        
        document.querySelectorAll('a').forEach(a => {
            const text = a.textContent.trim();
            const href = a.href;
            if (!href.includes('/txt') || seen.has(text) || text.length < 2 || text.length > 40) return;
            seen.add(text);
            
            // 获取作者 - 查找父容器中的作者链接或文本
            let author = '';
            let parent = a.parentElement;
            for (let i = 0; i < 5 && parent; i++) {
                // 检查作者链接
                const authorLink = parent.querySelector('a[href*="/author/"]');
                if (authorLink) { author = authorLink.textContent.trim(); break; }
                parent = parent.parentElement;
            }
            
            // 如果父容器没找到作者，看相邻文本
            if (!author) {
                parent = a.parentElement;
                if (parent) {
                    const text = parent.textContent;
                    // 作者名通常在书名后面的特殊字符后
                    const m = text.match(/[·\-—|]\\s*([^\\s·—|]+?)\\s*$/);
                    if (m && m[1].length < 10 && m[1] !== text) author = m[1];
                }
            }
            
            results.push({title: text, href: href, author: author, category: category});
        });
        return results;
    }""", [url, category])
    
    return books


def get_chapters_from_detail(page, url, max_ch=50):
    """从书籍详情页获取章节列表"""
    page.goto(url, wait_until="domcontentloaded", timeout=15000)
    time.sleep(0.5)
    
    # 获取描述和章节
    data = page.evaluate("""(max) => {
        // 描述
        let description = '';
        const descSelectors = ['.intro', '.description', '.book-intro', '#bookintro', '.summary'];
        for (const sel of descSelectors) {
            const el = document.querySelector(sel);
            if (el && el.textContent.trim().length > 20) {
                description = el.textContent.trim().substring(0, 500);
                break;
            }
        }
        const meta = document.querySelector('meta[name="description"]');
        if (!description && meta) description = meta.content.trim().substring(0, 500);
        
        // 封面
        let cover = '';
        const img = document.querySelector('.book-img img, .cover img, .bookImg img, img[src*="cover"]');
        if (img) cover = img.src;
        
        // 章节
        const chapters = [];
        const seen = new Set();
        const patterns = [/^第.{1,5}章/, /^第.{1,5}节/];
        
        document.querySelectorAll('a').forEach(a => {
            const t = a.textContent.trim();
            const h = a.href;
            if (t.length > 2 && t.length < 80 && !seen.has(t)) {
                for (const pat of patterns) {
                    if (pat.test(t)) {
                        seen.add(t);
                        chapters.push({title: t, href: h});
                        break;
                    }
                }
            }
        });
        
        return {description, cover, chapters: chapters.slice(0, max)};
    }""", max_ch)
    
    return data


def get_chapter_content(page, url):
    """获取单个章节内容"""
    try:
        page.goto(url, wait_until="domcontentloaded", timeout=10000)
        time.sleep(0.3)
    except:
        return ""
    
    content = page.evaluate("""() => {
        const selectors = [
            '#booktxt', '#content', '#chaptercontent',
            '.chapter-content', '.book-content', '.read-content',
            '.content', '#BookText', '.text-content', 'article'
        ];
        for (const sel of selectors) {
            const el = document.querySelector(sel);
            if (el && el.textContent.trim().length > 100) {
                return el.textContent.trim();
            }
        }
        const divs = document.querySelectorAll('div');
        let maxLen = 0, maxText = '';
        divs.forEach(div => {
            const text = div.textContent.trim();
            if (text.length > maxLen && text.length > 300) {
                maxLen = text.length;
                maxText = text;
            }
        });
        return maxText;
    }""")
    
    return (content or "")[:5000]


def main():
    flush_print("=== sjxs5.com 书籍爬取 v5 ===\n")
    
    results = []
    all_books = []
    seen_titles = set()
    
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        ctx = browser.new_context(
            user_agent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )
        page = ctx.new_page()
        page.set_default_timeout(15000)
        
        # Step 1: 从各分类获取书籍
        flush_print("Step 1: 获取分类书籍列表...")
        for cat_name, cat_url in CATEGORIES.items():
            flush_print(f"  分类: {cat_name}")
            try:
                books = extract_books_from_page(page, cat_url, cat_name)
                new_count = 0
                for b in books:
                    if b['title'] not in seen_titles and len(all_books) < 30:
                        seen_titles.add(b['title'])
                        all_books.append(b)
                        new_count += 1
                flush_print(f"    新增 {new_count} 本，总计 {len(all_books)} 本")
                # 每个分类最多5本
                if len(all_books) >= 30:
                    break
            except Exception as e:
                flush_print(f"    ❌ 错误: {e}")
        
        # 从首页补充
        if len(all_books) < 20:
            flush_print("\n  从首页补充...")
            try:
                home_books = extract_books_from_page(page, BASE_URL, "玄幻奇幻")
                for b in home_books:
                    if b['title'] not in seen_titles and len(all_books) < 25:
                        seen_titles.add(b['title'])
                        all_books.append(b)
            except:
                pass
        
        flush_print(f"\n共选取 {len(all_books)} 本书\n")
        
        # Step 2: 逐本获取章节 + 前10章内容
        flush_print("Step 2: 爬取章节和内容...")
        for i, book in enumerate(all_books):
            if len(results) >= 20:
                break
            
            title = book['title']
            author = book.get('author', '')
            href = book.get('href', '')
            category = book.get('category', '玄幻奇幻')
            
            flush_print(f"\n[{len(results)+1}/20] {title} ({author})")
            
            if not href or '/txt' not in href:
                flush_print(f"  ⚠️ 无有效URL，跳过")
                continue
            
            try:
                data = get_chapters_from_detail(page, href, MAX_CHAPTERS)
                chapters = data.get('chapters', [])
                description = data.get('description', '')
                cover = data.get('cover', '')
                
                if not chapters or len(chapters) < 3:
                    flush_print(f"  ❌ 章节数不足({len(chapters)})，跳过")
                    continue
                
                flush_print(f"  ✅ {len(chapters)} 章")
                for ch in chapters[:2]:
                    flush_print(f"     {ch['title']}")
                flush_print(f"     ...")
                flush_print(f"     {chapters[-1]['title']}")
                
                # 爬取前10章内容
                for j, ch in enumerate(chapters[:10]):
                    if ch.get('href'):
                        content = get_chapter_content(page, ch['href'])
                        ch['content'] = content[:3000] if content else ""
                        flush_print(f"     [{j+1}] {len(content)}字", )
                        time.sleep(0.2)
                
                results.append({
                    "title": title,
                    "author": author or "佚名",
                    "category": category,
                    "description": description[:300] if description else f"{title}是一本精彩的{category}小说。",
                    "cover": cover,
                    "url": href,
                    "chapters": chapters
                })
                
            except Exception as e:
                flush_print(f"  ❌ 错误: {e}")
                continue
        
        browser.close()
    
    # 保存
    with open(OUTPUT, 'w', encoding='utf-8') as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    
    total_ch = sum(len(r.get('chapters', [])) for r in results)
    with_content = sum(1 for ch_list in [r.get('chapters', []) for r in results] 
                       for ch in ch_list if ch.get('content'))
    flush_print(f"\n{'='*60}")
    flush_print(f"完成！{len(results)} 本书，{total_ch} 章节，{with_content} 章有内容")
    flush_print(f"保存到: {OUTPUT}")


if __name__ == "__main__":
    main()