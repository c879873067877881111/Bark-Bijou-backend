package com.smallnine.apiserver.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

// TODO: 先與前端串接後，再加入RBAC功能

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    
    private Long id;
    private String username;
    private String realname;
    private String email;
    private String password;
    private String phone;
    private Gender gender;
    private LocalDateTime birthDate;
    private Integer vipLevelsId = 1;
    private String imageUrl = "/member/member_images/user-img.svg";
    private LocalDateTime createdAt;
    private Boolean emailValidated = false;
    private String googleUid;
    private LocalDateTime imageUpdatedAt;
    private String resetToken;
    private LocalDateTime resetTokenExpiry;
    private String resetTokenSecret;
    private String googleName;
    private String city;
    private String address;
    private String zip;
    private LocalDateTime updatedAt;
    
    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return emailValidated != null ? emailValidated : false;
    }
    
    public enum Gender {
        male, female
    }
    
    public enum Role {
        USER, ADMIN, SITTER
    }
}