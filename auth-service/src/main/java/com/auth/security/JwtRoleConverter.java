package com.auth.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Component
public class JwtRoleConverter {

    private static final Map<String, GrantedAuthority> ROLE_MAP = Map.of(
            "USER", new SimpleGrantedAuthority("ROLE_USER"),
            "ADMIN", new SimpleGrantedAuthority("ROLE_ADMIN"),
            "SERVICE", new SimpleGrantedAuthority("ROLE_SERVICE")
    );

    public Collection<GrantedAuthority> convert(String role) {
        if (role == null) {
            return Collections.emptyList();
        }
        GrantedAuthority authority = ROLE_MAP.get(role);
        if (authority == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(authority);
    }
}
