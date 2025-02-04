package com.example.MATE.service;

import com.example.MATE.dto.LoginRequest;
import com.example.MATE.dto.UserDto;
import com.example.MATE.model.Department;
import com.example.MATE.model.User;
import com.example.MATE.repository.DepartmentRepository;
import com.example.MATE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //사용자 정의 exception
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class LoginException extends RuntimeException {
        public LoginException(String message) {super(message);}
    }

    //회원가입
    @Transactional
    public void signup(UserDto userDto){
        System.out.println(">>> [LoginService] 회원 가입");

        try {
            //유효 부서명 확인
            System.out.println(">>> [LoginService] 회원 가입 유효부서명? :"+userDto.getDepartmentName()+":");
            Department department = departmentRepository.findByDepartmentName(userDto.getDepartmentName())
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 부서명입니다."+userDto.getDepartmentName()));

            //계정여부 확인
            if(userRepository.existsByEmail(userDto.getEmail())){
                throw new LoginException("이미 계정이 존재합니다.");
            }

            //패스워드 암호화
            String encryptPW = passwordEncoder.encode(userDto.getPassword());

            //User엔티티 생성 및 department 설정
            User user = new User();
            user.setDepartment(department);
            user.setEmail(userDto.getEmail());
            user.setPassword(encryptPW);
            user.setName(userDto.getName());
            user.setRole(User.Role.USER);
            user.setSocial(false);

            userRepository.save(user);
        }catch(Exception ex){
            //예외 로그 출력
            System.out.println(">>> DB insert 오류 [LoginService]"+ex.getMessage());
            throw ex;
        }
    }

    public boolean isEmailDuplicated(String email){
        return userRepository.existsByEmail(email);
    }
}
