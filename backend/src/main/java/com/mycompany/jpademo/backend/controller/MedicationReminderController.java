
package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.MedicationReminderRequest;
import com.mycompany.jpademo.backend.dto.response.MedicationReminderResponse;
import com.mycompany.jpademo.backend.dto.response.ProfileResponse;
import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.repository.PatientRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.MedicationReminderService;
import com.mycompany.jpademo.backend.service.interfaces.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/patient/reminders")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicationReminderController {

    private final ProfileService profileService;
    private final MedicationReminderService reminderService;
    private final PatientRepository patientRepository;

    @GetMapping("/new")
    public String newReminder(@org.springframework.web.bind.annotation.RequestParam(required = false) String note,
                              Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
        Patient patient = getPatient(userDetails);

        if (!model.containsAttribute("reminderRequest")) {
            MedicationReminderRequest req = new MedicationReminderRequest();
            if (note != null && !note.isBlank()) {
                req.setNote(note.trim());
            }
            model.addAttribute("reminderRequest", req);
        }

        List<MedicationReminderResponse> reminders = reminderService.getAllReminders(patient.getPatientId());

        model.addAttribute("profile", profile);
        model.addAttribute("reminders", reminders);

        return "patient/reminder-new";
    }

    @GetMapping("/{reminderId}/edit")
    public String editReminder(@PathVariable Integer reminderId,
                               Model model,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
        Patient patient = getPatient(userDetails);
        MedicationReminderResponse reminder = reminderService.getReminder(patient.getPatientId(), reminderId);

        if (!model.containsAttribute("reminderRequest")) {
            model.addAttribute("reminderRequest", MedicationReminderRequest.builder()
                    .note(reminder.getNote())
                    .scheduledTime(reminder.getScheduledTime())
                    .build());
        }

        model.addAttribute("profile", profile);
        model.addAttribute("reminders", reminderService.getAllReminders(patient.getPatientId()));
        model.addAttribute("editingReminderId", reminderId);

        return "patient/reminder-new";
    }

    @PostMapping("/new")
    @Transactional
    public String createReminder(@Valid @ModelAttribute("reminderRequest") MedicationReminderRequest reminderRequest,
                                 BindingResult bindingResult,
                                 Model model,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
        Patient patient = getPatient(userDetails);

        // Lấy danh sách tất cả các mốc giờ người dùng chọn
        java.util.List<java.time.LocalTime> timesList = new java.util.ArrayList<>();
        if (reminderRequest.getScheduledTimes() != null && !reminderRequest.getScheduledTimes().isEmpty()) {
            for (java.time.LocalTime t : reminderRequest.getScheduledTimes()) {
                if (t != null) timesList.add(t);
            }
        }
        if (timesList.isEmpty() && reminderRequest.getScheduledTime() != null) {
            timesList.add(reminderRequest.getScheduledTime());
        }

        if (timesList.isEmpty()) {
            bindingResult.rejectValue("scheduledTime", "error.scheduledTime", "Vui lòng chọn ít nhất một khung giờ nhắc");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("reminders", reminderService.getAllReminders(patient.getPatientId()));
            return "patient/reminder-new";
        }

        int count = 0;
        for (java.time.LocalTime time : timesList) {
            MedicationReminderRequest singleReq = MedicationReminderRequest.builder()
                    .note(reminderRequest.getNote())
                    .scheduledTime(time)
                    .build();
            reminderService.createReminder(patient.getPatientId(), singleReq);
            count++;
        }

        redirectAttributes.addFlashAttribute("successMessage", "Đã tạo " + count + " lời nhắc uống thuốc thành công!");
        return "redirect:/patient/reminders/new";
    }

    @PostMapping("/{reminderId}/edit")
    @Transactional
    public String updateReminder(@PathVariable Integer reminderId,
                                 @Valid @ModelAttribute("reminderRequest") MedicationReminderRequest reminderRequest,
                                 BindingResult bindingResult,
                                 Model model,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
        Patient patient = getPatient(userDetails);

        java.util.List<java.time.LocalTime> timesList = new java.util.ArrayList<>();
        if (reminderRequest.getScheduledTimes() != null && !reminderRequest.getScheduledTimes().isEmpty()) {
            for (java.time.LocalTime t : reminderRequest.getScheduledTimes()) {
                if (t != null) timesList.add(t);
            }
        }
        if (timesList.isEmpty() && reminderRequest.getScheduledTime() != null) {
            timesList.add(reminderRequest.getScheduledTime());
        }

        if (timesList.isEmpty()) {
            bindingResult.rejectValue("scheduledTime", "error.scheduledTime", "Vui lòng chọn ít nhất một khung giờ nhắc");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("reminders", reminderService.getAllReminders(patient.getPatientId()));
            model.addAttribute("editingReminderId", reminderId);
            return "patient/reminder-new";
        }

        // Mốc giờ đầu tiên -> cập nhật bản ghi nhắc hiện tại
        MedicationReminderRequest firstReq = MedicationReminderRequest.builder()
                .note(reminderRequest.getNote())
                .scheduledTime(timesList.get(0))
                .build();
        reminderService.updateReminder(patient.getPatientId(), reminderId, firstReq);

        // Nếu có các mốc giờ bổ sung -> tạo mới lời nhắc cho từng mốc giờ
        for (int i = 1; i < timesList.size(); i++) {
            MedicationReminderRequest addReq = MedicationReminderRequest.builder()
                    .note(reminderRequest.getNote())
                    .scheduledTime(timesList.get(i))
                    .build();
            reminderService.createReminder(patient.getPatientId(), addReq);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật lời nhắc uống thuốc thành công!");
        return "redirect:/patient/reminders/new";
    }

    @PostMapping("/{reminderId}/delete")
    @Transactional
    public String deleteReminder(@PathVariable Integer reminderId,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        Patient patient = getPatient(userDetails);
        reminderService.deleteReminder(patient.getPatientId(), reminderId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa nhắc uống thuốc.");
        return "redirect:/patient/reminders/new";
    }

    private Patient getPatient(CustomUserDetails userDetails) {
        return patientRepository.findByUser(userDetails.getUser())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy patient cho user hiện tại."));
    }
}
