package com.codewise.controller;

import com.codewise.domain.User;
import com.codewise.dto.SignupRequestDto;
import com.codewise.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user") // "/user" 경로로 들어오는 모든 요청을 처리
public class UserController { // 회원 정보 조회, 수정, 삭제 요청을 처리하는 컨트롤러

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me") // "/user/me" 경로의 GET 요청을 처리 (현재 로그인한 사용자 정보 조회)
    public ResponseEntity<User> getMyInfo(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(userService.getUserInfo(username));
    }

    @DeleteMapping("/me") // "/user/me" 경로의 DELETE 요청을 처리 (현재 로그인한 사용자 계정 삭제)
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(userDetails.getUsername());
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }

    @PutMapping("/me") // "/user/me" 경로의 PUT 요청을 처리 (현재 로그인한 사용자 정보 수정)
    public ResponseEntity<String> updateUser(@RequestBody SignupRequestDto dto, @AuthenticationPrincipal UserDetails userDetails) {
        userService.updateUser(dto, userDetails.getUsername());
        return ResponseEntity.ok("회원 정보가 수정되었습니다.");
    }

    @GetMapping("/history") // "/user/history" 경로의 GET 요청을 처리 (로그인한 사용자의 분석 이력 정렬 조회)
    public ResponseEntity<?> getSortedHistory(@RequestParam(defaultValue = "id") String sortBy,
                                              @RequestParam(defaultValue = "desc") String direction,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getSortedUserHistory(userDetails.getUsername(), sortBy, direction));
    }

}
