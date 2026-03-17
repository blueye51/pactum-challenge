package com.pareto.pactum_challenge.controller;

import com.pareto.pactum_challenge.entity.User;
import com.pareto.pactum_challenge.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SessionController {

    private final UserService userService;

    @GetMapping("/me")
    public User getOrCreateUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId != null) {
            User existing = userService.findById(userId);
            if (existing != null) {
                return existing;
            }
        }

        // First visit
        User user = userService.createGuest();
        session.setAttribute("userId", user.getId());
        return user;
    }
}
