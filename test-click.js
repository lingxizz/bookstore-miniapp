const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1200, height: 900 } });
  const errors = [];
  page.on('pageerror', err => errors.push(err.message));

  await page.goto('file:///Users/zoulingxi/projects/bookstore-miniapp/prototype.html');
  await page.waitForTimeout(1000);

  // Test 1: Click a book cover in featured section
  const book = await page.locator('text=三体').first();
  await book.click();
  await page.waitForTimeout(500);
  await page.screenshot({ path: '/Users/zoulingxi/projects/bookstore-miniapp/screenshot-detail.png' });

  // Test 2: Click "立即阅读" button
  const readBtn = await page.locator('text=立即阅读');
  await readBtn.click();
  await page.waitForTimeout(500);
  await page.screenshot({ path: '/Users/zoulingxi/projects/bookstore-miniapp/screenshot-reader.png' });

  // Test 3: Click back
  const backBtn = await page.locator('text=‹').first();
  await backBtn.click();
  await page.waitForTimeout(300);

  // Test 4: Tab switch to 书城
  const storeTab = await page.locator('text=书城');
  await storeTab.click();
  await page.waitForTimeout(500);
  await page.screenshot({ path: '/Users/zoulingxi/projects/bookstore-miniapp/screenshot-store.png' });

  console.log('Errors:', errors.length ? errors : 'None');
  await browser.close();
})();
