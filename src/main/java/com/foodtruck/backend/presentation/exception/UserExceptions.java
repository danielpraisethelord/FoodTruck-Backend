package com.foodtruck.backend.presentation.exception;

public class UserExceptions {

    public static class InvalidCurrentPasswordException extends RuntimeException {
        public InvalidCurrentPasswordException(String message) {
            super(message);
        }
    }

    public static class PasswordMismatchException extends RuntimeException {
        public PasswordMismatchException(String message) {
            super(message);
        }
    }
}
