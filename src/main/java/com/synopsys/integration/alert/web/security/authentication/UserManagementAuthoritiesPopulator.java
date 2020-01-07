/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.web.security.authentication;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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

import com.synopsys.integration.alert.common.enumeration.DefaultUserRole;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;

@Component
public class UserManagementAuthoritiesPopulator {
    private static final Logger logger = LoggerFactory.getLogger(UserManagementAuthoritiesPopulator.class);
    private AuthenticationDescriptorKey AuthenticationDescriptorKey;
    private ConfigurationAccessor configurationAccessor;
    private UserAccessor userAccessor;

    @Autowired
    public UserManagementAuthoritiesPopulator(AuthenticationDescriptorKey AuthenticationDescriptorKey, ConfigurationAccessor configurationAccessor, UserAccessor userAccessor) {
        this.AuthenticationDescriptorKey = AuthenticationDescriptorKey;
        this.configurationAccessor = configurationAccessor;
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
        // TODO remove in 6.0.0 the createRolesMapping method.
        Map<String, String> roleMap = createRolesMapping(appendRolePrefix);
        Set<String> rolesFromDB = getRolesFromDatabase(userName, appendRolePrefix);
        Set<String> roles = new LinkedHashSet<>();
        roles.addAll(rolesFromDB);
        roles.addAll(existingRoles.stream().filter(role -> StringUtils.isNotBlank(role)).collect(Collectors.toSet()));
        //TODO remove in 6.0.0 with the deprecated method
        if (!roleMap.isEmpty()) {
            roles.addAll(existingRoles.stream()
                             .filter(roleMap::containsKey)
                             .map(roleMap::get)
                             .collect(Collectors.toSet()));
        }

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

    // TODO remove in 6.0.0
    private Map<String, String> createRolesMapping(boolean appendRolePrefix) {
        Map<String, String> roleMapping = new HashMap<>(DefaultUserRole.values().length);
        try {
            ConfigurationModel configuration = getCurrentConfiguration();
            Function<DefaultUserRole, String> function = appendRolePrefix ? this::createRoleWithPrefix : DefaultUserRole::name;
            Optional<String> adminRoleMappingName = getFieldValue(configuration, AuthenticationDescriptor.KEY_ROLE_MAPPING_NAME_ADMIN);
            Optional<String> jobManagerMappingName = getFieldValue(configuration, AuthenticationDescriptor.KEY_ROLE_MAPPING_NAME_JOB_MANAGER);
            Optional<String> userMappingName = getFieldValue(configuration, AuthenticationDescriptor.KEY_ROLE_MAPPING_NAME_USER);
            adminRoleMappingName.ifPresent(roleName -> roleMapping.put(roleName, function.apply(DefaultUserRole.ALERT_ADMIN)));
            jobManagerMappingName.ifPresent(roleName -> roleMapping.put(roleName, function.apply(DefaultUserRole.ALERT_JOB_MANAGER)));
            userMappingName.ifPresent(roleName -> roleMapping.put(roleName, function.apply(DefaultUserRole.ALERT_USER)));
        } catch (AlertException ex) {
            logger.debug("Error mapping roles to alert roles.", ex);
        }
        return roleMapping;
    }

    private Set<String> getRolesFromDatabase(String userName, boolean appendRolePrefix) {
        Function<String, String> function = appendRolePrefix ? (roleName) -> UserModel.ROLE_PREFIX + roleName : Function.identity();
        Optional<UserModel> userModel = userAccessor.getUser(userName);
        Set<String> roleNames = userModel.map(UserModel::getRoleNames).orElse(Set.of());
        Set<String> newRoleNames = new LinkedHashSet<>(roleNames.size());
        for (String roleName : roleNames) {
            newRoleNames.add(function.apply(roleName));
        }
        return newRoleNames;
    }

    // TODO remove in 6.0.0
    private String createRoleWithPrefix(DefaultUserRole alertRole) {
        return UserModel.ROLE_PREFIX + alertRole.name();
    }

    // TODO remove in 6.0.0
    private ConfigurationModel getCurrentConfiguration() throws AlertException {
        return configurationAccessor.getConfigurationsByDescriptorKey(AuthenticationDescriptorKey)
                   .stream()
                   .findFirst()
                   .orElseThrow(() -> new AlertException("Settings configuration missing"));
    }

    // TODO remove in 6.0.0
    private Optional<String> getFieldValue(ConfigurationModel configurationModel, String fieldKey) {
        return configurationModel.getField(fieldKey).flatMap(ConfigurationFieldModel::getFieldValue);
    }

}
