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

    private final CodeSubmissionRepository codeSubmissionRepository;
    private final UserRepository userRepository;

    public CodeSubmissionService(CodeSubmissionRepository codeSubmissionRepository,
                                 UserRepository userRepository) {
        this.codeSubmissionRepository = codeSubmissionRepository;
        this.userRepository = userRepository;
    }

    // userId 기반 코드 제출
    public void submitCode(Long userId, CodeSubmissionDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        CodeSubmission submission = CodeSubmission.builder()
                .user(user)
                .code(dto.getCode())
                .language(dto.getLanguage())
                .submittedAt(LocalDateTime.now())
                .build();

        codeSubmissionRepository.save(submission);
    }

    public CodeSubmissionDto getCodeById(Long id) {
        CodeSubmission sub = codeSubmissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("코드 없음"));
        return CodeSubmissionDto.fromEntity(sub);
    }

    public void deleteCode(Long id) {
        codeSubmissionRepository.deleteById(id);
    }

    // userId 기반 제출 목록 조회
    public List<CodeSubmissionDto> getSubmissionsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다."));
        List<CodeSubmission> list = codeSubmissionRepository.findAllByUser(user);
        return list.stream().map(CodeSubmissionDto::fromEntity).collect(Collectors.toList());
    }
}
