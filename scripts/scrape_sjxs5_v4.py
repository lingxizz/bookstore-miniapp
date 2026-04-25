#!/usr/bin/env python3
"""
从 sjxs5.com 各分类爬取书籍和章节
v4: 从每个分类页面获取推荐书籍，然后爬取每本书的章节名
"""
from playwright.sync_api import sync_playwright
import json, time, re, sys, urllib.parse

BASE_URL = "http://www.sjxs5.com"
MAX_CHAPTERS = 50  # 每本书最多50章
OUTPUT = "/Users/zoulingxi/projects/bookstore-miniapp/scripts/real_chapters.json"

# 分类映射：sjxs5分类 -> 我们的分类
CATEGORY_MAP = {
    "玄幻": "玄幻奇幻",
    "武侠": "武侠仙侠",
    "言情": "言情",
    "穿越": "穿越重生",
    "现代": "都市生活",
    "惊悚": "悬疑惊悚",
    "科幻": "科幻",
    "历史": "历史",
    "网游": "游戏竞技",
}


def get_category_books(page, category_url, category_name, max_books=3):
    """从分类页面获取书籍列表"""
    page.goto(category_url, wait_until="networkidle", timeout=15000)
    time.sleep(1)
    
    books = page.evaluate("""(args) => {
        const maxBooks = args[0];
        const results = [];
        const seen = new Set();
        
        // 获取页面上所有书籍链接
        document.querySelectorAll('a').forEach(a => {
            const text = a.textContent.trim();
            const href = a.href;
            if (href.includes('/txt') && seen.size < maxBooks && !seen.has(text) && text.length > 1 && text.length < 30) {
                // 尝试获取作者名
                const parent = a.closest('dl, li, div');
                let author = '';
                if (parent) {
                    const authorEl = parent.querySelector('a[href*="/author/"]');
                    if (authorEl) author = authorEl.textContent.trim();
                    else {
                        // 作者名通常在链接旁边
                        const siblings = parent.querySelectorAll('a, span');
                        for (const s of siblings) {
                            if (s.href && s.href.includes('/author/')) {
                                author = s.textContent.trim();
                                break;
                            }
                        }
                    }
                }
                seen.add(text);
                results.push({title: text, href: href, author: author});
            }
        });
        return results;
    }""", [max_books])
    
    return books


def get_homepage_books(page, max_per_category=3):
    """从首页各分类区块获取书籍"""
    page.goto(BASE_URL, wait_until="networkidle", timeout=15000)
    time.sleep(1)
    
    books = page.evaluate("""(maxPerCat) => {
        const results = [];
        const seen = new Set();
        
        // 首页各分类区块的标题和列表
        const headings = document.querySelectorAll('h2, h3');
        for (const h of headings) {
            const catName = h.textContent.trim();
            if (!catName || catName.length > 10) continue;
            
            // 找到该标题后面的列表
            let container = h.parentElement;
            if (!container) continue;
            
            // 获取该书名和作者
            const list = container.querySelector('ul, ol') || container.nextElementSibling;
            if (!list) continue;
            
            const items = list.querySelectorAll('li');
            let count = 0;
            for (const li of items) {
                if (count >= maxPerCat || results.length >= 30) break;
                const links = li.querySelectorAll('a');
                if (links.length >= 1) {
                    const title = links[0].textContent.trim();
                    const href = links[0].href;
                    let author = '';
                    if (links.length >= 2) {
                        author = links[1].textContent.trim();
                    } else {
                        // 作者可能在文本中
                        const text = li.textContent.trim();
                        const match = text.match(/[·\s]+([^\s·]+)$/);
                        if (match) author = match[1];
                    }
                    
                    if (title && href && href.includes('/txt') && !seen.has(title)) {
                        seen.add(title);
                        results.push({
                            category: catName,
                            title: title,
                            href: href,
                            author: author,
                        });
                        count++;
                    }
                }
            }
        }
        
        return results;
    }""", max_per_category)
    
    return books


def get_chapters(page, url, max_chapters=50):
    """从书籍详情页获取章节列表和内容"""
    try:
        page.goto(url, wait_until="networkidle", timeout=15000)
        time.sleep(0.5)
    except:
        return []
    
    chapters = page.evaluate("""(max) => {
        const all = [];
        const seen = new Set();
        const patterns = [
            /^第.{1,5}章/,
            /^第.{1,5}节/,
            /^Chapter\\s/i,
        ];
        
        document.querySelectorAll('a').forEach(a => {
            const t = a.textContent.trim();
            const h = a.href;
            if (t.length > 2 && t.length < 80 && !seen.has(t)) {
                for (const pat of patterns) {
                    if (pat.test(t)) {
                        seen.add(t);
                        all.push({title: t, href: h});
                        break;
                    }
                }
            }
        });
        
        return all.slice(0, max);
    }""", max_chapters)
    
    return chapters


def get_chapter_content(page, url):
    """获取单个章节的内容"""
    try:
        page.goto(url, wait_until="networkidle", timeout=10000)
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
        
        // fallback: 最长的div
        const divs = document.querySelectorAll('div');
        let maxLen = 0, maxText = '';
        divs.forEach(div => {
            const text = div.textContent.trim();
            if (text.length > maxLen && text.length > 200) {
                maxLen = text.length;
                maxText = text;
            }
        });
        return maxText;
    }""")
    
    return (content or "")[:5000]


def get_book_description(page, url):
    """从书籍详情页获取描述"""
    try:
        page.goto(url, wait_until="networkidle", timeout=15000)
        time.sleep(0.5)
    except:
        return ""
    
    desc = page.evaluate("""() => {
        // 尝试获取简介
        const selectors = ['.intro', '.description', '.book-intro', '#bookintro', '.summary', '.book-desc'];
        for (const sel of selectors) {
            const el = document.querySelector(sel);
            if (el && el.textContent.trim().length > 20) {
                return el.textContent.trim().substring(0, 500);
            }
        }
        
        // 从meta标签获取
        const meta = document.querySelector('meta[name="description"]');
        if (meta) return meta.content.trim().substring(0, 500);
        
        return '';
    }""")
    
    return desc or ""


def main():
    results = []
    
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        ctx = browser.new_context(
            user_agent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )
        page = ctx.new_page()
        page.set_default_timeout(15000)
        
        # Step 1: 从首页获取各分类推荐书籍
        print("=" * 60)
        print("Step 1: 从首页获取书籍列表...")
        print("=" * 60)
        homepage_books = get_homepage_books(page, max_per_category=3)
        print(f"首页找到 {len(homepage_books)} 本书")
        
        # 也从各分类页面获取更多书
        categories = [
            ("玄幻", "http://www.sjxs5.com/sort7/", "玄幻奇幻"),
            ("武侠仙侠", "http://www.sjxs5.com/sort9/", "武侠仙侠"),
            ("悬疑惊悚", "http://www.sjxs5.com/sort11/", "悬疑惊悚"),
            ("科幻", "http://www.sjxs5.com/sort12/", "科幻"),
            ("历史", "http://www.sjxs5.com/sort6/", "历史"),
            ("网游", "http://www.sjxs5.com/sort13/", "游戏竞技"),
        ]
        
        all_books = list(homepage_books)
        
        # 用set去重
        seen_titles = {b['title'] for b in all_books}
        
        for cat_name, cat_url, db_category in categories:
            print(f"\n获取分类: {cat_name} ({cat_url})")
            try:
                cat_books = get_category_books(page, cat_url, cat_name, max_books=3)
                for b in cat_books:
                    if b['title'] not in seen_titles:
                        b['category'] = db_category
                        all_books.append(b)
                        seen_titles.add(b['title'])
                print(f"  找到 {len(cat_books)} 本新书")
            except Exception as e:
                print(f"  ❌ 错误: {e}")
        
        # 限制总数到20本
        all_books = all_books[:25]  # 多取5本，避免某些失败后不够
        
        print(f"\n共选取 {len(all_books)} 本书待爬取")
        print("=" * 60)
        
        # Step 2: 逐本获取章节
        print("\nStep 2: 爬取章节列表...")
        print("=" * 60)
        
        for i, book in enumerate(all_books):
            if len(results) >= 20:
                break
                
            title = book.get('title', 'Unknown')
            author = book.get('author', '')
            url = book.get('href', '')
            category = book.get('category', '玄幻奇幻')
            
            print(f"\n[{i+1}/{len(all_books)}] {title} ({author})")
            
            if not url or '/txt' not in url:
                print(f"  ⚠️ 无有效URL，跳过")
                continue
            
            try:
                chapters = get_chapters(page, url, MAX_CHAPTERS)
                
                if not chapters:
                    print(f"  ❌ 无章节，跳过")
                    continue
                
                print(f"  ✅ {len(chapters)} 章")
                for ch in chapters[:3]:
                    print(f"     {ch['title']}")
                if len(chapters) > 3:
                    print(f"     ...")
                    print(f"     {chapters[-1]['title']}")
                
                # 获取简介
                desc = get_book_description(page, url)
                
                results.append({
                    "title": title,
                    "author": author or "佚名",
                    "category": category,
                    "description": desc[:300] if desc else f"{title}是一本精彩的网络小说。",
                    "url": url,
                    "chapters": chapters,
                })
                
            except Exception as e:
                print(f"  ❌ 错误: {e}")
                continue
            
            time.sleep(0.5)
        
        # Step 3: 爬取每本书前10章的内容
        print("\n\n" + "=" * 60)
        print(f"Step 3: 爬取章节内容（每本书前10章）...")
        print("=" * 60)
        
        for i, book in enumerate(results):
            print(f"\n[{i+1}/{len(results)}] {book['title']}")
            chapters_with_content = []
            
            # 只爬前10章内容（太多了会超时）
            for j, ch in enumerate(book['chapters'][:10]):
                if not ch.get('href'):
                    chapters_with_content.append(ch)
                    continue
                    
                print(f"  [{j+1}] {ch['title']}...", end=" ", flush=True)
                content = get_chapter_content(page, ch['href'])
                ch['content_preview'] = content[:2000] if content else ""
                chapters_with_content.append(ch)
                print(f"{'✅' if content else '❌'} {len(content)}字")
                time.sleep(0.3)
            
            book['chapters'] = chapters_with_content + book['chapters'][10:]
        
        browser.close()
    
    # 保存结果
    with open(OUTPUT, 'w', encoding='utf-8') as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    
    found = len(results)
    total_ch = sum(len(r.get('chapters', [])) for r in results)
    print(f"\n{'='*60}")
    print(f"完成！共 {found} 本书，{total_ch} 个章节")
    print(f"结果保存到: {OUTPUT}")


if __name__ == "__main__":
    main()