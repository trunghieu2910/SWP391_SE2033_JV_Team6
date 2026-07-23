package com.mycompany.jpademo.backend.security.userdetails;

import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.UserStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Wraps the User entity into UserDetails — the standard shape Spring
 * Security understands to know: what authority this user has, what their
 * hashed password is, and whether the account is locked/disabled.
 */
public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /** Shortcut to get back the underlying User entity — used in
     *  Controllers/Handlers that need fields beyond the standard UserDetails
     *  contract (e.g. fullName, email). */
    public User getUser() {
        return user;
    }

    /** Assigns a single authority in the form "ROLE_<ROLE_NAME>" (e.g.
     *  ROLE_DOCTOR) — consumed by hasRole()/hasAuthority() in SecurityConfig
     *  and by @PreAuthorize. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(
                "ROLE_" + user.getRole().getRoleName().name()));
    }

    /** Returns the HASHED password (bcrypt) — Spring Security matches it
     *  against the entered password using PasswordEncoder; we never compare
     *  passwords manually. */
    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    /** The "username" Spring Security uses internally to identify the login
     *  session — comes from user.getUserName(), NOT the email/phone/ID the
     *  person may have actually typed to log in. */
    @Override
    public String getUsername() {
        return user.getUserName();
    }

    /** Whether the account expires over time — this feature isn't used in
     *  this project, so it always returns true (never expires). */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** Whether the account is PERMANENTLY locked — only false when
     *  status = BANNED. Note: TEMPORARY locking (from repeated failed
     *  logins) is handled separately in CustomUserDetailsService, not by
     *  this flag. */
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.BANNED;
    }

    /** Whether the password has expired (forced periodic password change) —
     *  not used yet, always true. */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** Whether the account has finished OTP activation — only true when
     *  status = ACTIVE (a freshly registered account stays PENDING, and
     *  therefore cannot log in, until OTP verification is completed). */
    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }
}
