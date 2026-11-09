import { Page, Locator, expect } from '@playwright/test';

export class SwaggerAccountSection {
  readonly page: Page;
  readonly createAccountBlock: Locator;
  readonly tryItOutBtn: Locator;
  readonly userIdInput: Locator;
  readonly bodyTextarea: Locator;
  readonly executeBtn: Locator;
  readonly responseStatusCode: Locator;
  readonly responseBody: Locator;

  constructor(page: Page) {
    this.page = page;
    this.createAccountBlock = page.locator('#operations-Accounts-createAccount');
    this.tryItOutBtn = this.createAccountBlock.locator('button.try-out__btn');
    this.userIdInput = this.createAccountBlock.locator('input[placeholder="userId"]');
    this.bodyTextarea = this.createAccountBlock.locator('textarea.body-param__text');
    this.executeBtn = this.createAccountBlock.locator('button.execute');
    this.responseStatusCode = this.createAccountBlock.locator('tr.response td.response-col_status');
    this.responseBody = this.createAccountBlock.locator('.live-responses-table pre');
  }

  async expand() {
    await this.createAccountBlock.locator('.opblock-summary').click();
    await expect(this.tryItOutBtn).toBeVisible();
  }

  async clickTryItOut() {
    await this.tryItOutBtn.click();
    await expect(this.executeBtn).toBeVisible();
    await expect(this.bodyTextarea).toBeVisible();
  }

  async fillUserId(id: string = '1') {
    if (await this.userIdInput.isVisible()) {
      await this.userIdInput.fill(id);
    }
  }

  async fillRequestBody(accountName: string, accountType: string) {
    const payload = JSON.stringify({ name: accountName, type: accountType }, null, 2);
    await this.bodyTextarea.fill(payload);
  }

  async execute() {
    const responsePromise = this.page.waitForResponse(
      response => response.url().includes('/api/accounts') && response.request().method() === 'POST',
      { timeout: 15000 }
    );
    await this.executeBtn.click();
    return await responsePromise;
  }

  async verifySuccessResponse(expectedCode: string = '201') {
    await expect(this.responseStatusCode.first()).toContainText(expectedCode);
  }
}
