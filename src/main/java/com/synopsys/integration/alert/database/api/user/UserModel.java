package com.synopsys.integration.alert.database.api.user;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserModel {
    private final String name;
    private final String password;
    private final EnumSet<UserRole> roles;

    public static final UserModel of(final String userName, final String password, final List<String> roles) {
        final EnumSet<UserRole> roleSet = EnumSet.copyOf(roles.stream().map(UserRole::valueOf).collect(Collectors.toList()));
        return new UserModel(userName, password, roleSet);
    }

    private UserModel(final String name, final String password, final EnumSet<UserRole> roles) {
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

    public EnumSet<UserRole> getRoles() {
        return roles;
    }

    public boolean hasRole(final String role) {
        final Optional<UserRole> parsedRole = Optional.ofNullable(UserRole.valueOf(role.toUpperCase()));
        return parsedRole.isPresent() && hasRole(parsedRole.get());
    }

    public boolean hasRole(final UserRole role) {
        return roles.contains(role);
    }
}
