package com.mycompany.jpademo.backend.security.userdetails;

import com.mycompany.jpademo.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor

public class CustomUserDetails implements UserDetails {

    // 1. Cái hộp này sẽ chứa đối tượng User thực sự lấy từ Database lên
    private final User user;

    // 2. Hàm này móc cái Role của User ra, biến nó thành Quyền (Quyền của Spring luôn bắt đầu bằng ROLE_)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // Mấy hàm này tạm thời cho return true hết để tài khoản luôn hoạt động
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
        return true;
    }
}