package com.example.MATE.controller;

import com.example.MATE.dto.AdminFeedbackDto;
import com.example.MATE.dto.PageItemDto;
import com.example.MATE.dto.RequestDto;
import com.example.MATE.dto.SpeechLogDto;
import com.example.MATE.model.AdminFeedback;
import com.example.MATE.model.SpeechLog;
import com.example.MATE.repository.UserRepository;
import com.example.MATE.service.AdminService;
import com.example.MATE.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.events.CollectionEndEvent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// 관리자 관련 엔드포인트

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @GetMapping("/adminMain")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adminMain() {
        return "admin/adminMain";
    }


    @GetMapping("/adminFix/detail")
    public String detail(@RequestParam("feedbackId") Integer feedbackId, Model model) {
        AdminFeedback feedback = adminService.getFeedbackById(feedbackId);
        model.addAttribute("title", feedback.getTitle());
        model.addAttribute("userName", feedback.getUser().getName());
        model.addAttribute("toxicityId", feedback.getToxicityLog().getToxicityId());
        model.addAttribute("content", feedback.getContent());
        return "admin/detail";
    }

    @GetMapping("/adminFix")
    public String adminFix(Model model, @RequestParam(defaultValue = "0") int page) {
        // pagination
        int pageSize = 10; // 한 페이지에 보여줄 row 수


//        List<AdminFeedbackDto> feedbackList = adminService.getFeedbackList();
        Page<AdminFeedbackDto> feedbackList = adminService.getFeedbackListWithPaging(PageRequest.of(page, pageSize));
        model.addAttribute("feedbackList", feedbackList);

        // 이전 페이지 존재 확인
        if (feedbackList.hasPrevious()) {
            model.addAttribute("previousPage", page - 1);
        };

        // 다음 페이지 존재 확인
        if (feedbackList.hasNext()) {
            model.addAttribute("nextPage", page + 1);
        };

        // 페이지 번호 리스트 생성
        int totalPages = feedbackList.getTotalPages();
        List<PageItemDto> pageNumbers = IntStream.range(0, totalPages)
                .mapToObj(idx -> new PageItemDto(idx, idx + 1))
                .collect(Collectors.toList());
        model.addAttribute("pageNumbers", pageNumbers);


        return "admin/adminFix";
    }

    @GetMapping("/adminLog")
    public String adminLog(Model model, @RequestParam(defaultValue = "0") int page) {
        // pagination
        int pageSize = 10; // 한 페이지에 보여줄 row 수

        Page<SpeechLogDto> pagedSpeechLogs = userService.getPagedSpeechLogs(PageRequest.of(page, pageSize));
        model.addAttribute("pagedSpeechLogs", pagedSpeechLogs);
        model.addAttribute("currentPage", page);

        // 이전 페이지 존재 확인
        if (pagedSpeechLogs.hasPrevious()) {
            model.addAttribute("previousPage", page - 1);
        };

        // 다음 페이지 존재 확인
        if (pagedSpeechLogs.hasNext()) {
            model.addAttribute("nextPage", page + 1);
        };

        // 페이지 번호 리스트 생성
        int totalPages = pagedSpeechLogs.getTotalPages();
        List<PageItemDto> pageNumbers = IntStream.range(0, totalPages)
                        .mapToObj(idx -> new PageItemDto(idx, idx + 1))
                        .collect(Collectors.toList());
        model.addAttribute("pageNumbers", pageNumbers);

        return "admin/adminLog";
    }
}