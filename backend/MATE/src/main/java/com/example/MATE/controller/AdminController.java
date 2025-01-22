package com.example.MATE.controller;

import com.example.MATE.dto.RequestDto;
import com.example.MATE.model.AdminFeedback;
import com.example.MATE.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

// 관리자 관련 엔드포인트

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

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
    public String adminFix(Model model) {
        List<AdminFeedback> feedbackList = adminService.getFeedbackList();
        model.addAttribute("feedbackList", feedbackList);
        return "admin/adminFix";
    }
}