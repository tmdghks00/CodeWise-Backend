package com.codewise.dto;

import com.codewise.domain.CodeSubmission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodeSubmissionDto { // 클라이언트에 전달할 코드 제출 정보를 담는 DTO 클래스
    private Long id;         // 제출된 코드의 고유 식별자
    private String code;     // 제출된 코드 내용
    private String language; // 제출된 코드의 프로그래밍 언어
    private LocalDateTime submittedAt;  // 코드가 제출된 시간

    public static CodeSubmissionDto fromEntity(CodeSubmission sub) {
        return new CodeSubmissionDto(
                sub.getId(),
                sub.getCode(),
                sub.getLanguage(),
                sub.getSubmittedAt()
        );
    }
}
