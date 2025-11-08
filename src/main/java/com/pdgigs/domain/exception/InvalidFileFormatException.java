package com.pdgigs.domain.exception;

public class InvalidFileFormatException extends RuntimeException{
    public InvalidFileFormatException(String message){
        super (message);
    }
}
