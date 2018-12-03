package com.synopsys.integration.alert.database.api.user;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UserModel {
    private final String name;
    private final String password;
    private final List<GrantedAuthority> roles;

    public static final UserModel of(final String userName, final String password, final Collection<String> roles) {
        final List<GrantedAuthority> roleAuthorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        return new UserModel(userName, password, roleAuthorities);
    }

    public static final UserModel of(final String userName, final String password, final List<GrantedAuthority> roles) {
        return new UserModel(userName, password, roles);
    }

    private UserModel(final String name, final String password, final List<GrantedAuthority> roles) {
        this.name = name;
        this.password = password;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public List<GrantedAuthority> getRoles() {
        return roles;
    }

    public boolean hasRole(final String role) {
        return roles.stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals);
    }
}
