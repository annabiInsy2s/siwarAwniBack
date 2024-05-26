package com.aweni.aweniBack.error;

public record FieldError(
        String entityName,
        String fieldName,
        String message,
        String code
) {
}
