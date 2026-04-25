#!/usr/bin/env python3
"""
从 sjxs5.com 爬取20本小说的真实章节名 v3
关键改进：搜索结果中精确匹配原著（书名===书名 && 作者是正确的）
"""
from playwright.sync_api import sync_playwright
import json, time, re, urllib.parse

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

MAX_CHAPTERS = 50
BASE_URL = "http://www.sjxs5.com"
OUTPUT = "/Users/zoulingxi/projects/bookstore-miniapp/scripts/real_chapters.json"


def find_book_in_search(page, title, author):
    """搜索页面后，从搜索结果中找到原著链接"""
    # 获取所有链接与周围文本（包含作者信息）
    results = page.evaluate("""(args) => {
        const title = args[0];
        const author = args[1];
        const allLinks = document.querySelectorAll('a');
        const candidates = [];
        
        for (const a of allLinks) {
            const text = a.textContent.trim();
            const href = a.href;
            
            // 精确匹配书名
            if (text === title && href.includes('/txt')) {
                // 获取周围的文本看是否包含作者名
                let context = '';
                let el = a;
                // 向上查找包含更多信息的容器
                for (let i = 0; i < 5; i++) {
                    el = el.parentElement;
                    if (!el) break;
                    context += ' ' + el.textContent;
                }
                
                candidates.push({
                    text: text,
                    href: href,
                    hasAuthor: context.includes(author),
                    // 越小的ID通常是原著（热门书ID小）
                    bookId: parseInt((href.match(/txt(\\d+)/) || [,'999999'])[1])
                });
            }
        }
        
        // 优先级：精确书名+作者匹配 > 精确书名> 小ID (原著一般是小ID)
        const withAuthor = candidates.filter(c => c.hasAuthor);
        if (withAuthor.length > 0) {
            withAuthor.sort((a, b) => a.bookId - b.bookId);
            return withAuthor[0].href;
        }
        
        const exact = candidates.filter(c => c.text === title);
        if (exact.length > 0) {
            exact.sort((a, b) => a.bookId - b.bookId);
            return exact[0].href;
        }
        
        return null;
    }""", [title, author])
    
    return results


def search_book(page, title, author):
    """搜索书籍，返回详情页URL"""
    page.goto(BASE_URL, wait_until="networkidle", timeout=15000)
    time.sleep(0.5)
    
    # 填搜索框
    search_input = page.locator('input[type=text], input[name=searchkey], input.sos')
    search_input.first.fill(title)
    search_input.first.press("Enter")
    time.sleep(3)
    
    url = find_book_in_search(page, title, author)
    return url


def get_chapters(page, url, max_chapters=50):
    """从书籍详情页获取章节列表"""
    page.goto(url, wait_until="networkidle", timeout=15000)
    time.sleep(1)
    
    chapters = page.evaluate("""(max) => {
        const all = [];
        const patterns = [
            /^第.{1,5}章/,
            /^第.{1,5}节/,
            /^Chapter\\s/i,
            /^\\d+[\\.\\s、]/
        ];
        
        const seen = new Set();
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
    page.goto(url, wait_until="networkidle", timeout=15000)
    time.sleep(0.5)
    
    content = page.evaluate("""() => {
        const selectors = [
            '#booktxt', '#content', '#chaptercontent', '.chapter-content',
            '.book-content', '.read-content', '.content', '#BookText',
            '.text-content', 'article'
        ];
        
        for (const sel of selectors) {
            const el = document.querySelector(sel);
            if (el && el.textContent.trim().length > 100) {
                return el.textContent.trim();
            }
        }
        
        // fallback: 找最长的div
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
    
    return (content or "")[:5000]


def main():
    results = []
    
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=False)
        ctx = browser.new_context(
            user_agent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )
        page = ctx.new_page()
        
        for i, book in enumerate(BOOKS):
            print(f"\n[{i+1}/20] {book['title']} ({book['author']})")
            
            try:
                url = search_book(page, book['title'], book['author'])
                
                if not url:
                    print(f"  ❌ 未找到原著")
                    # 尝试用作者搜索
                    print(f"  🔄 尝试用作者名搜索...")
                    url = search_book(page, book['author'], book['author'])
                    if url:
                        # 在作者页面找原著
                        url = find_book_in_search(page, book['title'], book['author'])
                
                if not url:
                    print(f"  ❌ 确实未找到")
                    results.append({"title": book['title'], "author": book['author'], "found": False, "chapters": []})
                    continue
                
                print(f"  ✅ URL: {url}")
                
                chapters = get_chapters(page, url, MAX_CHAPTERS)
                print(f"  📖 {len(chapters)} 章")
                
                if chapters:
                    for ch in chapters[:3]:
                        print(f"     {ch['title']}")
                    if len(chapters) > 3:
                        print(f"     ...")
                        print(f"     {chapters[-1]['title']}")
                
                results.append({
                    "title": book['title'],
                    "author": book['author'],
                    "found": True,
                    "url": url,
                    "chapters": chapters
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
            
            time.sleep(1)
        
        browser.close()
    
    with open(OUTPUT, 'w', encoding='utf-8') as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    
    found = sum(1 for r in results if r.get('found'))
    total_ch = sum(len(r.get('chapters', [])) for r in results)
    print(f"\n{'='*60}")
    print(f"完成！找到 {found}/20 本书，共 {total_ch} 个章节")
    print(f"结果保存到: {OUTPUT}")


if __name__ == "__main__":
    main()