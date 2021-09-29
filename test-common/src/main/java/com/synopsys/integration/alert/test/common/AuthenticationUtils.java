package com.synopsys.integration.alert.test.common;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.security.UserPrincipal;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

public class AuthenticationUtils {
    private AtomicLong userIdGenerator = new AtomicLong(0);

    public Authentication createUser(RoleAccessor roleAccessor, String userName, String roleName) throws AlertConfigurationException {
        Set<UserRoleModel> roles = Set.of();
        if (roleAccessor.doesRoleNameExist(roleName)) {
            roles = roleAccessor.getRoles().stream()
                .filter(role -> role.getName().equals(roleName))
                .collect(Collectors.toSet());
        }
        return createAuthentication(userIdGenerator.incrementAndGet(), userName, roles);
    }

    public Authentication createAuthentication(Long id, String username, Set<UserRoleModel> roles) {
        UserModel userModel = UserModel.existingUser(id, username, "", "", AuthenticationType.DATABASE, roles, true);
        UserPrincipal userPrincipal = new UserPrincipal(userModel);
        return new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
    }

    public AuthorizationManager createAuthorizationManagerWithRole(String roleName, Supplier<PermissionMatrixModel> permissionSupplier) {
        RoleAccessor roleAccessor = createTestRoleAccessor();
        roleAccessor.createRoleWithPermissions(roleName, permissionSupplier.get());
        return new AuthorizationManager(roleAccessor);
    }

    public RoleAccessor createTestRoleAccessor() {
        return new MockRoleAccessor();
    }

    public void updateCurrentUser(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
