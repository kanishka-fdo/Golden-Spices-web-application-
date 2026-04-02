package com.kanishka.demo.auth;

import com.kanishka.demo.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // =========================================================
    // LOGIN PAGE
    // =========================================================
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String expired,
            @RequestParam(required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("loginError",
                    "Invalid email or password. Please try again.");
        }
        if (expired != null) {
            model.addAttribute("loginError",
                    "Your session has expired. Please log in again.");
        }
        if (logout != null) {
            model.addAttribute("loginSuccess",
                    "You have been logged out successfully.");
        }

        return "auth/login";
    }

    // =========================================================
    // REGISTER PAGE
    // =========================================================
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    // =========================================================
    // REGISTER SUBMIT
    // =========================================================
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute RegisterRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Return to form with validation errors
        if (result.hasErrors()) {
            model.addAttribute("registerRequest", request);
            return "auth/register";
        }

        try {
            authService.register(request);
            redirectAttributes.addFlashAttribute("success",
                    "Registration successful! Please log in.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            // Business-rule errors (duplicate email, weak password, etc.)
            model.addAttribute("registerRequest", request);
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}