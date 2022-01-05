/*
 * test-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.test.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.security.UserPrincipal;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

/**
 * This class maintains an internal RoleAccessor to store roles and permissions.
 * Add all the role and user data using the add methods.
 * When ready call the #createAuthorizationManagerWithCurrentUserSet method to obtain an AuthorizationManager initialized with the role data.
 * @see AuthorizationManager
 * @see RoleAccessor
 * @see MockRoleAccessor
 * @see UserPrincipal
 * @see UserRoleModel
 * @see SecurityContextHolder
 */
public class AuthenticationTestUtils {
    public static final int FULL_PERMISSIONS = 255;
    public static final int NO_PERMISSIONS = 0;

    private AtomicLong userIdGenerator = new AtomicLong(0);
    private Map<String, Set<String>> userToRoleNamesMap = new HashMap<>();
    private Map<String, Authentication> userAuthMap = new HashMap<>();
    private Map<String, Supplier<PermissionMatrixModel>> roleToPermissionsMap = new HashMap<>();
    private RoleAccessor roleAccessor = new MockRoleAccessor();

    public void addUserWithRole(String userName, String... roleNames) {
        Set<String> roleNameSet = Arrays.asList(roleNames).stream()
            .collect(Collectors.toSet());
        this.addUserWithRole(userName, roleNameSet);
    }

    public void addUserWithRole(String userName, Set<String> roleNames) {
        Set<String> existingRoles = userToRoleNamesMap.computeIfAbsent(userName, (ignored) -> new HashSet<>());
        existingRoles.addAll(roleNames);
    }

    public void addRoleWithPermissions(String roleName, Supplier<PermissionMatrixModel> permissions) {
        roleToPermissionsMap.put(roleName, permissions);
    }

    public AuthorizationManager createAuthorizationManagerWithCurrentUserSet(String currentUserName, String roleName, Supplier<PermissionMatrixModel> permissions) {
        addRoleWithPermissions(roleName, permissions);
        addUserWithRole(currentUserName, roleName);
        return createAuthorizationManagerWithCurrentUserSet(currentUserName);
    }

    public AuthorizationManager createAuthorizationManagerWithCurrentUserSet(String currentUserName) {
        AuthorizationManager authorizationManager = createAuthorizationManager();
        Authentication newCurrentUser = getUserAuthentication(currentUserName).orElseThrow(() -> new IllegalStateException(String.format("User: %s was never added. Cannot assign as the currrent user.", currentUserName)));
        updateCurrentUser(newCurrentUser);
        return authorizationManager;
    }

    public AuthorizationManager createAuthorizationManager() {
        // generate role data
        for (Map.Entry<String, Supplier<PermissionMatrixModel>> roleEntry : roleToPermissionsMap.entrySet()) {
            roleAccessor.createRoleWithPermissions(roleEntry.getKey(), roleEntry.getValue().get());
        }
        createUserAuthentications();

        return new AuthorizationManager(roleAccessor);
    }

    public int createUserAuthentications() {
        int count = userToRoleNamesMap.size();
        // generate users
        for (Map.Entry<String, Set<String>> userEntry : userToRoleNamesMap.entrySet()) {
            String userName = userEntry.getKey();
            Authentication authentication = createUserWithRole(userName, userEntry.getValue());
            userAuthMap.put(userName, authentication);
        }
        return count;
    }

    private Authentication createUserWithRole(String userName, Set<String> roleNames) {
        Set<UserRoleModel> roles = new HashSet<>();
        for (String roleName : roleNames) {
            if (roleAccessor.doesRoleNameExist(roleName)) {
                roles = roleAccessor.getRoles().stream()
                    .filter(role -> role.getName().equals(roleName))
                    .collect(Collectors.toSet());
            }
        }
        return createAuthentication(userIdGenerator.incrementAndGet(), userName, roles);
    }

    public Authentication createAuthentication(Long id, String username, Set<UserRoleModel> roles) {
        UserModel userModel = UserModel.existingUser(id, username, "", "", AuthenticationType.DATABASE, roles, true);
        UserPrincipal userPrincipal = new UserPrincipal(userModel);
        return new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
    }

    public Optional<Authentication> getUserAuthentication(String userName) {
        return Optional.ofNullable(userAuthMap.get(userName));
    }

    public void updateCurrentUser(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
