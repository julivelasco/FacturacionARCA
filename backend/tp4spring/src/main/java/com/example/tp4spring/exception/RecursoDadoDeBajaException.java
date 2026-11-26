package com.example.tp4spring.exception;

import lombok.Getter;

@Getter
public class RecursoDadoDeBajaException extends RuntimeException {
    private final Long id;

    public RecursoDadoDeBajaException(String message, Long id) {
        super(message);
        this.id = id;
    }
}
