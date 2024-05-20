package com.zigurs.ledger.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class BadRequestException extends HttpStatusCodeException {

    public BadRequestException(String description) {
        super(HttpStatus.BAD_REQUEST, description);
    }
}
