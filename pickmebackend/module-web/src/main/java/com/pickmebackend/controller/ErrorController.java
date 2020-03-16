package com.pickmebackend.controller;

import com.pickmebackend.common.ErrorsFormatter;
import com.pickmebackend.error.ErrorMessage;
import com.pickmebackend.exception.CodeNotExist;
import com.pickmebackend.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class ErrorController {

    private final ErrorsFormatter errorsFormatter;

    @ExceptionHandler(CodeNotExist.class)
    public ResponseEntity<?> codeNotExist() {
        return new ResponseEntity<>(errorsFormatter.badRequest(ErrorMessage.USER_NOT_FOUND.getValue()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> userNotFound() {
        return new ResponseEntity<>(errorsFormatter.badRequest(ErrorMessage.USER_NOT_FOUND.getValue()), HttpStatus.BAD_REQUEST);
    }
}
