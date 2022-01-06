/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.enumeration.DefaultUserRole;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;

@Component
public class UserManagementAuthoritiesPopulator {
    private final Logger logger = LoggerFactory.getLogger(UserManagementAuthoritiesPopulator.class);
    private final AuthenticationDescriptorKey authenticationDescriptorKey;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final UserAccessor userAccessor;

    @Autowired
    public UserManagementAuthoritiesPopulator(AuthenticationDescriptorKey authenticationDescriptorKey, ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor, UserAccessor userAccessor) {
        this.authenticationDescriptorKey = authenticationDescriptorKey;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.userAccessor = userAccessor;
    }

    public Set<GrantedAuthority> addAdditionalRoles(String userName, Set<GrantedAuthority> existingRoles) {
        Set<String> existingRoleNames = existingRoles.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        Set<String> alertRoles = addAdditionalRoleNames(userName, existingRoleNames, true);
        return alertRoles.stream()
                   .map(SimpleGrantedAuthority::new)
                   .collect(Collectors.toSet());

    }

    public Set<String> addAdditionalRoleNames(String userName, Set<String> existingRoles, boolean appendRolePrefix) {
        Set<String> rolesFromDB = getRolesFromDatabase(userName, appendRolePrefix);
        Set<String> roles = new LinkedHashSet<>(rolesFromDB);
        existingRoles
            .stream()
            .filter(StringUtils::isNotBlank)
            .forEach(roles::add);
        return roles;
    }

    public String getSAMLRoleAttributeName(String defaultName) {
        try {
            ConfigurationModel configurationModel = getCurrentConfiguration();
            return getFieldValue(configurationModel, AuthenticationDescriptor.KEY_SAML_ROLE_ATTRIBUTE_MAPPING).orElse(defaultName);
        } catch (AlertException ex) {
            logger.debug("Error getting SAML attribute name");
        }
        return defaultName;
    }

    private Set<String> getRolesFromDatabase(String userName, boolean appendRolePrefix) {
        Function<String, String> function = appendRolePrefix ? roleName -> UserModel.ROLE_PREFIX + roleName : Function.identity();
        Optional<UserModel> userModel = userAccessor.getUser(userName);
        Set<String> roleNames = userModel.map(UserModel::getRoleNames).orElse(Set.of(DefaultUserRole.ALERT_USER.name()));
        Set<String> newRoleNames = new LinkedHashSet<>(roleNames.size());
        for (String roleName : roleNames) {
            newRoleNames.add(function.apply(roleName));
        }
        return newRoleNames;
    }

    private ConfigurationModel getCurrentConfiguration() throws AlertException {
        return configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(authenticationDescriptorKey)
                   .stream()
                   .findFirst()
                   .orElseThrow(() -> new AlertException("Settings configuration missing"));
    }

    private Optional<String> getFieldValue(ConfigurationModel configurationModel, String fieldKey) {
        return configurationModel.getField(fieldKey).flatMap(ConfigurationFieldModel::getFieldValue);
    }

}
