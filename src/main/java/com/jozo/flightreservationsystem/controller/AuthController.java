package com.jozo.flightreservationsystem.controller;

import com.jozo.flightreservationsystem.dto.LoginDto;
import com.jozo.flightreservationsystem.dto.RegisterDto;
import com.jozo.flightreservationsystem.model.AppUser;
import com.jozo.flightreservationsystem.repository.AppUserRepository;
import com.jozo.flightreservationsystem.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    AppUserService appUserService;

    @GetMapping("/register")
    public String register(Model model) {
        RegisterDto registerDto = new RegisterDto();
        model.addAttribute("registerDto", registerDto);
        model.addAttribute("success", false);
        return "register";
    }

    @PostMapping("/register")
    public String register(Model model, @Valid @ModelAttribute RegisterDto registerDto, BindingResult bindingResult) {

        if(!registerDto.getPassword().equals(registerDto.getMatchingPassword())){
            bindingResult.rejectValue("matchingPassword", "error.password.mismatch", "Passwords do not match");
        }

        AppUser appUser = appUserRepository.findByEmail(registerDto.getEmail());

        if(appUser != null){
            bindingResult.rejectValue("email", "error.email.exists", "This email address is already in use");
        }

        if(bindingResult.hasErrors()){
            return "register";
        }

        try {

            var bCryptEncoder = new BCryptPasswordEncoder();

            AppUser newAppUser = new AppUser();
            newAppUser.setFirstName(registerDto.getFirstName());
            newAppUser.setLastName(registerDto.getLastName());
            newAppUser.setEmail(registerDto.getEmail());
            newAppUser.setPassword(bCryptEncoder.encode(registerDto.getPassword()));

            appUserRepository.save(newAppUser);

            // Redirect to home page after successful registration
            return "redirect:/?registered=true";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/login")
    public String login(Model model) {
        LoginDto loginDto = new LoginDto();
        model.addAttribute("loginDto", loginDto);
        model.addAttribute("success", false);
        return "login";
    }


}
