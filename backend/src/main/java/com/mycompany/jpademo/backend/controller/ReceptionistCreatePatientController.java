package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.RegisterRequest;
import com.mycompany.jpademo.backend.service.interfaces.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/receptionist/patients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECEPTIONIST')")
public class ReceptionistCreatePatientController {

    private final AuthService authService;

    // Hiển thị form tạo tài khoản
    @GetMapping("/create")
    public String showCreatePatientPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "receptionist/create-patient";
    }

    // Xử lý logic tạo tài khoản (Tái sử dụng AuthService)
    @PostMapping("/create")
    public String createPatient(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // Kiểm tra mật khẩu và xác nhận mật khẩu khớp nhau (bắt buộc do dùng chung DTO)
        if (request.getPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.registerRequest", "Mật khẩu xác nhận không khớp.");
        }

        if (bindingResult.hasErrors()) {
            return "receptionist/create-patient";
        }

        try {
            // Sử dụng hàm đăng ký gốc để ép buộc quy trình gửi OTP qua Email
            authService.register(request);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Tạo tài khoản thành công! Hệ thống đã gửi mã OTP tới Email của bệnh nhân. Vui lòng xác thực để kích hoạt tài khoản.");
            redirectAttributes.addAttribute("userName", request.getUserName());
            
            // Chuyển hướng sang màn hình nhập mã OTP
            return "redirect:/auth/register/verify-otp";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "receptionist/create-patient";
        }
    }
}
