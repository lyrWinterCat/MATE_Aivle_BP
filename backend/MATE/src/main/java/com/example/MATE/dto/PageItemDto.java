package com.example.MATE.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// 페이지네이션을 위한 DTO
@Data
@AllArgsConstructor
public class PageItemDto {
    private int pageIndex; // 실제 페이지 인덱스 (0부터 시작)
    private String displayIndex; // 화면에 보이는 인덱스 (1부터 시작)

    // 페이지 무한히 늘어나는 것 방지용
    private boolean isEllipsis; // 해당 항목이 "..." 인 경우 true 로 설정하여 링크 대신 단순 텍스트로 표시
}
