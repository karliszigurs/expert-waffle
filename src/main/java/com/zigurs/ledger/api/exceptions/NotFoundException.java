package com.zigurs.ledger.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class NotFoundException extends HttpStatusCodeException {

    public NotFoundException(String description) {
        super(HttpStatus.NOT_FOUND, description);
    }
}
