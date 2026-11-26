package com.example.tp4spring.exception;

public class ArcaException extends RuntimeException {
  public ArcaException(String message) {
    super(message);
  }
  public ArcaException(String message, Throwable cause) {
    super(message, cause);
  }
}