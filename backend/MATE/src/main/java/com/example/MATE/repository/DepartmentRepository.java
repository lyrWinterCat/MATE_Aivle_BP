package com.example.MATE.repository;

import com.example.MATE.model.Department;
import com.example.MATE.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    Optional<Department> findByDepartmentName(String departmentName);
}
