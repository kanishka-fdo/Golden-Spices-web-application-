package com.kanishka.demo.controller;

// ── IMPORTANT: This file must stay in com.kanishka.demo.controller
//    Do NOT move it to com.kanishka.demo.security ──

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request,
                              HttpServletResponse response) {

        Object statusAttr = request.getAttribute(
                RequestDispatcher.ERROR_STATUS_CODE);
        Object errorAttr  = request.getAttribute(
                RequestDispatcher.ERROR_EXCEPTION);

        // ── If it's a UsernameNotFoundException or "User not found" from
        //    RememberMe / SecurityConfig, clear the bad cookie and redirect
        //    to login so the user sees a clean login page. ──
        if (errorAttr instanceof Throwable t) {
            String msg = t.getMessage() != null ? t.getMessage() : "";
            if (isAuthRelated(t, msg)) {
                clearRememberMeCookie(response);
                return "redirect:/auth/login?expired=true";
            }
        }

        // Check the error message attribute too
        Object errorMsg = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (errorMsg instanceof String s && isUserNotFound(s)) {
            clearRememberMeCookie(response);
            return "redirect:/auth/login?expired=true";
        }

        if (statusAttr != null) {
            int statusCode = Integer.parseInt(statusAttr.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "redirect:/";
            }
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "redirect:/auth/login";
            }
            if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                return "redirect:/auth/login";
            }
            if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                // Check if caused by auth issue
                if (errorAttr instanceof Throwable t) {
                    Throwable cause = t;
                    while (cause != null) {
                        if (isAuthRelated(cause, cause.getMessage() != null
                                ? cause.getMessage() : "")) {
                            clearRememberMeCookie(response);
                            return "redirect:/auth/login?expired=true";
                        }
                        cause = cause.getCause();
                    }
                }
                return "redirect:/";
            }
        }

        return "redirect:/";
    }

    // ── Detect authentication-related exceptions ──
    private boolean isAuthRelated(Throwable t, String msg) {
        return t instanceof org.springframework.security.core.userdetails.UsernameNotFoundException
                || t instanceof org.springframework.security.core.AuthenticationException
                || isUserNotFound(msg);
    }

    private boolean isUserNotFound(String msg) {
        if (msg == null) return false;
        String lower = msg.toLowerCase();
        return lower.contains("user not found")
                || lower.contains("no account found")
                || lower.contains("usernamenotfoundexception");
    }

    // ── Delete the remember-me cookie so the browser stops sending it ──
    private void clearRememberMeCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("remember-me", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        // Also clear JSESSIONID in case session is corrupt
        Cookie sessionCookie = new Cookie("JSESSIONID", null);
        sessionCookie.setMaxAge(0);
        sessionCookie.setPath("/");
        response.addCookie(sessionCookie);
    }
}