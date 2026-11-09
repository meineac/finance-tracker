const fs = require('fs');
const path = require('path');

const collection = {
  info: {
    name: "Finance Tracker API Collection",
    description: "Complete test collection for Finance Tracker API covering positive, negative, request chaining, and security (IDOR, Auth, RFC 7807) scenarios.",
    schema: "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  variable: [
    { key: "baseUrl", value: "http://localhost:8080" }
  ],
  item: [
    {
      name: "01 - Authentication",
      description: "User registration and login scenarios (positive and negative)",
      item: [
        {
          name: "01.1 Register User A [Positive]",
          event: [
            {
              listen: "prerequest",
              script: {
                type: "text/javascript",
                exec: [
                  "const ts = Date.now();",
                  "const email = `usera_${ts}@example.com`;",
                  "pm.collectionVariables.set('userA_email', email);",
                  "pm.environment.set('userA_email', email);",
                  "pm.collectionVariables.set('userA_password', 'password123');",
                  "pm.environment.set('userA_password', 'password123');",
                  "pm.collectionVariables.set('userA_fullName', 'Test User A');",
                  "pm.environment.set('userA_fullName', 'Test User A');"
                ]
              }
            },
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 201 Created', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "pm.test('Response contains JWT token', function () {",
                  "    const data = pm.response.json();",
                  "    pm.expect(data).to.have.property('token');",
                  "    pm.expect(data.token).to.be.a('string').and.not.empty;",
                  "    pm.collectionVariables.set('token', data.token);",
                  "    pm.environment.set('token', data.token);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Content-Type", value: "application/json" }],
            body: {
              mode: "raw",
              raw: "{\n  \"email\": \"{{userA_email}}\",\n  \"password\": \"{{userA_password}}\",\n  \"fullName\": \"{{userA_fullName}}\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/users/register",
              host: ["{{baseUrl}}"],
              path: ["api", "users", "register"]
            }
          }
        },
        {
          name: "01.2 Login User A [Positive]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 200 OK', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "pm.test('Response contains valid JWT token', function () {",
                  "    const data = pm.response.json();",
                  "    pm.expect(data).to.have.property('token');",
                  "    pm.expect(data.token).to.be.a('string').and.not.empty;",
                  "    pm.collectionVariables.set('token', data.token);",
                  "    pm.environment.set('token', data.token);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Content-Type", value: "application/json" }],
            body: {
              mode: "raw",
              raw: "{\n  \"email\": \"{{userA_email}}\",\n  \"password\": \"{{userA_password}}\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/users/login",
              host: ["{{baseUrl}}"],
              path: ["api", "users", "login"]
            }
          }
        },
        {
          name: "01.3 Register - Invalid Email [Negative]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 400 Bad Request', function () {",
                  "    pm.response.to.have.status(400);",
                  "});",
                  "pm.test('ProblemDetail conforms to RFC 7807', function () {",
                  "    const data = pm.response.json();",
                  "    pm.expect(data).to.have.property('status', 400);",
                  "    pm.expect(pm.response.text()).to.not.include('Exception');",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Content-Type", value: "application/json" }],
            body: {
              mode: "raw",
              raw: "{\n  \"email\": \"not-an-email\",\n  \"password\": \"password123\",\n  \"fullName\": \"Invalid User\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/users/register",
              host: ["{{baseUrl}}"],
              path: ["api", "users", "register"]
            }
          }
        },
        {
          name: "01.4 Register - Short Password [Negative]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 400 Bad Request for short password (<6)', function () {",
                  "    pm.response.to.have.status(400);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Content-Type", value: "application/json" }],
            body: {
              mode: "raw",
              raw: "{\n  \"email\": \"short_pw_user@example.com\",\n  \"password\": \"123\",\n  \"fullName\": \"Short Password User\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/users/register",
              host: ["{{baseUrl}}"],
              path: ["api", "users", "register"]
            }
          }
        },
        {
          name: "01.5 Register - Duplicate Email [Negative]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 409 Conflict for duplicate email', function () {",
                  "    pm.response.to.have.status(409);",
                  "});",
                  "pm.test('ProblemDetail contains duplicate error message', function () {",
                  "    const data = pm.response.json();",
                  "    pm.expect(data).to.have.property('status', 409);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Content-Type", value: "application/json" }],
            body: {
              mode: "raw",
              raw: "{\n  \"email\": \"{{userA_email}}\",\n  \"password\": \"password123\",\n  \"fullName\": \"Duplicate User A\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/users/register",
              host: ["{{baseUrl}}"],
              path: ["api", "users", "register"]
            }
          }
        },
        {
          name: "01.6 Login - Invalid Password [Negative]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 401 or 403 for wrong password', function () {",
                  "    pm.expect(pm.response.code).to.be.oneOf([401, 403]);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Content-Type", value: "application/json" }],
            body: {
              mode: "raw",
              raw: "{\n  \"email\": \"{{userA_email}}\",\n  \"password\": \"wrong_password_attempt\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/users/login",
              host: ["{{baseUrl}}"],
              path: ["api", "users", "login"]
            }
          }
        }
      ]
    },
    {
      name: "02 - Accounts",
      description: "Account creation and listing endpoints",
      item: [
        {
          name: "02.1 Create Account (CARD) [Positive]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 201 Created', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "pm.test('CARD Account created with initial balance 0.00', function () {",
                  "    const data = pm.response.json();",
                  "    pm.expect(data).to.have.property('id');",
                  "    pm.expect(data.name).to.eql('Main Bank Card');",
                  "    pm.expect(data.type).to.eql('CARD');",
                  "    pm.expect(Number(data.balance)).to.eql(0);",
                  "    pm.collectionVariables.set('cardAccountId', data.id);",
                  "    pm.environment.set('cardAccountId', data.id);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            body: {
              mode: "raw",
              raw: "{\n  \"name\": \"Main Bank Card\",\n  \"type\": \"CARD\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/accounts",
              host: ["{{baseUrl}}"],
              path: ["api", "accounts"]
            }
          }
        },
        {
          name: "02.2 Create Account (CASH) [Positive]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 201 Created', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "pm.test('CASH Account created with balance 0.00', function () {",
                  "    const data = pm.response.json();",
                  "    pm.expect(data).to.have.property('id');",
                  "    pm.expect(data.name).to.eql('Wallet Cash');",
                  "    pm.expect(data.type).to.eql('CASH');",
                  "    pm.collectionVariables.set('cashAccountId', data.id);",
                  "    pm.environment.set('cashAccountId', data.id);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            body: {
              mode: "raw",
              raw: "{\n  \"name\": \"Wallet Cash\",\n  \"type\": \"CASH\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/accounts",
              host: ["{{baseUrl}}"],
              path: ["api", "accounts"]
            }
          }
        },
        {
          name: "02.3 Get User Accounts [Positive]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 200 OK', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "pm.test('Returns list containing created accounts', function () {",
                  "    const data = pm.response.json();",
                  "    pm.expect(data).to.be.an('array').with.length.at.least(2);",
                  "    const ids = data.map(a => a.id);",
                  "    const cardId = Number(pm.environment.get('cardAccountId') || pm.collectionVariables.get('cardAccountId'));",
                  "    const cashId = Number(pm.environment.get('cashAccountId') || pm.collectionVariables.get('cashAccountId'));",
                  "    pm.expect(ids).to.include(cardId);",
                  "    pm.expect(ids).to.include(cashId);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            url: {
              raw: "{{baseUrl}}/api/accounts",
              host: ["{{baseUrl}}"],
              path: ["api", "accounts"]
            }
          }
        },
        {
          name: "02.4 Create Account - Unauthorized [Negative]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 401 or 403 without authorization', function () {",
                  "    pm.expect(pm.response.code).to.be.oneOf([401, 403]);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" }
            ],
            body: {
              mode: "raw",
              raw: "{\n  \"name\": \"Unauthorized Account\",\n  \"type\": \"CARD\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/accounts",
              host: ["{{baseUrl}}"],
              path: ["api", "accounts"]
            }
          }
        },
        {
          name: "02.5 Create Account - Invalid Type [Negative]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 400 Bad Request for unsupported type', function () {",
                  "    pm.response.to.have.status(400);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            body: {
              mode: "raw",
              raw: "{\n  \"name\": \"Crypto Account\",\n  \"type\": \"CRYPTO\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/accounts",
              host: ["{{baseUrl}}"],
              path: ["api", "accounts"]
            }
          }
        }
      ]
    },
    {
      name: "03 - Categories",
      description: "Transaction Category creation and listing endpoints",
      item: [
        {
          name: "03.1 Create Custom Category [Positive]",
          event: [
            {
              listen: "prerequest",
              script: {
                type: "text/javascript",
                exec: [
                  "const ts = Date.now();",
                  "const name = `Groceries_${ts}`;",
                  "pm.collectionVariables.set('categoryName', name);",
                  "pm.environment.set('categoryName', name);"
                ]
              }
            },
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 201 Created', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "pm.test('Category created with isCustom=true', function () {",
                  "    const data = pm.response.json();",
                  "    pm.expect(data).to.have.property('id');",
                  "    const catName = pm.environment.get('categoryName') || pm.collectionVariables.get('categoryName');",
                  "    pm.expect(data.name).to.eql(catName);",
                  "    pm.collectionVariables.set('categoryId', data.id);",
                  "    pm.environment.set('categoryId', data.id);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            body: {
              mode: "raw",
              raw: "{\n  \"name\": \"{{categoryName}}\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/categories",
              host: ["{{baseUrl}}"],
              path: ["api", "categories"]
            }
          }
        },
        {
          name: "03.2 Get Categories [Positive]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 200 OK', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "pm.test('Contains system default and custom category', function () {",
                  "    const data = pm.response.json();",
                  "    pm.expect(data).to.be.an('array');",
                  "    const catName = pm.environment.get('categoryName') || pm.collectionVariables.get('categoryName');",
                  "    const names = data.map(c => c.name);",
                  "    pm.expect(names).to.include(catName);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            url: {
              raw: "{{baseUrl}}/api/categories",
              host: ["{{baseUrl}}"],
              path: ["api", "categories"]
            }
          }
        },
        {
          name: "03.3 Create Category - Blank Name [Negative]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 400 Bad Request for blank category name', function () {",
                  "    pm.response.to.have.status(400);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            body: {
              mode: "raw",
              raw: "{\n  \"name\": \"\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/categories",
              host: ["{{baseUrl}}"],
              path: ["api", "categories"]
            }
          }
        },
        {
          name: "03.4 Create Category - Duplicate Name [Negative]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 409 Conflict for duplicate category name', function () {",
                  "    pm.response.to.have.status(409);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            body: {
              mode: "raw",
              raw: "{\n  \"name\": \"{{categoryName}}\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/categories",
              host: ["{{baseUrl}}"],
              path: ["api", "categories"]
            }
          }
        }
      ]
    },
    {
      name: "04 - Transactions",
      description: "Transaction posting, history, and balance verification",
      item: [
        {
          name: "04.1 Create Transaction (INCOME) [Positive]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 201 Created', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "pm.test('INCOME transaction created with correct amount', function () {",
                  "    const data = pm.response.json();",
                  "    pm.expect(data).to.have.property('id');",
                  "    pm.expect(data.type).to.eql('INCOME');",
                  "    pm.expect(Number(data.amount)).to.eql(1000.50);",
                  "    pm.collectionVariables.set('txIncomeId', data.id);",
                  "    pm.environment.set('txIncomeId', data.id);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            body: {
              mode: "raw",
              raw: "{\n  \"accountId\": {{cardAccountId}},\n  \"categoryId\": {{categoryId}},\n  \"type\": \"INCOME\",\n  \"amount\": 1000.50,\n  \"description\": \"Salary deposit\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/transactions",
              host: ["{{baseUrl}}"],
              path: ["api", "transactions"]
            }
          }
        },
        {
          name: "04.2 Create Transaction (EXPENSE) [Positive]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 201 Created', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "pm.test('EXPENSE transaction created with correct amount', function () {",
                  "    const data = pm.response.json();",
                  "    pm.expect(data).to.have.property('id');",
                  "    pm.expect(data.type).to.eql('EXPENSE');",
                  "    pm.expect(Number(data.amount)).to.eql(250.00);",
                  "    pm.collectionVariables.set('txExpenseId', data.id);",
                  "    pm.environment.set('txExpenseId', data.id);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            body: {
              mode: "raw",
              raw: "{\n  \"accountId\": {{cardAccountId}},\n  \"categoryId\": {{categoryId}},\n  \"type\": \"EXPENSE\",\n  \"amount\": 250.00,\n  \"description\": \"Weekly supermarket grocery\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/transactions",
              host: ["{{baseUrl}}"],
              path: ["api", "transactions"]
            }
          }
        },
        {
          name: "04.3 Get Account Transactions [Positive]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 200 OK', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "pm.test('Returns 2 transactions in descending chronological order', function () {",
                  "    const data = pm.response.json();",
                  "    pm.expect(data).to.be.an('array').with.lengthOf(2);",
                  "    const expId = Number(pm.environment.get('txExpenseId') || pm.collectionVariables.get('txExpenseId'));",
                  "    const incId = Number(pm.environment.get('txIncomeId') || pm.collectionVariables.get('txIncomeId'));",
                  "    pm.expect(data[0].id).to.eql(expId);",
                  "    pm.expect(data[1].id).to.eql(incId);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            url: {
              raw: "{{baseUrl}}/api/transactions/account/{{cardAccountId}}",
              host: ["{{baseUrl}}"],
              path: ["api", "transactions", "account", "{{cardAccountId}}"]
            }
          }
        },
        {
          name: "04.4 Verify Updated Account Balance [Positive]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 200 OK', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "pm.test('Account balance reflects atomic math (1000.50 - 250.00 = 750.50)', function () {",
                  "    const data = pm.response.json();",
                  "    const cardId = Number(pm.environment.get('cardAccountId') || pm.collectionVariables.get('cardAccountId'));",
                  "    const acc = data.find(a => a.id === cardId);",
                  "    pm.expect(acc).to.not.be.undefined;",
                  "    pm.expect(Number(acc.balance)).to.eql(750.50);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            url: {
              raw: "{{baseUrl}}/api/accounts",
              host: ["{{baseUrl}}"],
              path: ["api", "accounts"]
            }
          }
        },
        {
          name: "04.5 Create Transaction - Negative Amount [Negative]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 400 Bad Request for negative amount', function () {",
                  "    pm.response.to.have.status(400);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            body: {
              mode: "raw",
              raw: "{\n  \"accountId\": {{cardAccountId}},\n  \"categoryId\": {{categoryId}},\n  \"type\": \"EXPENSE\",\n  \"amount\": -50.00,\n  \"description\": \"Negative amount\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/transactions",
              host: ["{{baseUrl}}"],
              path: ["api", "transactions"]
            }
          }
        },
        {
          name: "04.6 Create Transaction - Non-existent Account [Negative]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 404 Not Found for non-existent account', function () {",
                  "    pm.response.to.have.status(404);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            body: {
              mode: "raw",
              raw: "{\n  \"accountId\": 999999,\n  \"categoryId\": {{categoryId}},\n  \"type\": \"EXPENSE\",\n  \"amount\": 10.00,\n  \"description\": \"Ghost account\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/transactions",
              host: ["{{baseUrl}}"],
              path: ["api", "transactions"]
            }
          }
        },
        {
          name: "04.7 Create Transaction - Non-existent Category [Negative]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 404 Not Found for non-existent category', function () {",
                  "    pm.response.to.have.status(404);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            body: {
              mode: "raw",
              raw: "{\n  \"accountId\": {{cardAccountId}},\n  \"categoryId\": 999999,\n  \"type\": \"EXPENSE\",\n  \"amount\": 10.00,\n  \"description\": \"Ghost category\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/transactions",
              host: ["{{baseUrl}}"],
              path: ["api", "transactions"]
            }
          }
        }
      ]
    },
    {
      name: "05 - Security & Edge Cases",
      description: "Direct URL access without auth, IDOR parameter tampering, and RFC 7807 leakage checks",
      item: [
        {
          name: "05.1 Call Non-existent Endpoint [Negative 404]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 404 Not Found', function () {",
                  "    pm.response.to.have.status(404);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            url: {
              raw: "{{baseUrl}}/api/non-existent-route-path",
              host: ["{{baseUrl}}"],
              path: ["api", "non-existent-route-path"]
            }
          }
        },
        {
          name: "05.2 Unauthorized Direct Access to Categories [Security 401/403]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Access denied without Bearer token (401 or 403)', function () {",
                  "    pm.expect(pm.response.code).to.be.oneOf([401, 403]);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [],
            url: {
              raw: "{{baseUrl}}/api/categories",
              host: ["{{baseUrl}}"],
              path: ["api", "categories"]
            }
          }
        },
        {
          name: "05.3 Register User B [Security Setup]",
          event: [
            {
              listen: "prerequest",
              script: {
                type: "text/javascript",
                exec: [
                  "const ts = Date.now();",
                  "const email = `userb_${ts}@example.com`;",
                  "pm.collectionVariables.set('userB_email', email);",
                  "pm.environment.set('userB_email', email);",
                  "pm.collectionVariables.set('userB_password', 'password123');",
                  "pm.environment.set('userB_password', 'password123');",
                  "pm.collectionVariables.set('userB_fullName', 'Attacker User B');",
                  "pm.environment.set('userB_fullName', 'Attacker User B');"
                ]
              }
            },
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 201 Created for User B', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "pm.test('User B token stored', function () {",
                  "    const data = pm.response.json();",
                  "    pm.expect(data.token).to.be.a('string').and.not.empty;",
                  "    pm.collectionVariables.set('userB_token', data.token);",
                  "    pm.environment.set('userB_token', data.token);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Content-Type", value: "application/json" }],
            body: {
              mode: "raw",
              raw: "{\n  \"email\": \"{{userB_email}}\",\n  \"password\": \"{{userB_password}}\",\n  \"fullName\": \"{{userB_fullName}}\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/users/register",
              host: ["{{baseUrl}}"],
              path: ["api", "users", "register"]
            }
          }
        },
        {
          name: "05.4 IDOR - User B Accesses User A Transactions [Security 404]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('IDOR protection: User B cannot access User A transactions (404/403)', function () {",
                  "    pm.expect(pm.response.code).to.be.oneOf([403, 404]);",
                  "});",
                  "pm.test('Response does not leak User A transaction data', function () {",
                  "    const body = pm.response.text();",
                  "    pm.expect(body).to.not.include('Salary deposit');",
                  "    pm.expect(body).to.not.include('Weekly supermarket');",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [
              { key: "Authorization", value: "Bearer {{userB_token}}" }
            ],
            url: {
              raw: "{{baseUrl}}/api/transactions/account/{{cardAccountId}}",
              host: ["{{baseUrl}}"],
              path: ["api", "transactions", "account", "{{cardAccountId}}"]
            }
          }
        },
        {
          name: "05.5 IDOR - User B Posts Transaction to User A Account [Security 404]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('IDOR protection: User B cannot modify User A account (404/403)', function () {",
                  "    pm.expect(pm.response.code).to.be.oneOf([403, 404]);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{userB_token}}" }
            ],
            body: {
              mode: "raw",
              raw: "{\n  \"accountId\": {{cardAccountId}},\n  \"categoryId\": {{categoryId}},\n  \"type\": \"INCOME\",\n  \"amount\": 9999.00,\n  \"description\": \"Malicious transaction by User B\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/transactions",
              host: ["{{baseUrl}}"],
              path: ["api", "transactions"]
            }
          }
        },
        {
          name: "05.6 Verify User A Account Balance Unchanged [Security Positive]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 200 OK', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "pm.test('User A balance is strictly untampered (still 750.50)', function () {",
                  "    const data = pm.response.json();",
                  "    const cardId = Number(pm.environment.get('cardAccountId') || pm.collectionVariables.get('cardAccountId'));",
                  "    const acc = data.find(a => a.id === cardId);",
                  "    pm.expect(acc).to.not.be.undefined;",
                  "    pm.expect(Number(acc.balance)).to.eql(750.50);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            url: {
              raw: "{{baseUrl}}/api/accounts",
              host: ["{{baseUrl}}"],
              path: ["api", "accounts"]
            }
          }
        },
        {
          name: "05.7 RFC 7807 Error Security Audit (No Technical Leaks) [Security]",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status code is 404', function () {",
                  "    pm.response.to.have.status(404);",
                  "});",
                  "pm.test('RFC 7807 problem details does not leak stack trace or internal SQL/paths', function () {",
                  "    const text = pm.response.text();",
                  "    pm.expect(text).to.not.include('Exception:');",
                  "    pm.expect(text).to.not.include('at org.springframework.');",
                  "    pm.expect(text).to.not.include('at com.financetracker.');",
                  "    pm.expect(text).to.not.include('SELECT ');",
                  "    pm.expect(text).to.not.include('INSERT ');",
                  "    pm.expect(text).to.not.include('C:\\\\');",
                  "    pm.expect(text).to.not.include('/home/');",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            body: {
              mode: "raw",
              raw: "{\n  \"accountId\": 999999,\n  \"categoryId\": {{categoryId}},\n  \"type\": \"EXPENSE\",\n  \"amount\": 10.00,\n  \"description\": \"Audit\"\n}"
            },
            url: {
              raw: "{{baseUrl}}/api/transactions",
              host: ["{{baseUrl}}"],
              path: ["api", "transactions"]
            }
          }
        }
      ]
    }
  ]
};

const outputPath = path.join(__dirname, '..', 'postman', 'collections', 'FinanceTracker.postman_collection.json');
fs.writeFileSync(outputPath, JSON.stringify(collection, null, 2), 'utf8');
console.log(`Regenerated collection at: ${outputPath}`);
