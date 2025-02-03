package com.example.MATE.controller;

import com.example.MATE.dto.AdminFeedbackDto;
import com.example.MATE.dto.MeetingLogDto;
import com.example.MATE.dto.SpeechLogDto;
import com.example.MATE.model.AdminFeedback;
import com.example.MATE.model.SpeechLog;
import com.example.MATE.model.ToxicityLog;
import com.example.MATE.model.User;
import com.example.MATE.repository.UserRepository;
import com.example.MATE.service.AdminService;
import com.example.MATE.service.UserService;
import com.example.MATE.utils.DateUtil;
import com.example.MATE.utils.PaginationUtils;
import com.example.MATE.utils.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AdminService adminService;

    //유저메인페이지
    @GetMapping("/userMain")
    @PreAuthorize("hasAuthority('USER')")
    public String userMain(Model model, HttpSession session){
        System.out.println(">>> [UserController/userMain] 유저 메인 페이지 로드");

        //세션에서 사용자 메일가져오기
        String email = SecurityUtils.getCurrentUserEmail();
        //DB에서 사용자 정보 조회-이름과 롤 가져오기 위함
        if (email != null) {
            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isPresent()) { //DB에 있음
                User user = userOptional.get();
                System.out.println(">>> [UserController/userMain] "+user.getName());
                model.addAttribute("userName", user.getName());
                //model.addAttribute("userRole", user.getRole().name());
            }
        }

        //세션에서 에러메세지 받기
        String errorMessage = (String) session.getAttribute("errorMessage");
        if(errorMessage != null){
            errorMessage = URLDecoder.decode(errorMessage, StandardCharsets.UTF_8);
            model.addAttribute("errorMessage", errorMessage);
            session.removeAttribute("errorMessage");
        }
        return "user/userMain";
    }

    // 현재 로그인한 유저가 참여한 모든 회의를 보여주는 페이지
    @GetMapping("/meetingList")
    @PreAuthorize("hasAuthority('USER')")
    public String meetingList(Model model, HttpSession session,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(required = false) String employeeName,
                              @RequestParam(required = false) String startDate,
                              @RequestParam(required = false) String endDate){

        //세션에서 사용자 메일가져오기
        String email = SecurityUtils.getCurrentUserEmail();
        //DB에서 사용자 정보 조회-이름과 롤 가져오기 위함
        if (email != null) {
            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isPresent()) { //DB에 있음
                User user = userOptional.get();
                Integer userId = user.getUserId();
                //상단에 사용자명 노출
                System.out.println(">>> [UserController/meetingList] "+user.getName());
                model.addAttribute("userName", user.getName());

                //참여 미팅 로그
                Page<MeetingLogDto> meetingLogs = userService.getMeetingLogsSSF(userId, employeeName, startDate, endDate, PageRequest.of(page, 10));
                model.addAttribute("meetingLogs", meetingLogs);

                model.addAttribute("employeeName", employeeName !=null ? employeeName : "");
                model.addAttribute("startDate", startDate !=null ? startDate : "");
                model.addAttribute("endDate", endDate !=null ? endDate : "");

                // 이전페이지, 다음페이지, 페이지 번호 버튼 생성
                PaginationUtils.addPaginationAttributes(model, meetingLogs, page);
            }
        }

        return "user/meetingList";
    }
    //마이페이지 로그
    @GetMapping("/speechLog")
    @PreAuthorize("hasAuthority('USER')")
    public String speechLog(Model model, HttpSession session,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate,
                            @RequestParam(required = false) String speechType) {

        String email = SecurityUtils.getCurrentUserEmail();
        if (email != null) {
            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                Integer userId = user.getUserId();

                model.addAttribute("userName", user.getName());

                // 유저 ID 를 받아 해당 유저가 발화한 모든 발화 로그를 반환
                Page<Map<String, Object>> speechLogs = userService.getSpeechLogsByUserIdSSF(userId, startDate, endDate, speechType, PageRequest.of(page, 10));
                model.addAttribute("speechLogs", speechLogs);

                PaginationUtils.addPaginationAttributes(model, speechLogs, page);
            }
        }

        return "user/speechLog";
    }
    
    //정정게시판 이동
    @GetMapping("/userFix")
    @PreAuthorize("hasAuthority('USER')")
    public String userFix(Model model, HttpSession session,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate,
                          @RequestParam(required = false) String status) {
        //현재 사용자 조회
        String email = SecurityUtils.getCurrentUserEmail();
        if (email != null) {
            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                Integer userId = user.getUserId();
                model.addAttribute("userId", userId);
                model.addAttribute("userName", user.getName());

                model.addAttribute("startDate", startDate !=null ? startDate : "");
                model.addAttribute("endDate", endDate !=null ? endDate : "");
                model.addAttribute("status", status !=null ? status : "");

                // 현재 사용자의 정정게시글 조회
                Page<AdminFeedbackDto> userFixes = adminService.getFeedbackByUserIdWithFilter(userId, startDate, endDate, status, PageRequest.of(page, 10));
                model.addAttribute("userFixes", userFixes);

                PaginationUtils.addPaginationAttributes(model, userFixes, page);

            }
        }
        return "user/userFix";
    }

    //정정게시판-정정요청하기 버튼 클릭
    @GetMapping("/userFix/write")
    @PreAuthorize("hasAuthority('USER')")
    public String userFixWrite(Model model){
        String email = SecurityUtils.getCurrentUserEmail();
        //DB에서 사용자 정보 조회-이름과 롤 가져오기 위함
        if (email != null) {
            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isPresent()) { //DB에 있음
                User user = userOptional.get();
                Integer userId = user.getUserId();
                System.out.println(">>> [UserController/userFixWrite] "+user.getName());
                model.addAttribute("userId", user.getUserId());
                model.addAttribute("userName", user.getName());
            }
        }

        return "user/detail";
    }

    //정정게시판 -  정정 요청하기 글 제출(DB저장)
    @PostMapping("/userFix/write")
    @PreAuthorize("hasAuthority('USER')")
    public String writeFeedBack(
            @RequestParam("userId") Integer userId,
            @RequestParam("toxicityId") Integer toxicityId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "filepath", required = false) MultipartFile file){
        System.out.println(">>> 정정");
        try {
            AdminFeedbackDto feedbackDto = new AdminFeedbackDto();

            feedbackDto.setUserId(userId);
            feedbackDto.setToxicityId(toxicityId);
            feedbackDto.setTitle(title);
            feedbackDto.setContent(content);

            //파일 저장 로직
            if(!file.isEmpty()){
                String filePath = "/img/" + file.getOriginalFilename();
                feedbackDto.setFilepath(filePath);
            }
            userService.writeFeedBack(feedbackDto);
            return "redirect:/user/userFix";
        }catch(Exception e){
            System.out.println(">>> [UserController/writeFeedback] 실패 :"+e.getMessage());
            return "redirect:/user/userFix";
        }
    }

    //정정게시글 1개 조회
    @GetMapping("/userFix/detail")
    @PreAuthorize("hasAuthority('USER')")
    public String readFeedBack(@RequestParam("feedbackId") Integer feedbackId, Model model){

        String email = SecurityUtils.getCurrentUserEmail();
        //DB에서 사용자 정보 조회-이름과 롤 가져오기 위함
        if (email != null) {
            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isPresent()) { //DB에 있음
                User user = userOptional.get();
                System.out.println(">>> [UserController/readFeedBack] "+user.getName());
                System.out.println(">>> [UserController/readFeedBack] "+user.getUserId());
                model.addAttribute("userId", user.getUserId());
                model.addAttribute("userName", user.getName());
            }
        }
        AdminFeedback adminFeedback = adminService.getFeedbackById(feedbackId);
        if(adminFeedback == null){
            throw new IllegalArgumentException("유효하지 않는 정정요청ID : "+feedbackId);
        }
        ToxicityLog toxicityLog = adminFeedback.getToxicityLog();
        SpeechLog speechLog = toxicityLog.getSpeechLog();
        model.addAttribute("toxicitySpeechLog", speechLog.getContent());
        model.addAttribute("toxicitySpeechLog_time", DateUtil.format(speechLog.getTimestamp()));
        model.addAttribute("createdAt_format", DateUtil.format(adminFeedback.getCreatedAt()));

        if (adminFeedback.getFilepath() == null) {
            adminFeedback.setFilepath("");  // 빈 문자열로 설정하여 Mustache 오류 방지
        }
        model.addAttribute("feedback", adminFeedback);
        return "user/detail";
    }
}
