package com.synopsys.integration.alert.component.authentication.security.saml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.api.authentication.security.UserManagementAuthoritiesPopulator;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.security.UserPrincipal;

class UserDetailsServiceTest {

    private static final String USER_NAME = "user_name";
    private static final String EMAIL = "email_address";
    private static final String[] VALID_ROLES = { "ALERT_ADMIN" };
    private static final String[] VALID_DB_ROLES = { "ALERT_USER" };
    private UserManagementAuthoritiesPopulator authoritiesPopulator;

    @BeforeEach
    public void initializeAuthoritiesPopulator() {
        Set<UserRoleModel> roles = Arrays.stream(VALID_DB_ROLES)
                                       .map(UserRoleModel::of)
                                       .collect(Collectors.toSet());
        UserModel userModel = UserModel.newUser(USER_NAME, "password", EMAIL, AuthenticationType.SAML, roles, true);
        AuthenticationDescriptorKey key = new AuthenticationDescriptorKey();
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        ConfigurationModel configuration = Mockito.mock(ConfigurationModel.class);
        UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        Mockito.when(configuration.getField(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(key)).thenReturn(List.of(configuration));
        Mockito.when(userAccessor.getUser(Mockito.anyString())).thenReturn(Optional.of(userModel));
        authoritiesPopulator = new UserManagementAuthoritiesPopulator(key, configurationModelConfigurationAccessor, userAccessor);
    }

    private List<String> extractRoleNamesFromPrincipal(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                   .map(GrantedAuthority::getAuthority)
                   .map(authority -> StringUtils.remove(authority, UserModel.ROLE_PREFIX))
                   .collect(Collectors.toList());
    }
}
