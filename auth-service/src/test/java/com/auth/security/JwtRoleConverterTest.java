package com.auth.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class JwtRoleConverterTest {

    private final JwtRoleConverter converter = new JwtRoleConverter();

    @Test
    @DisplayName("Role mapping: strictly maps USER to ROLE_USER")
    void convert_mapsUserRoleToRoleUser() {
        Collection<GrantedAuthority> authorities = converter.convert("USER");
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Role mapping: strictly maps ADMIN to ROLE_ADMIN")
    void convert_mapsAdminRoleToRoleAdmin() {
        Collection<GrantedAuthority> authorities = converter.convert("ADMIN");
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Role mapping: strictly maps SERVICE to ROLE_SERVICE")
    void convert_mapsServiceRoleToRoleService() {
        Collection<GrantedAuthority> authorities = converter.convert("SERVICE");
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_SERVICE");
    }

    @Test
    @DisplayName("Strict whitelist: lowercase roles (user, admin, service) yield no authorities")
    void convert_lowercaseRolesYieldNoAuthority() {
        assertThat(converter.convert("user")).isEmpty();
        assertThat(converter.convert("admin")).isEmpty();
        assertThat(converter.convert("service")).isEmpty();
    }

    @Test
    @DisplayName("Strict whitelist: already-prefixed roles (ROLE_ADMIN, ROLE_USER, ROLE_SERVICE) yield no authorities")
    void convert_prefixedRolesYieldNoAuthority() {
        assertThat(converter.convert("ROLE_USER")).isEmpty();
        assertThat(converter.convert("ROLE_ADMIN")).isEmpty();
        assertThat(converter.convert("ROLE_SERVICE")).isEmpty();
    }

    @Test
    @DisplayName("Strict whitelist: unlisted roles yield no authorities")
    void convert_unlistedRolesYieldNoAuthority() {
        assertThat(converter.convert("ROOT")).isEmpty();
        assertThat(converter.convert("GUEST")).isEmpty();
        assertThat(converter.convert("OPERATOR")).isEmpty();
    }

    @Test
    @DisplayName("Strict whitelist: null, empty, or blank roles yield no authorities")
    void convert_nullOrBlankRoleYieldNoAuthority() {
        assertThat(converter.convert(null)).isEmpty();
        assertThat(converter.convert("")).isEmpty();
        assertThat(converter.convert("   ")).isEmpty();
    }
}
