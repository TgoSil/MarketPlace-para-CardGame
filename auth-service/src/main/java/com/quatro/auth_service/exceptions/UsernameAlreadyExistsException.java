package com.quatro.auth_service.exceptions;

public class UsernameAlreadyExistsException extends RuntimeException{
    
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }

}
