package com.jozo.flightreservationsystem.service;

import com.jozo.flightreservationsystem.model.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {
    private final String firstName;
    private final String lastName;

    public CustomUserDetails(AppUser appUser, Collection<? extends GrantedAuthority> authorities) {
        super(appUser.getEmail(), appUser.getPassword(), authorities);
        this.firstName = appUser.getFirstName();
        this.lastName = appUser.getLastName();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}


