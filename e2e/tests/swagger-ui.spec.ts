import { test, expect } from '@playwright/test';
import { SwaggerMainPage } from '../pages/SwaggerMainPage';
import { SwaggerAuthModal } from '../pages/SwaggerAuthModal';
import { SwaggerAccountSection } from '../pages/SwaggerAccountSection';

// Helper to create an isolated test user directly via API for test independence
async function createIsolatedUser(request: any) {
  const uniqueId = Date.now();
  const email = `e2e_user_${uniqueId}@example.com`;
  const password = 'password123';
  const fullName = `E2E Test User ${uniqueId}`;

  const response = await request.post('http://localhost:8080/api/users/register', {
    data: { email, password, fullName },
    headers: { 'Content-Type': 'application/json' }
  });

  expect(response.status()).toBe(201);
  const data = await response.json();
  return { email, password, token: data.token };
}

test.describe('Swagger UI - E2E UI Automation Suite', () => {

  test('TC-UI-01: Visual Layout & Structure of Swagger UI', async ({ page }) => {
    const swaggerPage = new SwaggerMainPage(page);

    // Navigate to Swagger UI
    await swaggerPage.goto();

    // Verify visual integrity of the main components
    await swaggerPage.verifyLoaded();

    // Verify all key tags are present
    await expect(swaggerPage.authTag).toBeVisible();
    await expect(swaggerPage.accountsTag).toBeVisible();
    await expect(swaggerPage.categoriesTag).toBeVisible();
    await expect(swaggerPage.transactionsTag).toBeVisible();

    // Verify page title and header
    await expect(page).toHaveTitle(/Swagger UI/i);
  });

  test('TC-UI-02: Interactive Authentication via Authorize Modal (POM)', async ({ page, request }) => {
    // Generate isolated user credentials
    const user = await createIsolatedUser(request);

    const swaggerPage = new SwaggerMainPage(page);
    const authModal = new SwaggerAuthModal(page);

    await swaggerPage.goto();
    await swaggerPage.verifyLoaded();

    // Open Authorize dialog
    await swaggerPage.openAuthorizeModal();
    await authModal.verifyVisible();

    // Enter Bearer token and confirm
    await authModal.setBearerToken(user.token);
    await authModal.close();

    // Verify lock icon indicates authorization
    await expect(page.locator('button.authorize svg.unlocked')).not.toBeVisible();
  });

  test('TC-UI-03: Create Account via Swagger UI with Explicit Waits', async ({ page, request }) => {
    // Isolated user setup
    const user = await createIsolatedUser(request);

    const swaggerPage = new SwaggerMainPage(page);
    const authModal = new SwaggerAuthModal(page);
    const accountSection = new SwaggerAccountSection(page);

    await swaggerPage.goto();
    await swaggerPage.openAuthorizeModal();
    await authModal.setBearerToken(user.token);
    await authModal.close();

    // Expand Accounts operation
    await accountSection.expand();
    await accountSection.clickTryItOut();

    // Fill userId query parameter and request body
    await accountSection.fillUserId('1');
    const accountName = `Checking Account ${Date.now()}`;
    await accountSection.fillRequestBody(accountName, 'CARD');

    // Execute with explicit network wait
    const response = await accountSection.execute();
    expect(response.status()).toBe(201);

    // Verify UI reflects success
    await accountSection.verifySuccessResponse('201');
  });

  test('TC-UI-04: Failure Hook and Screenshot Verification', async ({ page }) => {
    const swaggerPage = new SwaggerMainPage(page);
    await swaggerPage.goto();

    // Take an explicit screenshot to document test evidence
    await page.screenshot({ path: 'test-results/swagger-ui-verified.png', fullPage: true });
    expect(page.url()).toContain('/swagger-ui/');
  });
});
