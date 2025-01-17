package com.example.MATE.controller;

import com.example.MATE.dto.RequestDto;
import com.example.MATE.model.AdminFeedback;
import com.example.MATE.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 관리자 관련 엔드포인트

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "admin/admindashboard";
    }

    @GetMapping("/query/answer")
    public String adminQueryAnswer() {
        return "admin/adminqueryanswer";
    }

    @GetMapping("/query/search")
    public String adminQuerySearch(Model model) {
        List<AdminFeedback> feedbackList = adminService.getFeedbackList();
        model.addAttribute("feedbackList", feedbackList);
        return "admin/adminquerysearch";
    }
}