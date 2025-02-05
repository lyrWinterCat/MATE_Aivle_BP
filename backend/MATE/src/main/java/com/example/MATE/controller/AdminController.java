package com.example.MATE.controller;

import com.example.MATE.dto.*;
import com.example.MATE.model.*;
import com.example.MATE.repository.MeetingRepository;
import com.example.MATE.repository.ToxicityLogRepository;
import com.example.MATE.repository.UserRepository;
import com.example.MATE.service.AdminService;
import com.example.MATE.service.UserService;
import com.example.MATE.utils.DateUtil;
import com.example.MATE.utils.PaginationUtils;
import com.example.MATE.utils.SecurityUtils;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    private final ToxicityLogRepository toxicityLogRepository;

    @GetMapping("/adminMain")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adminMain(Model model) {
        // 총 회의 수
        Long meetingCounts = adminService.getMeetingCount();

        // 평균 회의 시간
        String formattedDuration = adminService.getAverageMeetingDuration();

        // 총 독성 발언 횟수
        Long toxicityCounts = adminService.getToxicityLogCount();

        // 부서별 독성 발언 횟수
        String departmentToxicityLogs =  adminService.getToxicityLogsCountByDepartment();
        String dailyToxicityLogsJson = adminService.getDailyToxicityLogs();

        model.addAttribute("meetingCounts", meetingCounts);
        model.addAttribute("averageMeetingDuration", formattedDuration);
        model.addAttribute("toxicityCounts", toxicityCounts);
        model.addAttribute("departmentToxicityLogs", departmentToxicityLogs);
        model.addAttribute("dailyToxicityLogs", dailyToxicityLogsJson);
        return "admin/adminMain";
    }


    @GetMapping("/adminFix/detail")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String detail(@RequestParam("feedbackId") Integer feedbackId, Model model) {
        AdminFeedback feedback = adminService.getFeedbackById(feedbackId);
        ToxicityLog toxicityLog = feedback.getToxicityLog();
        SpeechLog speechLog = toxicityLog.getSpeechLog();

        model.addAttribute("title", feedback.getTitle());
        model.addAttribute("userName", feedback.getUser().getName());
        model.addAttribute("toxicityId", speechLog.getContent());
        String filepath = (feedback.getFilepath() != null) ? feedback.getFilepath() : "";
        model.addAttribute("filepath", filepath);
        model.addAttribute("content", feedback.getContent());
        model.addAttribute("status",feedback.getStatus());
        model.addAttribute("speechLogTimestamp",DateUtil.format(speechLog.getTimestamp()));

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
    public String adminFix(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(required = false) String employeeName,
                           @RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate,
                           @RequestParam(required = false) String status) {

        // Page<AdminFeedbackDto> feedbackList = adminService.getFeedbackListWithPaging(PageRequest.of(page, pageSize));
        Page<AdminFeedbackDto> feedbackList = adminService.getFeedbackListSSF(employeeName, startDate, endDate, status, PageRequest.of(page, 10));
        model.addAttribute("feedbackList", feedbackList);

        model.addAttribute("employeeName", employeeName !=null ? employeeName : "");
        model.addAttribute("startDate", startDate !=null ? startDate : "");
        model.addAttribute("endDate", endDate !=null ? endDate : "");
        model.addAttribute("status", status !=null ? status : "");

        // 이전페이지, 다음페이지, 페이지 번호 버튼 생성
        PaginationUtils.addPaginationAttributes(model, feedbackList, page);

        return "admin/adminFix";
    }

    @GetMapping("/adminLog")
    public String adminLog(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate,
                           @RequestParam(required = false) String speechType) {

        Page<Map<String, Object>> speechLogs = userService.getAllSpeechLogsSSF(startDate, endDate, speechType, PageRequest.of(page, 10));
        model.addAttribute("speechLogs", speechLogs);

        model.addAttribute("startDate", startDate != null ? startDate : "");
        model.addAttribute("endDate", endDate != null ? endDate : "");
        model.addAttribute("speechType", speechType != null ? speechType : "");
        
        // 이전페이지, 다음페이지, 페이지 번호 버튼 생성
        PaginationUtils.addPaginationAttributes(model, speechLogs, page);

        return "admin/adminLog";
    }
}