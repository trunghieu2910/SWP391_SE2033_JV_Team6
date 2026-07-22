
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
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasRole('PATIENT')")
public class MedicationReminderController {

    private final ProfileService profileService;
    private final MedicationReminderService reminderService;
    private final PatientRepository patientRepository;

    @GetMapping("/new")
    public String newReminder(Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
        Patient patient = getPatient(userDetails);

        if (!model.containsAttribute("reminderRequest")) {
            model.addAttribute("reminderRequest", new MedicationReminderRequest());
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

        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("reminders", reminderService.getAllReminders(patient.getPatientId()));
            return "patient/reminder-new";
        }

        reminderService.createReminder(patient.getPatientId(), reminderRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Đã tạo nhắc uống thuốc. Email sẽ được gửi tại giờ đã chọn mỗi ngày.");
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

        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("reminders", reminderService.getAllReminders(patient.getPatientId()));
            model.addAttribute("editingReminderId", reminderId);
            return "patient/reminder-new";
        }

        reminderService.updateReminder(patient.getPatientId(), reminderId, reminderRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật nhắc uống thuốc.");
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
