package com.jozo.flightreservationsystem.service;

import com.jozo.flightreservationsystem.model.AppUser;
import com.jozo.flightreservationsystem.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AppUserService {

    @Autowired
    AppUserRepository appUserRepository;

    public boolean authenticateUser(String email, String password) {
        AppUser user = appUserRepository.findByEmail(email);
        if (user == null) {
            return false;
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(password, user.getPassword());
    }

    public Optional<AppUser> getUserById(Integer id) {
        return appUserRepository.findById(id);
    }

    public AppUser saveUser(AppUser user) {
        return appUserRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return appUserRepository.findByEmail(email) != null;
    }

    public Optional<AppUser> getUserByEmail(String email) {
        AppUser user = appUserRepository.findByEmail(email);
        return user != null ? Optional.of(user) : Optional.empty();
    }
}
