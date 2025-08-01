package com.codewise.service;

import com.codewise.domain.CodeSubmission;
import com.codewise.domain.User;
import com.codewise.dto.CodeSubmissionDto;
import com.codewise.exception.CustomException;
import com.codewise.repository.CodeSubmissionRepository;
import com.codewise.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class CodeSubmissionService {
    // 사용자 제출 코드 저장, 조회, 삭제 등의 비즈니스 로직을 처리하는 서비스 클래스

    private final CodeSubmissionRepository codeSubmissionRepository;
    private final UserRepository userRepository;

    public CodeSubmissionService(CodeSubmissionRepository codeSubmissionRepository,
                                 UserRepository userRepository) {
        this.codeSubmissionRepository = codeSubmissionRepository;
        this.userRepository = userRepository;
    }

    // 이메일 기반으로 사용자 조회 및 코드 제출 저장
    public void submitCode(String email, CodeSubmissionDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        CodeSubmission submission = CodeSubmission.builder()
                .user(user)
                .code(dto.getCode())
                .language(dto.getLanguage())
                .submittedAt(LocalDateTime.now())
                .build();

        codeSubmissionRepository.save(submission);
    }

    // 이메일 기반으로 사용자 코드 목록 조회
    public List<CodeSubmissionDto> getUserCodeList(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        return codeSubmissionRepository.findAllByUser(user).stream()
                .map(sub -> new CodeSubmissionDto(sub.getId(), sub.getCode(), sub.getLanguage()))
                .collect(Collectors.toList());
    }

    // 코드 ID로 코드 조회
    public CodeSubmissionDto getCodeById(Long id) {
        CodeSubmission sub = codeSubmissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("코드 없음"));
        return new CodeSubmissionDto(sub.getId(), sub.getCode(), sub.getLanguage());
    }

    // 코드 ID로 코드 삭제
    public void deleteCode(Long id) {
        codeSubmissionRepository.deleteById(id);
    }

    // 이메일 기반으로 제출 코드 전체 조회
    public List<CodeSubmissionDto> getSubmissionsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다."));

        List<CodeSubmission> list = codeSubmissionRepository.findAllByUser(user);
        return list.stream().map(CodeSubmissionDto::fromEntity).collect(Collectors.toList());
    }
}
