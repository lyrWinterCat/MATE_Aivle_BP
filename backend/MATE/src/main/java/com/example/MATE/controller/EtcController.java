package com.example.MATE.controller;

import com.example.MATE.dto.DepartmentDto;
import com.example.MATE.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class EtcController {

    private final DepartmentService departmentService;

    //회원가입에서 사용 - 부서리스트
    @GetMapping("/departmentInfo")
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        List<DepartmentDto> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

}
