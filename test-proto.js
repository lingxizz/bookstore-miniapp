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
  
  await page.screenshot({ path: '/Users/zoulingxi/projects/bookstore-miniapp/screenshot-home2.png' });
  
  const book = page.locator('text=三体').first();
  await book.click();
  await page.waitForTimeout(800);
  await page.screenshot({ path: '/Users/zoulingxi/projects/bookstore-miniapp/screenshot-detail2.png' });
  
  await page.locator('text=立即阅读').click();
  await page.waitForTimeout(800);
  await page.screenshot({ path: '/Users/zoulingxi/projects/bookstore-miniapp/screenshot-reader2.png' });
  
  await browser.close();
  console.log('Done');
})();
