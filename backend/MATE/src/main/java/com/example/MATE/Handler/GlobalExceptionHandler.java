package com.example.MATE.Handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;


@ControllerAdvice
public class GlobalExceptionHandler {
    //403 권한문제 처리 - SpringSecurity필터의 403 에러는 잡지 못함- SecurityConfig에서 처리함
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException ex,
                                              HttpServletRequest request,
                                              Model model){
        model.addAttribute("error", "403 권한 오류.");
        model.addAttribute("url", request.getRequestURI());
        return "error/error";
    }
    //401 자격증명실패
    @ExceptionHandler(AuthenticationException.class)
    public String handleAuthenticationException(AuthenticationException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error/error"; //에러 페이지의 뷰 이름
    }
    //runtime
    @ExceptionHandler(RuntimeException.class)
    public RedirectView handlerRuntimeException(RuntimeException ex, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorMessage",ex.getMessage());
        return new RedirectView("/signIn");
    }
    //404처리
    @ExceptionHandler(NoHandlerFoundException.class)
    public String handle404(NoHandlerFoundException e, HttpServletRequest request, Model model) {
        model.addAttribute("error", "페이지를 찾을 수 없습니다.");
        model.addAttribute("url", request.getRequestURI());
        return "error/error";
    }

    //500처리
    @ExceptionHandler(Exception.class)
    public String handle500(Exception e, HttpServletRequest request, Model model) {
        model.addAttribute("error", "서버에 오류가 발생했습니다.");
        model.addAttribute("message", e.getMessage());
        return "error/error";
    }
}
