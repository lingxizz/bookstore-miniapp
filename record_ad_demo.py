import os
from playwright.sync_api import sync_playwright

VID_DIR = '/Users/zoulingxi/projects/bookstore-miniapp/videos'
os.makedirs(VID_DIR, exist_ok=True)

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(
        viewport={'width': 375, 'height': 812},
        record_video_dir=VID_DIR,
        record_video_size={'width': 375, 'height': 812}
    )
    page = context.new_page()

    # 1. 打开首页，等待自动登录
    page.goto('http://localhost:8080')
    page.wait_for_timeout(3000)

    # 2. 直接跳转到第4章（锁定章节）
    page.goto('http://localhost:8080/#/pages/reader/reader?bookId=1&chapterId=4')
    page.wait_for_timeout(3000)

    # 3. 点击"看广告解锁"，等待弹窗显示
    page.click('text=看广告解锁', timeout=5000)
    page.wait_for_timeout(2500)  # 等待弹窗完全显示

    # 4. 等待广告播放完成（剩余时间）
    page.wait_for_timeout(3000)

    # 5. 等待解锁成功后显示内容
    page.wait_for_timeout(2500)

    # 6. 滚动阅读
    page.mouse.wheel(0, 400)
    page.wait_for_timeout(1500)

    context.close()
    browser.close()

# 重命名视频
import glob
files = glob.glob(os.path.join(VID_DIR, '*.webm'))
if files:
    latest = max(files, key=os.path.getctime)
    target = os.path.join(VID_DIR, 'ad_unlock_demo.webm')
    os.rename(latest, target)
    print(f'Video saved: {target}')
else:
    print('No video found')
