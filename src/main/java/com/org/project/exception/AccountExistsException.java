package com.org.project.exception;

public class AccountExistsException extends RuntimeException {
    public AccountExistsException() {
        super("Account already exists with the given email or provider");
    }
}
