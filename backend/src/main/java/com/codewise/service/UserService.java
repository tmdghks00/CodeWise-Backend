package com.codewise.service;

import com.codewise.domain.User;
import com.codewise.dto.AnalysisResultDto;
import com.codewise.dto.AnalysisResultFilterRequestDto;
import com.codewise.dto.SignupRequestDto;
import com.codewise.exception.CustomException;
import com.codewise.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class UserService {
// 사용자 정보 조회, 수정, 삭제 및 분석 이력 정렬 조회를 처리하는 서비스 클래스

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AnalysisResultService analysisResultService;

    public UserService(UserRepository userRepository, // 생성자 주입을 통해 의존성 초기화
                       PasswordEncoder passwordEncoder,
                       AnalysisResultService analysisResultService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.analysisResultService = analysisResultService;
    }

    public User getUserInfo(String email) { // 이메일로 사용자 정보를 조회하는 메서드
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
    }

    public void deleteUser(String email) { // 이메일로 사용자 계정을 삭제하는 메서드
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        userRepository.delete(user);
    }

    public void updateUser(SignupRequestDto dto, String email) { // 이메일과 DTO 로 사용자 정보를 수정하는 메서드
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다."));

        if (dto.getPassword() != null) { // DTO 에 비밀번호가 포함되어 있으면
            user.setPassword(passwordEncoder.encode(dto.getPassword())); // 새 비밀번호를 암호화하여 설정
        }
        if (dto.getEmail() != null) { // DTO 에 이메일이 포함되어 있으면
            user.setEmail(dto.getEmail()); // 새 이메일 설정
        }
        userRepository.save(user);  // 변경된 사용자 정보 저장
    }

    // 사용자의 분석 이력을 정렬하여 조회하는 메서드
    public List<AnalysisResultDto> getSortedUserHistory(String email, String sortBy, String direction) {
        // AnalysisResultFilterRequestDto 를 생성하여 새로운 통합 메서드에 전달
        AnalysisResultFilterRequestDto filterDto = new AnalysisResultFilterRequestDto();
        filterDto.setSortBy(sortBy);
        filterDto.setOrder(direction);
        // language, keyword 필터링도 필요하면 여기에 추가
        return analysisResultService.getFilteredAndSortedUserHistory(email, filterDto);
    }


}
