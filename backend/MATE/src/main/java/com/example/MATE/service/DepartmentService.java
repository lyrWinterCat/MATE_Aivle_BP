package com.example.MATE.service;

import com.example.MATE.dto.DepartmentDto;
import com.example.MATE.model.Meeting;
import com.example.MATE.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    public List<DepartmentDto> getAllDepartments() {
        List<Object[]> results = departmentRepository.findAllDepartmentIdAndNames();

        return results.stream()
                .map(obj -> new DepartmentDto((Integer) obj[0], (String) obj[1]))
                .collect(Collectors.toList());
    }
}
