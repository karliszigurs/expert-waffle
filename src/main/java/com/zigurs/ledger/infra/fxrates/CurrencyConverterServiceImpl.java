package com.zigurs.ledger.infra.fxrates;

import com.zigurs.ledger.api.CurrencyConverterService;
import com.zigurs.ledger.infra.fxrates.model.CurrencyBeaconResponse;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;

@Service
public class CurrencyConverterServiceImpl implements CurrencyConverterService {

    private final String currencyBeaconApiKey;
    private final String currencyBeaconBaseURL;

    private final RestTemplate restTemplate;

    public CurrencyConverterServiceImpl(
            RestTemplate restTemplate,
            @Value("${currency_beacon_api_base_url}") String baseURL,
            @Value("${currency_beakon_api_key}") String apiKey
    ) {
        this.restTemplate = restTemplate;
        currencyBeaconApiKey = apiKey;
        currencyBeaconBaseURL = baseURL;
    }

    @Override
    public CurrencyConversionResult convert(
            @NonNull String fromCurrency,
            @NonNull String toCurrency,
            @NonNull BigInteger amount) throws CurrencyConversionException {
        try {
            String currencyBeaconRequest = String.format(
                    "%s?api_key=%s&from=%s&to=%s&amount=%s",
                    currencyBeaconBaseURL,
                    currencyBeaconApiKey,
                    fromCurrency,
                    toCurrency,
                    amount);

            ResponseEntity<CurrencyBeaconResponse> responseEntity = restTemplate.getForEntity(currencyBeaconRequest, CurrencyBeaconResponse.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                CurrencyBeaconResponse response = responseEntity.getBody();

                if (response == null) {
                    throw new CurrencyConversionException( // conversion failed on server side
                            String.format("unable to convert %s into %s, internal error", fromCurrency, toCurrency)
                    );
                }

                return new CurrencyConversionResult(
                        response.from(),
                        response.to(),
                        response.value().toBigInteger()
                );
            } else {
                throw new CurrencyConversionException( // conversion failed on server side
                        String.format("unable to convert %s into %s, provider error", fromCurrency, toCurrency)
                );
            }
        } catch (RestClientException e) {
            throw new CurrencyConversionException( // conversion failed due to network issue
                    String.format("unable to convert %s into %s, provider unavailable", fromCurrency, toCurrency)
            );
        }
    }
}
