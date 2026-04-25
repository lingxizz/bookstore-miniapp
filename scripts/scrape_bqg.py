#!/usr/bin/env python3
"""
Scrape 20 novels from 笔趣阁 (mfxs0.cn) - real chapter names and content.
Uses requests with proper session handling and cookies.
"""

import json
import re
import time
import requests
from bs4 import BeautifulSoup
from urllib.parse import quote

BASE_URL = "https://www.mfxs0.cn"
DELAY = 0.8  # seconds between requests
MAX_CHAPTERS = 50
MAX_CONTENT_CHARS = 2000
OUTPUT_PATH = "/Users/zoulingxi/projects/bookstore-miniapp/scripts/real_books_data.json"

BOOKS = [
    ("斗破苍穹", "天蚕土豆"),
    ("凡人修仙传", "忘语"),
    ("诡秘之主", "爱潜水的乌贼"),
    ("全职高手", "蝴蝶蓝"),
    ("庆余年", "猫腻"),
    ("遮天", "辰东"),
    ("鬼吹灯", "天下霸唱"),
    ("盗墓笔记", "南派三叔"),
    ("大奉打更人", "卖报小郎君"),
    ("斗罗大陆", "唐家三少"),
    ("雪中悍刀行", "烽火戏诸侯"),
    ("三体", "刘慈欣"),
    ("明朝那些事儿", "当年明月"),
    ("仙逆", "耳根"),
    ("吞噬星空", "我吃西红柿"),
    ("赘婿", "愤怒的香蕉"),
    ("剑来", "烽火戏诸侯"),
    ("修真聊天群", "圣骑士的传说"),
    ("全球高武", "老鹰吃小鸡"),
    ("我有一座冒险屋", "我会修空调"),
]

session = requests.Session()
session.headers.update({
    "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
    "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
    "Referer": BASE_URL,
})

# First visit homepage to get cookies
print("Initializing session...")
resp = session.get(BASE_URL, timeout=15)
print(f"Session init: {resp.status_code}")
time.sleep(1)


def fetch(url, retries=3):
    for attempt in range(retries):
        try:
            resp = session.get(url, timeout=15)
            resp.encoding = resp.apparent_encoding
            time.sleep(DELAY)
            if resp.status_code == 200:
                return resp
            elif resp.status_code == 403:
                print(f"  403 Forbidden for {url}, retrying...")
                time.sleep(3)
            else:
                print(f"  Status {resp.status_code} for {url}")
                time.sleep(1)
        except Exception as e:
            print(f"  Retry {attempt+1}/{retries}: {e}")
            time.sleep(2)
    return None


def search_book(title, author):
    """Search for a book and find the original work by matching author."""
    search_url = f"{BASE_URL}/so/{quote(title)}/"
    print(f"  Searching: {search_url}")
    resp = fetch(search_url)
    if not resp:
        return None
    
    soup = BeautifulSoup(resp.text, "lxml")
    results = []
    
    # Parse search results - find all h2/h3 headings with links
    for heading in soup.find_all(["h2", "h3"]):
        link = heading.find("a", href=True)
        if not link:
            continue
        book_title = link.get_text(strip=True)
        href = link.get("href", "")
        if not href.startswith("http"):
            href = BASE_URL + ("/" if not href.startswith("/") else "") + href
        
        # Find author link near this heading
        author_text = ""
        parent = heading.parent
        if parent:
            for a in parent.find_all("a", href=True):
                if "/author/" in a.get("href", ""):
                    author_text = a.get_text(strip=True)
                    break
        
        results.append({"title": book_title, "url": href, "author": author_text})
    
    # Remove duplicates
    seen = set()
    unique = []
    for r in results:
        if r["title"] not in seen:
            seen.add(r["title"])
            unique.append(r)
    
    # First try exact title match with exact author match
    for r in unique:
        if r["title"] == title and r["author"] == author:
            print(f"  Found exact match: {r['title']} by {r['author']} -> {r['url']}")
            return r["url"]
    
    # Then try exact title match (some authors might be listed slightly differently)
    for r in unique:
        if r["title"] == title and author in r.get("author", ""):
            print(f"  Found title match with author: {r['title']} by {r['author']} -> {r['url']}")
            return r["url"]
    
    # Then try title without "之", "之" suffix patterns - exact title only
    for r in unique:
        if r["title"] == title:
            print(f"  Found title match (author mismatch): {r['title']} by {r['author']} -> {r['url']}")
            return r["url"]
    
    print(f"  No match found. Search results:")
    for r in unique[:5]:
        print(f"    - {r['title']} by {r['author']}")
    
    return None


def get_chapter_list(book_url):
    """Get chapter list from book detail page."""
    print(f"  Getting chapters from: {book_url}")
    resp = fetch(book_url)
    if not resp:
        return []
    
    soup = BeautifulSoup(resp.text, "lxml")
    
    chapters = []
    
    # Primary: look for div#list or div.listmain containing chapter links
    for selector in ["div#list", "div.listmain", "div.mulu", "div.chapter-list", "div.book_list"]:
        list_div = soup.select_one(selector)
        if list_div:
            for a in list_div.find_all("a", href=True):
                href = a.get("href", "")
                title = a.get_text(strip=True)
                if title and href and re.match(r'^第.{1,5}[章节]|^Chapter|^chapter', title):
                    full_url = href if href.startswith("http") else BASE_URL + ("/" + href if not href.startswith("/") else href)
                    chapters.append({"title": title, "url": full_url})
            if chapters:
                break
    
    # Fallback: broader search for chapter links
    if not chapters:
        all_links = []
        for a in soup.find_all("a", href=True):
            href = a.get("href", "")
            title = a.get_text(strip=True)
            if title and href and (re.match(r'^第.{1,5}章', title) or re.match(r'^第.{1,3}节', title)):
                full_url = href if href.startswith("http") else BASE_URL + ("/" + href if not href.startswith("/") else href)
                all_links.append({"title": title, "url": full_url})
        
        if all_links:
            chapters = all_links
    
    # If still nothing, try without chapter prefix requirement
    if not chapters:
        # Get all links from the main content area
        content_area = soup.select_one("div.left, div.main, div.content")
        if content_area:
            for a in content_area.find_all("a", href=True):
                href = a.get("href", "")
                title = a.get_text(strip=True)
                if title and href and len(title) < 60:
                    # Skip nav links
                    if any(x in title for x in ["首页", "书架", "排行", "推荐", "书库", "全本", "作者"]):
                        continue
                    full_url = href if href.startswith("http") else BASE_URL + ("/" + href if not href.startswith("/") else href)
                    # Only include if URL contains chapter patterns
                    if re.search(r'/\d+\.html?', href) or '/chapter' in href.lower() or len(title) > 5:
                        chapters.append({"title": title, "url": full_url})
    
    # De-duplicate
    seen_urls = set()
    unique_chapters = []
    for ch in chapters:
        if ch["url"] not in seen_urls:
            seen_urls.add(ch["url"])
            unique_chapters.append(ch)
    
    print(f"  Found {len(unique_chapters)} chapters")
    return unique_chapters


def get_chapter_content(chapter_url):
    """Get content of a chapter."""
    resp = fetch(chapter_url)
    if not resp:
        return ""
    
    soup = BeautifulSoup(resp.text, "lxml")
    
    # Try various content selectors
    for selector in [
        "div#booktxt", "div#content", "div#BookText", 
        "div.chapter-content", "div.read-content", 
        "div.content", "div#chaptercontent",
        "div.text-content", "div.bookcontent", 
        "div.showtxt", "div#htmlContent",
        "div.readarea", "div.chapter_content",
        "div#nr1", "div#text-area",
    ]:
        content_el = soup.select_one(selector)
        if content_el:
            # Remove unwanted elements
            for tag in content_el.find_all(["script", "style"]):
                tag.decompose()
            # Remove ad divs
            for tag in content_el.find_all("div"):
                cls = " ".join(tag.get("class", []))
                if any(kw in cls.lower() for kw in ["ad", "recommend", "pg", "link", "share", "copyright"]):
                    tag.decompose()
            
            text = content_el.get_text(separator="\n").strip()
            if len(text) > 100:  # Reasonable minimum
                if len(text) > MAX_CONTENT_CHARS:
                    text = text[:MAX_CONTENT_CHARS]
                return text
    
    # Last resort: find the largest text block
    best_text = ""
    for div in soup.find_all("div"):
        div_text = div.get_text(strip=True)
        if len(div_text) > len(best_text) and len(div_text) > 200:
            # Make sure it's actual content, not navigation
            if len(div_text) > len(best_text):
                best_text = div_text
    
    if best_text and len(best_text) > 200:
        if len(best_text) > MAX_CONTENT_CHARS:
            best_text = best_text[:MAX_CONTENT_CHARS]
        return best_text
    
    return ""


def process_book(title, author):
    """Process a single book."""
    print(f"\n{'='*60}")
    print(f"Processing: {title} by {author}")
    print(f"{'='*60}")
    
    book_url = search_book(title, author)
    if not book_url:
        print(f"  SKIPPING: Book not found")
        return {"title": title, "author": author, "chapters": [], "error": "Book not found on site"}
    
    chapters = get_chapter_list(book_url)[:MAX_CHAPTERS]
    if not chapters:
        print(f"  SKIPPING: No chapters found")
        return {"title": title, "author": author, "chapters": [], "error": "No chapters found"}
    
    chapter_data = []
    for i, ch in enumerate(chapters):
        print(f"  Chapter {i+1}/{len(chapters)}: {ch['title']}")
        content = get_chapter_content(ch["url"])
        if content:
            chapter_data.append({"order": i + 1, "title": ch["title"], "content": content})
        else:
            chapter_data.append({"order": i + 1, "title": ch["title"], "content": ""})
            print(f"    WARNING: Empty content")
    
    successful = sum(1 for c in chapter_data if c["content"])
    print(f"  Done: {successful}/{len(chapter_data)} chapters with content")
    
    return {"title": title, "author": author, "chapters": chapter_data}


def main():
    results = {"books": []}
    
    for i, (title, author) in enumerate(BOOKS):
        print(f"\n[{i+1}/20] Processing: {title} by {author}")
        book_data = process_book(title, author)
        results["books"].append(book_data)
        
        # Save progress
        with open(OUTPUT_PATH, "w", encoding="utf-8") as f:
            json.dump(results, f, ensure_ascii=False, indent=2)
        print(f"  Saved progress ({i+1}/20)")
    
    print(f"\nDone! Saved to {OUTPUT_PATH}")
    for b in results["books"]:
        ch_count = len(b.get("chapters", []))
        non_empty = sum(1 for c in b.get("chapters", []) if c.get("content"))
        status = f"✓ {non_empty}/{ch_count}" if ch_count > 0 else f"✗ {b.get('error', 'unknown')}"
        print(f"  {b['title']} by {b['author']}: {status}")


if __name__ == "__main__":
    main()
