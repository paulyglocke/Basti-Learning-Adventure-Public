const {defineConfig} = require('@playwright/test');
module.exports = defineConfig({
  testDir: './tests', workers: 1, timeout: 60000,
  use: {browserName: 'chromium', headless: true, viewport: {width: 360, height: 800}},
  reporter: 'list'
});
