package com.example.MATE.controller;

import com.example.MATE.dto.AdminFeedbackDto;
import com.example.MATE.dto.MeetingDetailDto;
import com.example.MATE.dto.MeetingLogDto;
import com.example.MATE.dto.SpeechLogDto;
import com.example.MATE.model.*;
import com.example.MATE.repository.UserRepository;
import com.example.MATE.service.AdminService;
import com.example.MATE.service.MeetingService;
import com.example.MATE.service.UserService;
import com.example.MATE.utils.DateUtil;
import com.example.MATE.utils.PaginationUtils;
import com.example.MATE.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AdminService adminService;
    private final MeetingService meetingService;

    private static final String UPLOAD_DIR = "file"; // 파일 저장 폴더

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
                model.addAttribute("userId",user.getUserId());
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

    // "상세보기" 버튼을 누르면 해당 회의의 정보, 요약, todolist 를 보여줌
    @GetMapping("/meetingDetail")
    @PreAuthorize("hasAuthority('USER')")
    public String meetingDetail(@RequestParam("meetingId") Integer meetingId, Model model, HttpSession session) {

        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) { // 로그인 안 된 사용자는 리스트로 리디렉트
            return "redirect:/user/meetingList";
        }
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) { // 존재하지 않는 사용자도 리디렉트
            return "redirect:/user/meetingList";
        }
        User user = userOptional.get();
        model.addAttribute("userName", user.getName());

        MeetingDetailDto meetingDetail = meetingService.getMeetingDetailById(meetingId);
        if (meetingDetail == null) {
            throw new IllegalArgumentException("유효하지 않은 회의 ID: " + meetingId);
        }
        model.addAttribute("meetingDetail", meetingDetail);

        //회의 참여자에서 내가 참여했는지 확인
        List<MeetingParticipant> participants = meetingService.getParticipantsByMeetingId(meetingId);
        boolean isParticipant = participants.stream()
                .anyMatch(participant -> participant.getUser() != null && participant.getUser().getUserId().equals(user.getUserId()));

        if (!isParticipant) {
            return "redirect:/user/meetingList";
        }
        return "user/meetingDetail";
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

                model.addAttribute("startDate", startDate !=null ? startDate : "");
                model.addAttribute("endDate", endDate !=null ? endDate : "");
                model.addAttribute("speechType", speechType !=null ? speechType :  "");

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

        // return "user/detail";

        // detail.html 과 write.html 은 분리될 필요가 있음
        return "user/write";
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

            // 파일 저장 로직 - MP3 파일만 허용
            if (file != null && !file.isEmpty()) {
                String originalFileName = file.getOriginalFilename();


                // 파일 확장자 검증 (MP3, wav, m4a 허용)
                if (originalFileName == null || !originalFileName.matches(".*\\.(mp3|wav|m4a)$")) {
                    throw new IllegalArgumentException("MP3, WAV, M4A 파일만 업로드 가능합니다.");
                }

                // 저장 폴더 생성 (없으면 생성)
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // 저장할 파일 경로 지정
                Path filePath = Paths.get(UPLOAD_DIR, originalFileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                feedbackDto.setFilepath(originalFileName); // DB에는 파일명만 저장
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
        if (email == null) { // 로그인 안 된 사용자는 리스트로 리디렉트
            return "redirect:/user/userFix";
        }
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) { // 존재하지 않는 사용자도 리디렉트
            return "redirect:/user/userFix";
        }
        User user = userOptional.get();
        model.addAttribute("userName", user.getName());
        model.addAttribute("userId", user.getUserId());

        AdminFeedback adminFeedback = adminService.getFeedbackById(feedbackId);
        if(adminFeedback == null){
            throw new IllegalArgumentException("유효하지 않는 정정요청ID : "+feedbackId);
        }
        //피드백의 userId와 현재 userId 비교
        if(!adminFeedback.getUser().getUserId().equals(user.getUserId())){
            return "redirect:/user/userFix";
        }
        ToxicityLog toxicityLog = adminFeedback.getToxicityLog();
        SpeechLog speechLog = toxicityLog.getSpeechLog();
        model.addAttribute("toxicitySpeechLog", speechLog.getContent());
        model.addAttribute("toxicitySpeechLog_time", DateUtil.format(speechLog.getTimestamp()));
        model.addAttribute("createdAt_format", DateUtil.format(adminFeedback.getCreatedAt()));

        model.addAttribute("feedback", adminFeedback);

        String response = adminService.getCommentByFeedbackId(feedbackId)
                .map(AdminFeedbackComments::getContent)
                .orElse(null);
        model.addAttribute("response", response);

        return "user/detail";
    }

    // WebConfig 의 정적 리소스 매핑을 통해 파일 다운로드
    // 다운로드 버튼 누르면 /file/{{filepath}} URL 로 요청을 보냄
    @GetMapping("/{filename}")
    public void downloadFile(@PathVariable String filename, HttpServletResponse response) {
        File file = new File(UPLOAD_DIR, filename);
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 클라이언트에게 파일을 보내는 코드
        // 서버가 클라이언트에게 응답 스트림을 통해 데이터를 보냄
        try (FileInputStream fis = new FileInputStream(file)) {

            // 브라우저가 파일을 바로 열지 않고, 다운로드 창이 뜨도록 설정
            response.setContentType("application/octet-stream"); // 모든 파일 형식에 적용 가능

            // 파일명 형식 설정
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            // 파일을 읽어서 클라이언트에게 전송
            fis.transferTo(response.getOutputStream());

            // 전송 강제 완료
            response.flushBuffer();
        } catch (IOException e) {
            System.out.println(">>> 파일 다운로드 실패: " + e.getMessage());
        }
    }
}
