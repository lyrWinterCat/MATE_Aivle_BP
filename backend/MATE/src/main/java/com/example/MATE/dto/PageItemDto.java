package com.example.MATE.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// 페이지네이션을 위한 DTO
@Data
@AllArgsConstructor
public class PageItemDto {
    private int pageIndex; // 실제 페이지 인덱스 (0부터 시작)
    private int displayIndex; // 화면에 보이는 인덱스 (1부터 시작)
}
