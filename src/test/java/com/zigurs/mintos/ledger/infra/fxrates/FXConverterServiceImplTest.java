package com.zigurs.mintos.ledger.infra.fxrates;

import com.zigurs.mintos.ledger.api.FXConversionResult;
import com.zigurs.mintos.ledger.api.FXConverterService;
import com.zigurs.mintos.ledger.api.TransferService;
import com.zigurs.mintos.ledger.infra.fxrates.model.CurrencyBeaconResponse;

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

class FXConverterServiceImplTest {

    private RestTemplate restTemplate;
    private FXConverterService fxConverterService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        fxConverterService = new FXConverterServiceImpl(restTemplate, "http://base_url", "api_key");
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

        FXConversionResult result = fxConverterService.convert("FROM", "TO", BigInteger.valueOf(1L));

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
                FXConverterService.FXConversionException.class,
                () -> {

                    fxConverterService.convert("FROM", "TO", BigInteger.valueOf(1L));
                },
                "FXConversionException expected, but not thrown"
        );

        verify(restTemplate, times(1)).getForEntity(anyString(), any(CurrencyBeaconResponse.class.getClass()));
    }


    @Test
    void badResponse() throws TransferService.TransferException {
        ResponseEntity<CurrencyBeaconResponse> entity = ResponseEntity.ok(null);

        when(restTemplate.getForEntity(anyString(), any(CurrencyBeaconResponse.class.getClass()))).thenReturn(entity);

        assertThrows(
                FXConverterService.FXConversionException.class,
                () -> {

                    fxConverterService.convert("FROM", "TO", BigInteger.valueOf(1L));
                },
                "FXConversionException expected, but not thrown"
        );

        verify(restTemplate, times(1)).getForEntity(anyString(), any(CurrencyBeaconResponse.class.getClass()));
    }

    @Test
    void networkError() throws TransferService.TransferException {
        when(restTemplate.getForEntity(anyString(), any(CurrencyBeaconResponse.class.getClass()))).thenThrow(
                new RestClientException("you shall not pass")
        );

        assertThrows(
                FXConverterService.FXConversionException.class,
                () -> {

                    fxConverterService.convert("FROM", "TO", BigInteger.valueOf(1L));
                },
                "FXConversionException expected, but not thrown"
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

                    fxConverterService.convert("FROM", "TO", BigInteger.valueOf(1L));
                },
                "runtime exception expected, but not thrown"
        );

        verify(restTemplate, times(1)).getForEntity(anyString(), any(CurrencyBeaconResponse.class.getClass()));
    }

    @Test
    void nullGuards() throws TransferService.TransferException {
        assertThrows(
                NullPointerException.class,
                () -> {
                    fxConverterService.convert(null, "TO", BigInteger.valueOf(1L));
                },
                "runtime exception expected, but not thrown"
        );
        assertThrows(
                NullPointerException.class,
                () -> {
                    fxConverterService.convert("FROM", null, BigInteger.valueOf(1L));
                },
                "runtime exception expected, but not thrown"
        );
        assertThrows(
                NullPointerException.class,
                () -> {
                    fxConverterService.convert("NULL", "TO", null);
                },
                "runtime exception expected, but not thrown"
        );
    }
}