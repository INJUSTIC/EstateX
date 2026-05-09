package com.estatex.domain.exception;

public class AccessDeniedException extends DomainException {
    public AccessDeniedException() {
        super("Access denied: you do not have permission to perform this action");
    }
    public AccessDeniedException(String message) {
        super(message);
    }
}
