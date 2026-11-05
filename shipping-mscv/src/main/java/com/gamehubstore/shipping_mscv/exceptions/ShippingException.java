package com.gamehubstore.shipping_mscv.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY) // Devuelve un código HTTP 422
public class ShippingException extends RuntimeException {

    public ShippingException(String message) {
        super(message);
    }
}