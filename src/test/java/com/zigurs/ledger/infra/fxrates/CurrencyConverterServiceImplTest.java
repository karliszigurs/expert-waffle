package com.zigurs.ledger.infra.fxrates;

import com.zigurs.ledger.api.CurrencyConverterService;
import com.zigurs.ledger.api.TransferService;
import com.zigurs.ledger.infra.fxrates.model.CurrencyBeaconResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CurrencyConverterServiceImplTest {

    private RestTemplate restTemplate;
    private CurrencyConverterService currencyConverterService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        currencyConverterService = new CurrencyConverterServiceImpl(restTemplate, "http://base_url", "api_key");
    }


    @Test
    void greenPath() throws TransferService.TransferException {
        CurrencyBeaconResponse response = new CurrencyBeaconResponse(
                "FROM",
                "TO",
                BigDecimal.valueOf(123L),
                BigDecimal.valueOf(456L)
        );
        ResponseEntity<CurrencyBeaconResponse> entity = ResponseEntity.of(Optional.of(response));

        when(restTemplate.getForEntity(anyString(), any(CurrencyBeaconResponse.class.getClass()))).thenReturn(entity);

        CurrencyConverterService.CurrencyConversionResult result = currencyConverterService.convert("FROM", "TO", BigInteger.valueOf(1L));

        assertEquals("FROM", result.from());
        assertEquals("TO", result.to());
        assertEquals(456L, result.value().longValue());

        verify(restTemplate, times(1)).getForEntity(anyString(), any(CurrencyBeaconResponse.class.getClass()));
    }

    @Test
    void serverError() throws TransferService.TransferException {
        ResponseEntity<CurrencyBeaconResponse> entity = ResponseEntity.internalServerError().build();

        when(restTemplate.getForEntity(anyString(), any(CurrencyBeaconResponse.class.getClass()))).thenReturn(entity);

        assertThrows(
                CurrencyConverterService.CurrencyConversionException.class,
                () -> {

                    currencyConverterService.convert("FROM", "TO", BigInteger.valueOf(1L));
                }
        );

        verify(restTemplate, times(1)).getForEntity(anyString(), any(CurrencyBeaconResponse.class.getClass()));
    }


    @Test
    void badResponse() throws TransferService.TransferException {
        ResponseEntity<CurrencyBeaconResponse> entity = ResponseEntity.ok(null);

        when(restTemplate.getForEntity(anyString(), any(CurrencyBeaconResponse.class.getClass()))).thenReturn(entity);

        assertThrows(
                CurrencyConverterService.CurrencyConversionException.class,
                () -> {

                    currencyConverterService.convert("FROM", "TO", BigInteger.valueOf(1L));
                }
        );

        verify(restTemplate, times(1)).getForEntity(anyString(), any(CurrencyBeaconResponse.class.getClass()));
    }

    @Test
    void networkError() throws TransferService.TransferException {
        when(restTemplate.getForEntity(anyString(), any(CurrencyBeaconResponse.class.getClass()))).thenThrow(
                new RestClientException("you shall not pass")
        );

        assertThrows(
                CurrencyConverterService.CurrencyConversionException.class,
                () -> {

                    currencyConverterService.convert("FROM", "TO", BigInteger.valueOf(1L));
                }
        );

        verify(restTemplate, times(1)).getForEntity(anyString(), any(CurrencyBeaconResponse.class.getClass()));
    }


    @Test
    void runtimeException() throws TransferService.TransferException {
        when(restTemplate.getForEntity(anyString(), any(CurrencyBeaconResponse.class.getClass()))).thenThrow(
                new NullPointerException()
        );

        assertThrows(
                NullPointerException.class,
                () -> {

                    currencyConverterService.convert("FROM", "TO", BigInteger.valueOf(1L));
                }
        );

        verify(restTemplate, times(1)).getForEntity(anyString(), any(CurrencyBeaconResponse.class.getClass()));
    }

    @Test
    void serviceNullGuards() throws TransferService.TransferException {
        assertThrows(
                NullPointerException.class,
                () -> {
                    currencyConverterService.convert(null, "TO", BigInteger.valueOf(1L));
                }
        );

        assertThrows(
                NullPointerException.class,
                () -> {
                    currencyConverterService.convert("FROM", null, BigInteger.valueOf(1L));
                }
        );
        assertThrows(
                NullPointerException.class,
                () -> {
                    currencyConverterService.convert("NULL", "TO", null);
                }
        );
    }

    @Test
    void resultNullGuards() throws TransferService.TransferException {
        assertThrows(
                NullPointerException.class,
                () -> {
                    new CurrencyConverterService.CurrencyConversionResult(
                            "from",
                            "to",
                            null
                    );
                }
        );

        assertThrows(
                NullPointerException.class,
                () -> {
                    new CurrencyConverterService.CurrencyConversionResult(
                            "from",
                            null,
                            BigInteger.valueOf(1L)
                    );
                }
        );

        assertThrows(
                NullPointerException.class,
                () -> {
                    new CurrencyConverterService.CurrencyConversionResult(
                            null,
                            "to",
                            BigInteger.valueOf(1L)
                    );
                }
        );
    }
}