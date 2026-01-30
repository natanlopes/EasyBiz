package br.com.easybiz.exception;

import java.time.LocalDateTime;

public record ApiError(
    LocalDateTime timestamp,
    int status,
    String error,
    String message
) {}
