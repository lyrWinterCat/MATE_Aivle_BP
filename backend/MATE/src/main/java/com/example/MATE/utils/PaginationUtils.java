package com.example.MATE.utils;

import com.example.MATE.dto.PageItemDto;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PaginationUtils {

    // 페이지네이션 관련 속성을 Model 객체에 추가하는 메서드
    public static <T> void addPaginationAttributes(Model model, Page<T> pagedData, int currentPage) {
        model.addAttribute("currentPage", currentPage);

        // 이전 페이지 존재 여부 확인
        if (pagedData.hasPrevious()) {
            model.addAttribute("previousPage", currentPage - 1);
        }

        // 다음 페이지 존재 여부 확인
        if (pagedData.hasNext()) {
            model.addAttribute("nextPage", currentPage + 1);
        }

        // 페이지 번호 리스트 생성
        int totalPages = pagedData.getTotalPages();
        List<PageItemDto> pageNumbers = IntStream.range(0, totalPages)
                .mapToObj(idx -> new PageItemDto(idx, idx + 1))
                .collect(Collectors.toList());

        model.addAttribute("pageNumbers", pageNumbers);
    }
}
