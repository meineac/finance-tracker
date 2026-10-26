package com.financetracker.shared.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class ExchangeRateClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    @org.springframework.beans.factory.annotation.Autowired
    public ExchangeRateClient(@Value("${application.exchange-rate.api-url:https://api.exchangerate.host}") String baseUrl) {
        this(new RestTemplate(), baseUrl);
    }

    public ExchangeRateClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        String url = String.format("%s/convert?from=%s&to=%s", baseUrl, fromCurrency, toCurrency);
        ExchangeRateResponse response = restTemplate.getForObject(url, ExchangeRateResponse.class);
        if (response == null || response.result() == null) {
            throw new IllegalStateException("Failed to obtain exchange rate from external service");
        }
        return response.result();
    }

    public record ExchangeRateResponse(Boolean success, BigDecimal result) {}
}
