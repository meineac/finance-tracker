import { Page, Locator, expect } from '@playwright/test';

export class SwaggerAuthModal {
  readonly page: Page;
  readonly modal: Locator;
  readonly tokenInput: Locator;
  readonly authorizeSubmitButton: Locator;
  readonly closeButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.modal = page.locator('.modal-ux');
    this.tokenInput = page.locator('.auth-container input[type="text"]');
    this.authorizeSubmitButton = page.locator('button.modal-btn.auth.authorize');
    this.closeButton = page.locator('button.btn-done');
  }

  async verifyVisible() {
    await expect(this.modal).toBeVisible();
    await expect(this.tokenInput).toBeVisible();
  }

  async setBearerToken(token: string) {
    await this.tokenInput.fill(token);
    await this.authorizeSubmitButton.click();
  }

  async close() {
    await this.closeButton.click();
    await expect(this.modal).not.toBeVisible();
  }
}
