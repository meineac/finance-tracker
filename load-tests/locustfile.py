"""
Locust Performance and Stress Test Suite for Finance Tracker API
Профиль нагрузки и проверка требований NFR-02 (время ответа < 500 мс при 100 VUs).
"""

import time
from locust import HttpUser, task, between, events

class FinanceTrackerUser(HttpUser):
    # Пауза между запросами пользователя от 0.5 до 1.5 секунды (в среднем 1.0 с)
    wait_time = between(0.5, 1.5)

    def on_start(self):
        """Регистрация и получение JWT-токена перед запуском сценариев"""
        self.email = f"load_user_{int(time.time() * 1000)}@example.com"
        self.password = "password123"
        
        # 1. Регистрация нового пользователя
        reg_payload = {
            "email": self.email,
            "password": self.password,
            "fullName": "Load Test User"
        }
        res = self.client.post("/api/users/register", json=reg_payload)
        if res.status_code == 201:
            self.token = res.json().get("token")
        else:
            # Если уже существует, выполняем вход
            login_res = self.client.post("/api/users/login", json={"email": self.email, "password": self.password})
            self.token = login_res.json().get("token")

        self.headers = {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }

        # 2. Создание тестового счета
        acc_res = self.client.post(
            "/api/accounts",
            json={"name": "Locust Test Card", "type": "CARD"},
            headers=self.headers
        )
        if acc_res.status_code == 201:
            self.account_id = acc_res.json().get("id")
        else:
            self.account_id = 1

        # 3. Создание тестовой категории
        cat_res = self.client.post(
            "/api/categories",
            json={"name": f"LocustCat_{int(time.time() * 1000)}"},
            headers=self.headers
        )
        if cat_res.status_code == 201:
            self.category_id = cat_res.json().get("id")
        else:
            self.category_id = 1

    @task(5)
    def get_accounts(self):
        """Просмотр счетов (50% нагрузки чтения)"""
        with self.client.get("/api/accounts", headers=self.headers, catch_response=True) as response:
            if response.status_code == 200:
                if response.elapsed.total_seconds() > 0.5:
                    response.failure(f"SLA breached: {response.elapsed.total_seconds() * 1000} ms > 500 ms")
                else:
                    response.success()
            else:
                response.failure(f"Unexpected status: {response.status_code}")

    @task(2)
    def get_categories(self):
        """Просмотр категорий (20% нагрузки чтения)"""
        self.client.get("/api/categories", headers=self.headers)

    @task(2)
    def create_transaction(self):
        """Создание доходной транзакции (20% нагрузки записи с атомарным обновлением)"""
        tx_payload = {
            "accountId": self.account_id,
            "categoryId": self.category_id,
            "type": "INCOME",
            "amount": 100.50,
            "description": "Locust regular income"
        }
        self.client.post("/api/transactions", json=tx_payload, headers=self.headers)

    @task(1)
    def get_transactions(self):
        """Просмотр выписки транзакций по счету (10% нагрузки чтения)"""
        self.client.get(f"/api/transactions/account/{self.account_id}", headers=self.headers)
