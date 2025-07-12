package com.codewise.exception;


//  사용자 정의 예외를 생성하여 일관된 방식으로 에러를 처리하기 위한 커스텀 예외 클래스
public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}
