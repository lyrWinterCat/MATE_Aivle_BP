package com.example.MATE.dto;

import com.example.MATE.model.Department;
import com.example.MATE.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private int userId;
    private String departmentName;
    private String email;
    private String password;
    private String name;
    private User.Role role;
    private boolean isSocial;

    public static UserDto of (User user){
        UserDto userDto = new UserDto();
        userDto.setUserId(user.getUserId());
        userDto.setDepartmentName(user.getDepartment().getDepartmentName());
        userDto.setPassword(user.getPassword());
        userDto.setName(user.getName());
        userDto.setRole(user.getRole());
        userDto.setSocial(user.isSocial());

        return userDto;
    }
}
