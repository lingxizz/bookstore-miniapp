const { chromium } = require('/usr/local/lib/node_modules/playwright');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1200, height: 900 } });
  
  page.on('console', msg => {
    if (msg.type() === 'error') console.log('CONSOLE ERROR:', msg.text());
  });
  page.on('pageerror', err => console.log('PAGE ERROR:', err.message));
  
  await page.goto('file:///Users/zoulingxi/projects/bookstore-miniapp/prototype.html', { waitUntil: 'networkidle' });
  await page.waitForTimeout(3000);
  
  // Go to reader
  await page.locator('text=三体').first().click();
  await page.waitForTimeout(500);
  await page.locator('text=立即阅读').click();
  await page.waitForTimeout(500);
  
  // Scroll to bottom to trigger paywall - use JS
  await page.evaluate(() => {
    const el = document.querySelectorAll('.hide-scroll')[1];
    if (el) el.scrollTo(0, el.scrollHeight);
  });
  await page.waitForTimeout(800);
  await page.screenshot({ path: '/Users/zoulingxi/projects/bookstore-miniapp/screenshot-paywall.png' });
  
  // Click recharge in paywall
  await page.locator('text=充值获取金币').click();
  await page.waitForTimeout(800);
  await page.screenshot({ path: '/Users/zoulingxi/projects/bookstore-miniapp/screenshot-recharge.png' });
  
  // Click back
  await page.locator('text=‹').first().click();
  await page.waitForTimeout(500);
  await page.screenshot({ path: '/Users/zoulingxi/projects/bookstore-miniapp/screenshot-back.png' });
  
  // Tab: 书城
  await page.locator('text=书城').click();
  await page.waitForTimeout(500);
  await page.screenshot({ path: '/Users/zoulingxi/projects/bookstore-miniapp/screenshot-store.png' });
  
  // Tab: 书架
  await page.locator('text=书架').click();
  await page.waitForTimeout(500);
  await page.screenshot({ path: '/Users/zoulingxi/projects/bookstore-miniapp/screenshot-shelf.png' });
  
  // Tab: 我的
  await page.locator('text=我的').click();
  await page.waitForTimeout(500);
  await page.screenshot({ path: '/Users/zoulingxi/projects/bookstore-miniapp/screenshot-me.png' });
  
  await browser.close();
  console.log('All flow tests passed');
})();
