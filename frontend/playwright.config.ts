import { defineConfig } from "@playwright/test";
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false,
  workers: 1,
  timeout: 180000,
  expect: { timeout: 15000 },
  reporter: [["list"], ["json", { outputFile: "test-results/results.json" }]],
  use: {
    baseURL: "http://127.0.0.1:8093",
    channel: "chromium",
    headless: true,
    actionTimeout: 15000,
    navigationTimeout: 15000,
    viewport: { width: 1280, height: 900 },
    trace: "off",
    screenshot: "only-on-failure",
  },
  webServer: {
    command: "node e2e/server.mjs",
    url: "http://127.0.0.1:8093/api/auth/session",
    timeout: 60000,
    reuseExistingServer: process.env["E2E_REUSE_SERVER"] === "true",
  },
});
