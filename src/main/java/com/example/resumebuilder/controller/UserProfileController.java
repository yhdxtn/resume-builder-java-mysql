package com.example.resumebuilder.controller;

import com.example.resumebuilder.dto.UserProfileForm;
import com.example.resumebuilder.entity.AppUser;
import com.example.resumebuilder.repository.UserRepository;
import com.example.resumebuilder.service.AvatarStorageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class UserProfileController {
    private final UserRepository userRepository;
    private final AvatarStorageService avatarStorageService;

    public UserProfileController(UserRepository userRepository, AvatarStorageService avatarStorageService) {
        this.userRepository = userRepository;
        this.avatarStorageService = avatarStorageService;
    }

    @GetMapping
    public String edit(Model model, Principal principal) {
        AppUser user = currentUser(principal);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", toForm(user));
        }
        model.addAttribute("user", user);
        return "profile/edit";
    }

    @PostMapping
    public String save(@ModelAttribute("form") UserProfileForm form,
                       @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                       Principal principal,
                       RedirectAttributes redirectAttributes) {
        AppUser user = currentUser(principal);
        try {
            user.setFullName(clean(form.getFullName()));
            user.setJobTitle(clean(form.getJobTitle()));
            user.setGraduationSchool(clean(form.getGraduationSchool()));
            user.setEducationLevel(clean(form.getEducationLevel()));
            user.setPhone(clean(form.getPhone()));
            user.setContactEmail(clean(form.getContactEmail()));
            user.setLocation(clean(form.getLocation()));
            user.setWebsite(clean(form.getWebsite()));

            if (Boolean.TRUE.equals(form.getRemoveDefaultAvatar())) {
                user.setDefaultAvatarPath(null);
            }
            String croppedAvatarPath = avatarStorageService.storeDataUriAvatar(user, form.getDefaultAvatarDataUri());
            if (croppedAvatarPath != null) {
                user.setDefaultAvatarPath(croppedAvatarPath);
            } else {
                String avatarPath = avatarStorageService.storeAvatar(user, avatarFile);
                if (avatarPath != null) {
                    user.setDefaultAvatarPath(avatarPath);
                }
            }
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "个人主页已保存。以后新建或编辑简历时，基本信息会自动带入。");
            return "redirect:/profile";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/profile";
        }
    }

    private UserProfileForm toForm(AppUser user) {
        UserProfileForm form = new UserProfileForm();
        form.setFullName(user.getFullName());
        form.setJobTitle(user.getJobTitle());
        form.setGraduationSchool(user.getGraduationSchool());
        form.setEducationLevel(user.getEducationLevel());
        form.setPhone(user.getPhone());
        form.setContactEmail(user.getContactEmail());
        form.setLocation(user.getLocation());
        form.setWebsite(user.getWebsite());
        form.setRemoveDefaultAvatar(false);
        return form;
    }

    private AppUser currentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName()).orElseThrow();
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }
}
