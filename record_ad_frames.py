import os
from playwright.sync_api import sync_playwright

VID_DIR = '/Users/zoulingxi/projects/bookstore-miniapp/videos'
FRAMES_DIR = os.path.join(VID_DIR, 'frames')
os.makedirs(FRAMES_DIR, exist_ok=True)

# 清空旧帧
for f in os.listdir(FRAMES_DIR):
    os.remove(os.path.join(FRAMES_DIR, f))

frame_idx = 0
def snap(page):
    global frame_idx
    page.screenshot(path=os.path.join(FRAMES_DIR, f'frame_{frame_idx:04d}.png'))
    frame_idx += 1

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page(viewport={'width': 375, 'height': 812})

    # 1. 首页
    page.goto('http://localhost:8080')
    page.wait_for_timeout(2000)
    snap(page)

    # 2. 跳转阅读器第4章
    page.goto('http://localhost:8080/#/pages/reader/reader?bookId=1&chapterId=4')
    page.wait_for_timeout(2500)
    snap(page)
    snap(page)

    # 3. 点击解锁按钮
    page.click('text=看广告解锁', timeout=5000)
    
    # 广告播放期间每200ms截图
    for i in range(25):  # 5秒
        page.wait_for_timeout(200)
        snap(page)

    # 4. 解锁成功后
    page.wait_for_timeout(1500)
    snap(page)
    snap(page)

    # 5. 滚动
    page.mouse.wheel(0, 400)
    page.wait_for_timeout(500)
    snap(page)
    snap(page)

    browser.close()

print(f'Total frames: {frame_idx}')
