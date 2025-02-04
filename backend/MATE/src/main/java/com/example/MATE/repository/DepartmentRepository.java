package com.example.MATE.repository;

import com.example.MATE.model.Department;
import com.example.MATE.model.Meeting;
import com.example.MATE.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Integer>{

    Optional<Department> findByDepartmentName(String departmentName);

    //모든 departmentRepository 조회
    @Query("SELECT d.departmentId, d.departmentName FROM Department d")
    List<Object[]> findAllDepartmentIdAndNames();
}
