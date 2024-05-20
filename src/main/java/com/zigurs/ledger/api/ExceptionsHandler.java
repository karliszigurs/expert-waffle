package com.zigurs.ledger.api;

import com.zigurs.ledger.api.responses.ErrorResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;

@ControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler(value = TransferService.TransferException.class)
    public ResponseEntity<ErrorResponse> handleTransferException(TransferService.TransferException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = CurrencyConverterService.CurrencyConversionException.class)
    public ResponseEntity<ErrorResponse> handleFxException(CurrencyConverterService.CurrencyConversionException ex) {
        return new ResponseEntity<>(new ErrorResponse("currency conversion unavailable"), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(value = HttpStatusCodeException.class)
    public ResponseEntity<ErrorResponse> handleStatusCodeExceptions(HttpStatusCodeException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), ex.getStatusCode());
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> fallbackHandler(Throwable ex) {
        return new ResponseEntity<>(new ErrorResponse("internal error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
