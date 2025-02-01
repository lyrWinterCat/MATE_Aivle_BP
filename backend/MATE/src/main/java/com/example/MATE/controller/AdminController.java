package com.example.MATE.controller;

import com.example.MATE.dto.*;
import com.example.MATE.model.AdminFeedback;
import com.example.MATE.model.AdminFeedbackComments;
import com.example.MATE.model.SpeechLog;
import com.example.MATE.model.User;
import com.example.MATE.repository.UserRepository;
import com.example.MATE.service.AdminService;
import com.example.MATE.service.UserService;
import com.example.MATE.utils.PaginationUtils;
import com.example.MATE.utils.SecurityUtils;
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
    @PreAuthorize("hasAuthority('ADMIN')")
    public String detail(@RequestParam("feedbackId") Integer feedbackId, Model model) {
        AdminFeedback feedback = adminService.getFeedbackById(feedbackId);
        model.addAttribute("title", feedback.getTitle());
        model.addAttribute("userName", feedback.getUser().getName());
        model.addAttribute("toxicityId", feedback.getToxicityLog().getToxicityId());
        String filepath = (feedback.getFilepath() != null) ? feedback.getFilepath() : "";
        model.addAttribute("filepath", filepath);
        model.addAttribute("content", feedback.getContent());
        model.addAttribute("status",feedback.getStatus());
        String response = adminService.getCommentByFeedbackId(feedbackId)
                .map(AdminFeedbackComments::getContent)
                .orElse("");
        model.addAttribute("response", response);
        return "admin/detail";
    }
    //정정게시글 상태 수정 + 댓글 저장
    @PostMapping("/adminFix/detail")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String comment(
            @RequestParam("feedbackId") Integer feedbackId,
            @RequestParam("response") String response,
            @RequestParam("status") String status){
        System.out.println(">>> 답변하기");
        //게시글조회
        AdminFeedback feedback = adminService.getFeedbackById(feedbackId);
        if (feedback == null) {
            throw new IllegalArgumentException("해당 피드백을 찾을 수 없습니다. feedbackId: " + feedbackId);
        }
        //상태수정
        System.out.println(">>>"+status);
        try {
            feedback.setStatus(AdminFeedback.FeedbackStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 상태값입니다: " + status);
        }
        userService.updateFeedback(feedback);
        //댓글객체셋팅
        AdminFeedbackCommentsDto dto = new AdminFeedbackCommentsDto();
        dto.setFeedbackId(feedbackId);
        dto.setContent(response);
        String email = SecurityUtils.getCurrentUserEmail();
        User user = userService.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException("로그인해주세요."));
        dto.setUserId(user.getUserId());
        adminService.saveComments(dto);

        return "redirect:/admin/adminFix/detail?feedbackId="+feedbackId;
    }

    @GetMapping("/adminFix")
    public String adminFix(Model model, @RequestParam(defaultValue = "0") int page) {
        // pagination
        int pageSize = 10; // 한 페이지에 보여줄 row 수

        Page<AdminFeedbackDto> feedbackList = adminService.getFeedbackListWithPaging(PageRequest.of(page, pageSize));
        model.addAttribute("feedbackList", feedbackList);

        // 이전페이지, 다음페이지, 페이지 번호 버튼 생성
        PaginationUtils.addPaginationAttributes(model, feedbackList, page);

        return "admin/adminFix";
    }

    @GetMapping("/adminLog")
    public String adminLog(Model model, @RequestParam(defaultValue = "0") int page) {
        // pagination
        int pageSize = 10; // 한 페이지에 보여줄 row 수

        Page<SpeechLogDto> pagedSpeechLogs = userService.getPagedSpeechLogs(PageRequest.of(page, pageSize));
        model.addAttribute("pagedSpeechLogs", pagedSpeechLogs);
        
        // 이전페이지, 다음페이지, 페이지 번호 버튼 생성
        PaginationUtils.addPaginationAttributes(model, pagedSpeechLogs, page);

        return "admin/adminLog";
    }
}