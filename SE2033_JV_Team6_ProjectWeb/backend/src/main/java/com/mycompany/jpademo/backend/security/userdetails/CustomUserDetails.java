package com.mycompany.jpademo.backend.security.userdetails;

import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.UserStatusEnum;
import lombok.*;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Builder
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName());
        return Collections.singletonList(authority);
    }

    public User getUser() {
        return user;
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatusEnum.BLOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatusEnum.ACTIVE;
    }
}
