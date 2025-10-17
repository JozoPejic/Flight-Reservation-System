package com.jozo.flightreservationsystem.controller;

import com.jozo.flightreservationsystem.model.AppUser;
import com.jozo.flightreservationsystem.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private AppUserService appUserService;

    @GetMapping("/profile")
    public String userProfile(Model model) {
        AppUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("user", currentUser);
        return "user/profile";
    }

    @GetMapping("/bookings")
    public String userBookings(Model model) {
        AppUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("userId", currentUser.getId());
        return "redirect:/booking/my-bookings/" + currentUser.getId();
    }

    private AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            
            String email = authentication.getName();
            Optional<AppUser> user = appUserService.getUserByEmail(email);
            return user.orElse(null);
        }
        return null;
    }
}
