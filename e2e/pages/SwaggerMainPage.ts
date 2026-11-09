import { Page, Locator, expect } from '@playwright/test';

export class SwaggerMainPage {
  readonly page: Page;
  readonly title: Locator;
  readonly infoContainer: Locator;
  readonly topbar: Locator;
  readonly authorizeButton: Locator;
  readonly accountsTag: Locator;
  readonly transactionsTag: Locator;
  readonly categoriesTag: Locator;
  readonly authTag: Locator;

  constructor(page: Page) {
    this.page = page;
    this.topbar = page.locator('.topbar');
    this.title = page.locator('.title');
    this.infoContainer = page.locator('.information-container');
    this.authorizeButton = page.locator('button.authorize');
    this.accountsTag = page.locator('#operations-tag-Accounts');
    this.transactionsTag = page.locator('#operations-tag-Transactions');
    this.categoriesTag = page.locator('#operations-tag-Transaction_Categories');
    this.authTag = page.locator('#operations-tag-Authentication');
  }

  async goto() {
    await this.page.goto('/swagger-ui/index.html');
    await this.page.waitForLoadState('domcontentloaded');
  }

  async verifyLoaded() {
    await expect(this.title).toBeVisible({ timeout: 10000 });
    await expect(this.infoContainer).toContainText('Personal Finance Tracker API');
    await expect(this.authorizeButton).toBeVisible();
    await expect(this.accountsTag).toBeVisible();
  }

  async openAuthorizeModal() {
    await expect(this.authorizeButton).toBeVisible();
    await this.authorizeButton.click();
  }
}
