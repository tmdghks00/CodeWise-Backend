package com.codewise.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 이 클래스가 RESTful API를 제공하는 컨트롤러임을 명시
public class HomeController { // 기본 홈 API 응답을 처리하는 컨트롤러
    @GetMapping("/") // 루트 경로("/")로 들어오는 GET 요청을 처리
    public String home() { // "CodeWise 백엔드 서버 실행 중!" 메시지를 반환
        return "CodeWise 백엔드 서버 실행 중!";
    }
}
