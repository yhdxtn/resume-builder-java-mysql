package com.example.resumebuilder.controller;

import com.example.resumebuilder.entity.AppUser;
import com.example.resumebuilder.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(Model model) {
        List<AppUser> users = userRepository.findAll().stream()
                .sorted(Comparator.comparing(AppUser::getId, Comparator.nullsLast(Long::compareTo)).reversed())
                .toList();
        int totalPoints = users.stream().mapToInt(u -> u.getPoints() == null ? 0 : u.getPoints()).sum();
        long enabledCount = users.stream().filter(AppUser::isEnabled).count();
        model.addAttribute("users", users);
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("enabledCount", enabledCount);
        model.addAttribute("totalPoints", totalPoints);
        return "admin/users/list";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @RequestParam(defaultValue = "0") Integer points,
                         @RequestParam(required = false) String role,
                         @RequestParam(required = false) Boolean enabled,
                         @RequestParam(required = false, defaultValue = "save") String action,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        AppUser user = userRepository.findById(id).orElseThrow();
        AppUser currentAdmin = userRepository.findByUsername(principal.getName()).orElseThrow();
        String cleanRole = normalizeRole(role);
        int safePoints = Math.max(0, points == null ? 0 : points);
        if ("plus100".equals(action)) {
            safePoints = safePoints + 100;
        } else if ("minus100".equals(action)) {
            safePoints = Math.max(0, safePoints - 100);
        }
        boolean nextEnabled = Boolean.TRUE.equals(enabled);

        if (user.getId().equals(currentAdmin.getId())) {
            if (!nextEnabled) {
                redirectAttributes.addFlashAttribute("error", "不能禁用当前正在登录的管理员账号。 ");
                return "redirect:/admin/users";
            }
            if (!"ADMIN".equals(cleanRole)) {
                redirectAttributes.addFlashAttribute("error", "不能把当前管理员降级为普通用户。 ");
                return "redirect:/admin/users";
            }
        }

        user.setPoints(safePoints);
        user.setRole(cleanRole);
        user.setEnabled(nextEnabled);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "用户“" + user.getUsername() + "”已更新。 ");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/points")
    public String addPoints(@PathVariable Long id,
                            @RequestParam(defaultValue = "0") Integer delta,
                            RedirectAttributes redirectAttributes) {
        AppUser user = userRepository.findById(id).orElseThrow();
        int current = user.getPoints() == null ? 0 : user.getPoints();
        int next = Math.max(0, current + (delta == null ? 0 : delta));
        user.setPoints(next);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "用户“" + user.getUsername() + "”积分已调整为 " + next + "。 ");
        return "redirect:/admin/users";
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "USER";
        }
        String clean = role.trim().toUpperCase();
        return "ADMIN".equals(clean) ? "ADMIN" : "USER";
    }
}
