package com.codewise.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


// 전역적으로 발생하는 예외를 잡아 사용자에게 일관된 응답 메시지를 제공하는 예외 핸들러 클래스
@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException 타입의 예외가 발생했을 때 이 메서드가 처리하도록 지정
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleCustomException(CustomException e) {
        return ResponseEntity.badRequest().body("Custom Error: " + e.getMessage());
    }

    // IllegalArgumentException 타입의 예외가 발생했을 때 이 메서드가 처리하도록 지정
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArg(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
    }

    // 모든 종류의 Exception (최상위 예외)이 발생했을 때 이 메서드가 처리하도록 지정
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneral(Exception e) {
        return ResponseEntity.internalServerError().body("서버 에러 발생: " + e.getMessage());
    }
}
