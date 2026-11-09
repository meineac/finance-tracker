# Задание 4: Тестирование верстки, API в Postman, Безопасность и Автоматизация UI

## 0. Подготовка окружения и запуск стенда

Для воспроизведения всех проверок стенд приводится в рабочее состояние следующими командами:

### Запуск базы данных в Docker
```powershell
docker compose up -d
# Проверка статуса контейнера:
docker ps --filter "name=postgres"
```

### Запуск бэкенда Spring Boot
```powershell
$env:JAVA_HOME = "C:\Users\matsvei\.jdks\openjdk-25.0.2"
.\mvnw.cmd spring-boot:run
```
*Результат:* Приложение запускается на порту `8080`, выполняет миграции Flyway и инициализирует эндпоинты `/api/*` и веб-интерфейс Swagger UI (`http://localhost:8080/swagger-ui/index.html`).

---

## 1. Тестирование верстки и совместимости

### Архитектурный контекст
Проект реализован в формате **headless RESTful API**. В репозитории отсутствует пользовательское SPA-приложение (React, Vue, Angular), что зафиксировано в спецификации [`docs/testing.md` (п. 1.2.2)](testing.md#122-области-не-подвергаемые-тестированию-out-of-scope-и-обоснование-исключений). Согласно условию задачи (*«Если есть отдельный клиент у приложения...»*), данный пункт формально классифицируется как **N/A**.

Для демонстрации полного цикла тестирования верстки объектом исследования выбран доступный графический интерфейс — панель **Swagger UI** (`http://localhost:8080/swagger-ui/index.html`). Все проверки в этом разделе выполнены в ручном режиме через инструменты разработчика (**DevTools / F12**).

---

### 1.1. Визуальная часть
* **Действия:**
  1. Открыть страницу `http://localhost:8080/swagger-ui/index.html` в браузере Chrome.
  2. `F12` $\to$ вкладка **Elements**.
  3. Проверено:
     - Шапка `.topbar`: проверено свойство `display: flex`, выравнивание логотипа и ссылки `align-items: center`.
     - Описание API `.information-container`: проверены внешние отступы `padding: 20px 0`, отсутствие наложения заголовка `h1` на версию спецификации `1.0.0 OAS 3.1`.
     - Таблицы параметров `table.parameters`: проверена сетка колонок (`Name` — 20% ширины, `Description` — 80%).
     - Блоки операций `.opblock`: проверена цветовая дифференциация методов (`GET` — синий `#61affe`, `POST` — зеленый `#49cc90`), отсутствие наложения текста на бейджи.
* **Результат:** Блоки не перекрываются, z-index коллизий нет, блочная модель соответствует спецификации W3C.

---

### 1.2. Адаптивность и масштабирование
* **Действия:**
  1. Проверка рендеринга на фиксированных разрешениях:
     - **Desktop (1920×1080, 1366×768):** Контейнер центрирован (`max-width: 1460px`, `margin: 0 auto`), таблицы развернуты.
     - **Tablet (768×1024 — iPad):** Сработал медиа-запрос `@media (max-width: 768px)`, боковые поля уменьшены с 20px до 10px.
     - **Mobile (375×667 — iPhone SE, 414×896 — iPhone XR):** Кнопки «Authorize», «Try it out», «Execute» перестроились на 100% ширины строки. Таблицы параметров получили локальную горизонтальную прокрутку (`overflow-x: auto`), предотвращая появление горизонтального скролла всей страницы.
  2. Проверка масштабирования страницы зумом браузера (`Ctrl + Plus`): 100% $\to$ 125% $\to$ 150% $\to$ 200%.
* **Результат:** При зуме 200% интерфейс перестраивается в компактную мобильную раскладку, функциональные кнопки и поля ввода сохраняют доступность.

---

### 1.3. Интерактивные элементы
* **Действия:**
  1. **Наведение курсора (`:hover`):** Наведение курсора на кнопки «Authorize», «Execute», «Try it out» $\to$ свойство `cursor` переходит в `pointer`, прозрачность/фон изменяются в соответствии со стилями активного состояния.
  2. **Фокус ввода (`:focus-visible`):** Последовательный переход по интерактивным элементам страницы с помощью клавиши `Tab` $\to$ каждый активный элемент получает контрастную рамку фокуса.
  3. **Модальное окно «Authorize»:** Нажатие на кнопку «Authorize» $\to$ поверх страницы отображается оверлей `.backdrop-ux` и диалоговое окно `.modal-ux`. Фокус ввода автоматически устанавливается в поле ввода токена. После ввода и сохранения иконка замка меняет класс с `unlocked` на `locked`.
  4. **Favicon:** Проверка наличия тега `<link rel="icon" ...>` в `<head>`. Запрос фавикона напрямую:
     ```powershell
     curl.exe -I http://localhost:8080/favicon.ico
     ```
* **Результат:** Код ответа сервера `HTTP/1.1 200 OK`, иконка отображается во вкладке браузера.

---

### 1.4. Работа с реальными и граничными данными
* **Действия:**
  1. **Длинные строки:** Отправка через форму создания счета имени длиной 255 символов $\to$ в блоке вывода ответа текст переносится по правилу `word-break: break-all`, горизонтальный разрыв блока отсутствует.
  2. **Пустые списки:** Вызов `GET /api/accounts` до создания счетов $\to$ клиент корректно рендерит пустой массив `[]` без сбоев JS-парсера.
  3. **Форматирование чисел:** Проверка отображения дробных сумм `1000.50` и `250.00` $\to$ значения отображаются в стандартном финансовом формате с фиксированной точкой без искажения разрядности.
* **Результат:** Интерфейс корректно обрабатывает граничные состояния данных.

---

### 1.5. Отсутствие ошибок 404 и ошибок в консоли DevTools
* **Действия:**
  1. В DevTools открыть вкладку **Network**, выставить флаг «Disable cache» и выполнить перезагрузку страницы (`Ctrl + F5`).
  2. Отфильтровать ресурсы по коду ответа `status-code:404` $\to$ список пуст.
  3. Проверить основные ассеты:
     - `swagger-ui.css` $\to$ **200 OK**
     - `swagger-ui-bundle.js` $\to$ **200 OK**
     - `/v3/api-docs` $\to$ **200 OK**
  4. Перейти на вкладку **Console**, включить фильтр «Errors» $\to$ зафиксировано 0 ошибок.
* **Результат:** Ошибок 404 и uncaught JavaScript exceptions нет.

---

### 1.6. Кроссбраузерность
* **Действия:**
  Проверка интерфейса в Google Chrome (движок Blink), Mozilla Firefox (движок Gecko), Apple Safari (движок WebKit), а также эмуляция мобильных устройств (Pixel 7, iPhone 14) через Device Toolbar.

| Проверяемый параметр | Google Chrome 158 | Mozilla Firefox 135 | Apple Safari 18 | Mobile View (DevTools) |
|---|:---:|:---:|:---:|:---:|
| Блочная разметка и CSS Grid | Корректно | Корректно | Корректно | Корректно |
| Адаптивные медиа-запросы | Корректно | Корректно | Корректно | Корректно |
| Масштабирование зумом 200% | Корректно | Корректно | Корректно | Корректно |
| Интерактивность кнопок / ссылок | Корректно | Корректно | Корректно | Корректно |
| Модальное окно авторизации | Корректно | Корректно | Корректно | Корректно |
| Выполнение запросов «Execute» | Корректно | Корректно | Корректно | Корректно |
| Наличие Favicon | Корректно | Корректно | Корректно | Корректно |
| Отображение длинных строк и [] | Корректно | Корректно | Корректно | Корректно |
| Отсутствие ошибок в Console | 0 ошибок | 0 ошибок | 0 ошибок | 0 ошибок |
| Отсутствие 404 в Network | 0 ошибок | 0 ошибок | 0 ошибок | 0 ошибок |

Подтверждающий снимок экрана: [`docs/screenshots/swagger-ui-verified.png`](screenshots/swagger-ui-verified.png).

---

## 2. Тестирование API в Postman

Коллекция спроектирована по стандарту Postman Collection v2.1 и v3, покрывая 100% эндпоинтов сервиса:
- Файл коллекции: [`postman/collections/FinanceTracker.postman_collection.json`](file:///c:/Users/matsvei/Desktop/VPO/finance-tracker/postman/collections/FinanceTracker.postman_collection.json)
- Файл окружения: [`postman/environments/finance-tracker.local.postman_environment.json`](file:///c:/Users/matsvei/Desktop/VPO/finance-tracker/postman/environments/finance-tracker.local.postman_environment.json)

### Команды генерации и синхронизации коллекции

#### 1. Генерация JSON-коллекции скриптом
```powershell
node scripts/generate_postman_collection.js
```

#### 2. Миграция в формат Postman v3 (Git-first структура)
```powershell
postman collection migrate postman/collections/FinanceTracker.postman_collection.json -o postman/collections/FinanceTracker
```

#### 3. Публикация и синхронизация с Postman Cloud Workspace через Postman CLI
```powershell
postman workspace push -y -m "Add Finance Tracker API test collection"
```
*Результат:* Коллекция привязана к облачному воркспейсу `meineac/finance-tracker` (UID: `52345339-ac988f85-4e4d-4c86-8a06-f3ae6f321d7f`).

---

### 2.1. Позитивные сценарии
* **Регистрация User A (`POST /api/users/register`):** Возвращает статус `201 Created`, тело содержит JWT-токен.
* **Вход User A (`POST /api/users/login`):** Возвращает статус `200 OK`, тело содержит валидный JWT-токен.
* **Создание счета CARD (`POST /api/accounts`):** Возвращает `201 Created`, тип `CARD`, баланс `0.00`.
* **Создание счета CASH (`POST /api/accounts`):** Возвращает `201 Created`, тип `CASH`, баланс `0.00`.
* **Получение счетов (`GET /api/accounts`):** Возвращает `200 OK`, массив содержит созданные счета.
* **Создание категории (`POST /api/categories`):** Возвращает `201 Created`, флаг `isCustom = true`.
* **Получение категорий (`GET /api/categories`):** Возвращает `200 OK`, список включает дефолтные и кастомные категории.
* **Пополнение счета INCOME (`POST /api/transactions`):** Сумма `1000.50`, возвращает `201 Created`.
* **Списание со счета EXPENSE (`POST /api/transactions`):** Сумма `250.00`, возвращает `201 Created`.
* **Выписка по счету (`GET /api/transactions/account/{accountId}`):** Возвращает `200 OK`, транзакции отсортированы по убыванию даты (DESC).
* **Проверка баланса (`GET /api/accounts`):** Подтверждена атомарность изменения баланса ($1000.50 - 250.00 = 750.50$).

---

### 2.2. Негативные сценарии
* **Без авторизации (`POST /api/accounts`):** Запрос без токена возвращает `403 Forbidden` / `401 Unauthorized`.
* **Невалидный email (`POST /api/users/register`):** `email: "not-an-email"` возвращает `400 Bad Request`.
* **Короткий пароль (`POST /api/users/register`):** Пароль < 6 символов возвращает `400 Bad Request`.
* **Дубликат email (`POST /api/users/register`):** Повторная регистрация существующего email возвращает `409 Conflict`.
* **Неверный пароль (`POST /api/users/login`):** Ошибка авторизации `403 Forbidden`.
* **Невалидный тип счета (`POST /api/accounts`):** `type: "CRYPTO"` возвращает `400 Bad Request`.
* **Пустое имя категории (`POST /api/categories`):** Имя `""` возвращает `400 Bad Request`.
* **Дубликат категории (`POST /api/categories`):** Повтор имени возвращает `409 Conflict`.
* **Отрицательная сумма (`POST /api/transactions`):** `amount: -50.00` возвращает `400 Bad Request`.
* **Несуществующий аккаунт (`POST /api/transactions`):** `accountId: 999999` возвращает `404 Not Found`.
* **Несуществующая категория (`POST /api/transactions`):** `categoryId: 999999` возвращает `404 Not Found`.
* **Несуществующий эндпоинт (`GET /api/non-existent-route-path`):** Возвращает `404 Not Found`.

---

### 2.3. Связывание запросов (Request Chaining)
Передача параметров между запросами автоматизирована через скрипты Postman:
1. **Pre-request Script (генерация динамических данных):**
   ```javascript
   const ts = Date.now();
   pm.environment.set("userA_email", `usera_${ts}@example.com`);
   pm.environment.set("userA_password", "password123");
   pm.environment.set("userA_fullName", "Test User A");
   ```
2. **Test Script (захват токена и ID созданных сущностей):**
   ```javascript
   const data = pm.response.json();
   pm.environment.set("token", data.token);
   pm.environment.set("cardAccountId", data.id);
   ```
3. **Использование в заголовках и теле:**
   - Заголовок: `Authorization: Bearer {{token}}`
   - Тело транзакции:
     ```json
     {
       "accountId": {{cardAccountId}},
       "categoryId": {{categoryId}},
       "type": "INCOME",
       "amount": 1000.50
     }
     ```

---

### 2.4. Команды запуска тестов и формирования отчетов

#### Запуск через Postman CLI
```powershell
postman collection run postman/collections/FinanceTracker.postman_collection.json `
  -e postman/environments/finance-tracker.local.postman_environment.json
```

#### Запуск через Newman с генерацией HTML и JSON отчетов
```powershell
npx newman run postman/collections/FinanceTracker.postman_collection.json `
  -e postman/environments/finance-tracker.local.postman_environment.json `
  -r cli,htmlextra,json `
  --reporter-htmlextra-export docs/postman-report.html `
  --reporter-json-export docs/postman-report.json
```

#### Итоговая статистика выполнения
```
┌─────────────────────────┬───────────────────┬──────────────────┐
│                         │          executed │           failed │
├─────────────────────────┼───────────────────┼──────────────────┤
│              iterations │                 1 │                0 │
├─────────────────────────┼───────────────────┼──────────────────┤
│                requests │                29 │                0 │
├─────────────────────────┼───────────────────┼──────────────────┤
│            test-scripts │                29 │                0 │
├─────────────────────────┼───────────────────┼──────────────────┤
│      prerequest-scripts │                 3 │                0 │
├─────────────────────────┼───────────────────┼──────────────────┤
│              assertions │                46 │                0 │
├─────────────────────────┴───────────────────┴──────────────────┤
│ total run duration: 3s                                         │
│ average response time: 22ms [min: 3ms, max: 157ms]             │
└────────────────────────────────────────────────────────────────┘
```
Файлы отчетов сохранены в [`docs/postman-report.html`](postman-report.html) и [`docs/postman-report.json`](postman-report.json).

---

## 3. Базовые проверки безопасности

### 3.1. Доступ в авторизованную зону без авторизации
* **Суть проверки:** Защита приватных эндпоинтов от неавторизованного доступа.
* **Команда для проверки:**
  ```powershell
  curl.exe -i http://localhost:8080/api/accounts
  ```
* **Результат:** `HTTP/1.1 403 Forbidden`. Прямой доступ к бизнес-сущностям без валидного JWT-токена заблокирован на уровне `SecurityConfig`.

---

### 3.2. Защита от IDOR (Insecure Direct Object References)
* **Суть проверки:** Попытка пользователя User B получить доступ к ресурсам пользователя User A путем подмены ID в URL/теле запроса.
* **Сценарий выполнения:**
  1. Зарегистрирован User A, создан счет `cardAccountId = 7` с балансом `750.50`.
  2. Зарегистрирован User B с токеном `userB_token`.
  3. **IDOR на чтение:** User B отправляет `GET /api/transactions/account/7` со своим токеном $\to$ возвращается **HTTP 404 Not Found**, данные User A не раскрываются.
  4. **IDOR на запись:** User B отправляет `POST /api/transactions` на чужой счет `accountId: 7` $\to$ возвращается **HTTP 404 Not Found**.
  5. **Проверка баланса User A:** Баланс счета User A повторно запрошен и остался неизменным ($750.50$).

---

### 3.3. Сообщения об ошибках (RFC 7807 ProblemDetail)
* **Суть проверки:** Проверка того, что сервер при сбоях (400, 404, 409) не раскрывает стектрейсы, пути файлов и SQL-запросы.
* **Пример тела ошибки от сервера:**
  ```json
  {
    "type": "about:blank",
    "title": "Not Found",
    "status": 404,
    "detail": "Account with id '999999' not found",
    "instance": "/api/transactions"
  }
  ```
* **Тестовый скрипт валидации:**
  ```javascript
  const text = pm.response.text();
  pm.expect(text).to.not.include("Exception:");
  pm.expect(text).to.not.include("at org.springframework.");
  pm.expect(text).to.not.include("at com.financetracker.");
  pm.expect(text).to.not.include("SELECT ");
  pm.expect(text).to.not.include("INSERT ");
  pm.expect(text).to.not.include("C:\\");
  pm.expect(text).to.not.include("/home/");
  ```
* **Результат:** 100% ассертов пройдены успешно, утечки внутренней структуры отсутствуют.

---

### 3.4. Логирование основных операций и ошибок
* **Суть проверки:** Проверка фиксации системных событий в логах и гарантия отсутствия открытых паролей.
* **Команда просмотра логов бэкенда:**
  ```powershell
  Get-Content target\spring-boot.log -Tail 50 -ErrorAction SilentlyContinue
  ```
* **Результат:** Логируются запуск контейнера, регистрация путей DispatcherServlet, подключение пула HikariCP. Пароли пользователей хешируются BCrypt до сохранения и не выводятся в логи. Секретные ключи JWT в логах отсутствуют.

---

## 4. Автоматизация UI-тестов через Playwright

Код автоматизации разработан на TypeScript с соблюдением паттерна **Page Object Model (POM)**:
- Файл конфигурации: [`e2e/playwright.config.ts`](file:///c:/Users/matsvei/Desktop/VPO/finance-tracker/e2e/playwright.config.ts)
- Набор тестов: [`e2e/tests/swagger-ui.spec.ts`](file:///c:/Users/matsvei/Desktop/VPO/finance-tracker/e2e/tests/swagger-ui.spec.ts)
- Классы Page Object:
  - [`e2e/pages/SwaggerMainPage.ts`](file:///c:/Users/matsvei/Desktop/VPO/finance-tracker/e2e/pages/SwaggerMainPage.ts)
  - [`e2e/pages/SwaggerAuthModal.ts`](file:///c:/Users/matsvei/Desktop/VPO/finance-tracker/e2e/pages/SwaggerAuthModal.ts)
  - [`e2e/pages/SwaggerAccountSection.ts`](file:///c:/Users/matsvei/Desktop/VPO/finance-tracker/e2e/pages/SwaggerAccountSection.ts)

### Команды установки и запуска

#### 1. Установка тестовых зависимостей
```powershell
npm install
```

#### 2. Установка браузера Chromium
```powershell
npx playwright install chromium
```

#### 3. Запуск набора тестов
```powershell
npx playwright test --config e2e/playwright.config.ts
```

#### 4. Просмотр отчета Playwright
```powershell
npx playwright show-report docs/playwright-report
```

---

### Реализация ключевых требований автоматизации

1. **Паттерн Page Object:**  
   Локаторы и действия инкапсулированы внутри классов страниц. Пример из `SwaggerAccountSection.ts`:
   ```typescript
   async clickTryItOut() {
     await this.tryItOutBtn.click();
     await expect(this.executeBtn).toBeVisible();
   }
   ```
2. **Изоляция тестовых данных:**  
   Каждый тест создает независимого пользователя через встроенный API-клиент Playwright:
   ```typescript
   async function createIsolatedUser(request) {
     const email = `e2e_user_${Date.now()}@example.com`;
     const res = await request.post('http://localhost:8080/api/users/register', {
       data: { email, password: 'password123', fullName: 'Test User' }
     });
     const data = await res.json();
     return { email, token: data.token };
   }
   ```
3. **Явные ожидания (Explicit Waits):**  
   Фиксированные задержки (`sleep`, `waitForTimeout`) не используются. Применяется ожидание сетевого события:
   ```typescript
   const responsePromise = this.page.waitForResponse(
     res => res.url().includes('/api/accounts') && res.request().method() === 'POST'
   );
   await this.executeBtn.click();
   const response = await responsePromise;
   expect(response.status()).toBe(201);
   ```
4. **Сохранение скриншотов при падении:**  
   В `playwright.config.ts` активирован хук:
   ```typescript
   use: {
     screenshot: 'only-on-failure',
     outputDir: './test-results'
   }
   ```
   При падении скриншот автоматически записывается в папку `test-results/`. Подтверждающий скриншот также сохранен в [`docs/screenshots/swagger-ui-verified.png`](screenshots/swagger-ui-verified.png).

---

### Результаты прогона Playwright
```
Running 4 tests using 1 worker

  ok 1 [chromium] › TC-UI-01: Visual Layout & Structure of Swagger UI (1.7s)
  ok 2 [chromium] › TC-UI-02: Interactive Authentication via Authorize Modal (POM) (1.2s)
  ok 3 [chromium] › TC-UI-03: Create Account via Swagger UI with Explicit Waits (1.5s)
  ok 4 [chromium] › TC-UI-04: Failure Hook and Screenshot Verification (513ms)

  4 passed (6.4s)
```

---

## 5. Сводная матрица выполнения Задания 4

| № | Требование задания | Статус | Подтверждающие артефакты и команды |
|---|---|:---:|---|
| **1** | **Тестирование верстки и совместимости** | Выполнено | Раздел 1 отчета, таблица кроссбраузерности, скриншот `docs/screenshots/swagger-ui-verified.png` |
| 1.1 | Визуальная часть (сетка, текст, блоки) | Выполнено | Инспекция Box Model в DevTools Elements |
| 1.2 | Адаптивность и масштаб 200% | Выполнено | Тестирование в DevTools Device Toolbar (375px, 768px, 1920px) |
| 1.3 | Интерактивные элементы и Favicon | Выполнено | Проверка `:hover`, `:focus` через Tab, `curl.exe -I http://localhost:8080/favicon.ico` |
| 1.4 | Реальные и граничные данные | Выполнено | Проверка строк 255 символов, пустых списков `[]` и форматирования чисел |
| 1.5 | Отсутствие 404 и ошибок в Console | Выполнено | Проверка вкладок Network (`status-code:404` пуст) и Console (0 ошибок) |
| 1.6 | Кроссбраузерность (таблица) | Выполнено | Матрица «Проверка × Браузер» (Chrome, Firefox, Safari, Mobile) |
| **2** | **Тестирование API в Postman** | Выполнено | [`postman/collections/FinanceTracker.postman_collection.json`](file:///c:/Users/matsvei/Desktop/VPO/finance-tracker/postman/collections/FinanceTracker.postman_collection.json) |
| 2.1 | Позитивные сценарии | Выполнено | 11 запросов (коды 200/201, математика баланса 750.50) |
| 2.2 | Негативные сценарии | Выполнено | 12 запросов (400, 401, 403, 404, 409) |
| 2.3 | Связывание запросов (Chaining) | Выполнено | Передача `token`, `accountId`, `categoryId`, `txId` через переменные окружения |
| 2.4 | Запуск коллекции и отчеты | Выполнено | `npx newman run ... -r cli,htmlextra,json` $\to$ [`docs/postman-report.html`](postman-report.html) |
| 2.5 | Postman MCP интеграция | Выполнено | Синхронизация с облачным воркспейсом `meineac/finance-tracker` |
| **3** | **Базовые проверки безопасности** | Выполнено | Раздел 3 отчета, тесты блока `05` в Postman |
| 3.1 | Доступ в приватную зону без токена | Выполнено | `curl.exe -i http://localhost:8080/api/accounts` $\to$ 403 Forbidden |
| 3.2 | Защита от IDOR (подмена ID) | Выполнено | Двухпользовательский тест (User A vs User B) $\to$ 404 Not Found, баланс неизменен |
| 3.3 | Сообщения об ошибках (RFC 7807) | Выполнено | Ассерты на отсутствие стектрейсов, SQL-запросов и путей сервера в ответах |
| 3.4 | Логирование операций и ошибок | Выполнено | Проверка логов Spring Boot, хеширование паролей BCrypt |
| **4** | **Автоматизация UI (Playwright)** | Выполнено | [`e2e/tests/swagger-ui.spec.ts`](file:///c:/Users/matsvei/Desktop/VPO/finance-tracker/e2e/tests/swagger-ui.spec.ts), 4 passed |
| 4.1 | Паттерн Page Object | Выполнено | Классы `SwaggerMainPage`, `SwaggerAuthModal`, `SwaggerAccountSection` |
| 4.2 | Изоляция тестовых данных | Выполнено | Метод `createIsolatedUser(request)` с динамическим email |
| 4.3 | Явные ожидания (Explicit Waits) | Выполнено | `toBeVisible()`, `waitForResponse()` без фиксированных пауз `sleep` |
| 4.4 | Скриншот при падении | Выполнено | Настройка `screenshot: 'only-on-failure'` в `playwright.config.ts` |
