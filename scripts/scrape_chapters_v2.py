#!/usr/bin/env python3
"""
从 sjxs5.com 爬取20本小说的真实章节名
用 Playwright headless=False 绕过反爬
"""

from playwright.sync_api import sync_playwright
import json, time, re

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


def search_and_find(page, title, author):
    """搜索并找到原著详情页URL"""
    # 导航到搜索
    page.goto(BASE_URL, wait_until="networkidle", timeout=15000)
    time.sleep(0.5)
    
    # 填搜索框并提交
    page.fill('input[type=text]', title)
    page.click('button:has-text("搜")')
    time.sleep(3)
    
    # 查找精确匹配的原著
    links = page.evaluate("""(args) => {
        const title = args[0];
        const author = args[1];
        const results = [];
        document.querySelectorAll('a').forEach(a => {
            const text = a.textContent.trim();
            const href = a.href;
            if (text === title || (text === title && href.includes('sjxs5'))) {
                // 检查附近是否有作者名
                const parent = a.closest('dl, li, div, dd');
                const parentText = parent ? parent.textContent : '';
                results.push({
                    text: text,
                    href: href,
                    hasAuthor: parentText.includes(author)
                });
            }
        });
        // 优先返回作者匹配的
        const authorMatch = results.filter(r => r.hasAuthor);
        if (authorMatch.length > 0) return authorMatch[0].href;
        const exact = results.filter(r => r.text === title);
        if (exact.length > 0) return exact[0].href;
        return null;
    }""", [title, author])
    
    return links


def get_chapters_from_detail(page, url, max_chapters=50):
    """从详情页获取章节列表"""
    page.goto(url, wait_until="networkidle", timeout=15000)
    time.sleep(1)
    
    chapters = page.evaluate("""(max) => {
        const all = [];
        // 尝试多种选择器找章节链接
        const chapterPatterns = [
            /^第.{1,5}章/,
            /^第.{1,5}节/,
            /^\\d+[\\.\\s、]/,
            /^Chapter\\s/i
        ];
        
        document.querySelectorAll('a').forEach(a => {
            const t = a.textContent.trim();
            const h = a.href;
            if (t.length > 2 && t.length < 50) {
                for (const pat of chapterPatterns) {
                    if (pat.test(t)) {
                        all.push({title: t, href: h});
                        break;
                    }
                }
            }
        });
        
        // 去重
        const seen = new Set();
        const unique = [];
        for (const ch of all) {
            if (!seen.has(ch.title)) {
                seen.add(ch.title);
                unique.push(ch);
            }
        }
        
        return unique.slice(0, max);
    }""", max_chapters)
    
    return chapters


def main():
    results = []
    
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=False)
        ctx = browser.new_context(
            user_agent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36"
        )
        page = ctx.new_page()
        
        for i, book in enumerate(BOOKS):
            print(f"\n[{i+1}/20] {book['title']} ({book['author']})")
            
            try:
                url = search_and_find(page, book['title'], book['author'])
                
                if not url:
                    print(f"  ❌ 未找到原著，尝试直接搜索")
                    results.append({"title": book['title'], "author": book['author'], "found": False, "chapters": []})
                    continue
                
                print(f"  ✅ URL: {url}")
                
                chapters = get_chapters_from_detail(page, url, MAX_CHAPTERS)
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