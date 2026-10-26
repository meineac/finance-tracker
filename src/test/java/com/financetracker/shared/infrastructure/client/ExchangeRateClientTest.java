package com.financetracker.shared.infrastructure.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("ExchangeRateClient HTTP Stub Integration Tests")
class ExchangeRateClientTest {

    private ExchangeRateClient client;
    private MockRestServiceServer mockServer;
    private static final String BASE_URL = "https://api.exchangerate.host";

    @BeforeEach
    void setUp() {
        client = new ExchangeRateClient(BASE_URL);
        mockServer = MockRestServiceServer.createServer(client.getRestTemplate());
    }

    @Test
    @DisplayName("Should successfully fetch exchange rate when external service returns 200 OK")
    void getExchangeRate_WhenExternalServiceReturnsOk_ShouldReturnRate() {
        // Arrange
        String from = "USD";
        String to = "RUB";
        String expectedUrl = BASE_URL + "/convert?from=USD&to=RUB";
        String responseJson = """
                {
                    "success": true,
                    "result": 92.50
                }
                """;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // Act
        BigDecimal rate = client.getExchangeRate(from, to);

        // Assert
        assertThat(rate).isEqualByComparingTo(new BigDecimal("92.50"));
        mockServer.verify();
    }

    @ParameterizedTest(name = "Pair {0} to {1} -> rate {2}")
    @CsvSource({
            "USD, EUR, 0.92",
            "EUR, USD, 1.08",
            "GBP, USD, 1.25",
            "USD, RUB, 95.00"
    })
    @DisplayName("Should correctly handle various currency pairs via HTTP stub")
    void getExchangeRate_ParameterizedCurrencyPairs_ShouldReturnExpectedRates(
            String from, String to, String expectedRate) {
        // Arrange
        String expectedUrl = String.format("%s/convert?from=%s&to=%s", BASE_URL, from, to);
        String responseJson = String.format("""
                {
                    "success": true,
                    "result": %s
                }
                """, expectedRate);

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // Act
        BigDecimal rate = client.getExchangeRate(from, to);

        // Assert
        assertThat(rate).isEqualByComparingTo(new BigDecimal(expectedRate));
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw HttpServerErrorException when external service returns HTTP 500")
    void getExchangeRate_WhenExternalServiceReturns500_ShouldThrowException() {
        // Arrange
        String from = "USD";
        String to = "EUR";
        String expectedUrl = BASE_URL + "/convert?from=USD&to=EUR";

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        // Act & Assert
        assertThatThrownBy(() -> client.getExchangeRate(from, to))
                .isInstanceOf(HttpServerErrorException.class);
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw RestClientException when external service returns HTTP 404")
    void getExchangeRate_WhenExternalServiceReturns404_ShouldThrowException() {
        // Arrange
        String from = "INVALID";
        String to = "EUR";
        String expectedUrl = BASE_URL + "/convert?from=INVALID&to=EUR";

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThatThrownBy(() -> client.getExchangeRate(from, to))
                .isInstanceOf(RestClientException.class);
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw IllegalStateException when external response has null result")
    void getExchangeRate_WhenResponseHasNullResult_ShouldThrowIllegalStateException() {
        // Arrange
        String from = "USD";
        String to = "RUB";
        String expectedUrl = BASE_URL + "/convert?from=USD&to=RUB";
        String responseJson = """
                {
                    "success": false,
                    "result": null
                }
                """;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // Act & Assert
        assertThatThrownBy(() -> client.getExchangeRate(from, to))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to obtain exchange rate");
        mockServer.verify();
    }
}
