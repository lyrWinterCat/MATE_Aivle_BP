package com.example.MATE.utils;

import com.example.MATE.dto.PageItemDto;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
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
        List<PageItemDto> pageItems = new ArrayList<>();

        // 총 페이지 수가 7 이하인 경우 모든 페이지 번호를 표시
        if (totalPages <= 7) {
            for (int i = 0; i < totalPages; i++) {
                pageItems.add(new PageItemDto(i, String.valueOf(i + 1), false));
            }
        } else {
            // 현재 페이지가 초반에 위치하는 경우 (인덱스 0 ~ 3)
            if (currentPage <= 3) {
                for (int i = 0; i <= 3; i++) {
                    pageItems.add(new PageItemDto(i, String.valueOf(i + 1), false));
                }
                pageItems.add(new PageItemDto(-1, "...", true));
                pageItems.add(new PageItemDto(totalPages - 1, String.valueOf(totalPages), false));
            }
            // 현재 페이지가 후반에 위치하는 경우 (마지막 4페이지)
            else if (currentPage >= totalPages - 4) {
                pageItems.add(new PageItemDto(0, "1", false));
                pageItems.add(new PageItemDto(-1, "...", true));
                for (int i = totalPages - 4; i < totalPages; i++) {
                    pageItems.add(new PageItemDto(i, String.valueOf(i + 1), false));
                }
            }
            // 현재 페이지가 중간에 위치하는 경우
            else {
                pageItems.add(new PageItemDto(0, "1", false));
                pageItems.add(new PageItemDto(-1, "...", true));
                for (int i = currentPage - 1; i <= currentPage + 1; i++) {
                    pageItems.add(new PageItemDto(i, String.valueOf(i + 1), false));
                }
                pageItems.add(new PageItemDto(-1, "...", true));
                pageItems.add(new PageItemDto(totalPages - 1, String.valueOf(totalPages), false));
            }
        }

        model.addAttribute("pageNumbers", pageItems);
    }
}
