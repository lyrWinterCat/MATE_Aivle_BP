package com.example.MATE.controller;

import com.example.MATE.dto.RequestDto;
import com.example.MATE.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 관리자 관련 엔드포인트

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public String adminDashboard(){
        return "admin/admindashboard";
    }

    @GetMapping("/query/answer")
    public String adminQueryAnswer(){
        return "admin/adminqueryanswer";
    }

    @GetMapping("/query/search")
    public String adminQuerySearch(){
        return "admin/adminquerysearch";
    }

//    @GetMapping("/dashboard")
//    public String adminDashboard(){
//        return "admin/dashboard";
//    }
//
//    @GetMapping("/query")
//    public String adminSearch(){
//        return "admin/search";
//    }
//
//    @GetMapping("/query/answer")
//    public String adminAnswer(){
//        return "admin/answer";
//    }
//
//    @GetMapping("/log")
//    public String adminLog(){
//        return "admin/log";
//    }
//    @GetMapping("/dashboard")
//    public ResponseEntity<?> getAdminDashboard(){
//        return ResponseEntity.ok("Admin Dashboard");
//    }
//
//    @GetMapping("/search")
//    public ResponseEntity<List<RequestDto>> searchRequests() {
//        List<RequestDto> requests = adminService.getAllRequests();
//        return ResponseEntity.ok(requests);
//    }
//
//    @PostMapping("/answer")
//    public ResponseEntity<?> answerRequest(@RequestBody RequestDto requestDto) {
////        adminService.answerRequest(requestDto);
//        return ResponseEntity.ok("Request answered successfully");
//    }
}
