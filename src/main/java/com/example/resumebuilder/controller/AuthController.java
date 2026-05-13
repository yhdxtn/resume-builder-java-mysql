package com.example.resumebuilder.controller;

import com.example.resumebuilder.dto.RegisterForm;
import com.example.resumebuilder.entity.AppUser;
import com.example.resumebuilder.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult bindingResult, Model model) {
        String username = form.getUsername() == null ? "" : form.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            bindingResult.rejectValue("username", "exists", "用户名已存在");
        }
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setFullName(form.getFullName());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole("USER");
        user.setPoints(100);
        if (username.contains("@")) {
            user.setContactEmail(username);
        }
        userRepository.save(user);
        model.addAttribute("registered", true);
        return "auth/login";
    }
}
