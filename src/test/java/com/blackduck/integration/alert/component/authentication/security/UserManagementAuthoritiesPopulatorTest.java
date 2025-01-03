/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.authentication.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.blackduck.integration.alert.api.authentication.security.UserManagementAuthoritiesPopulator;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.UserAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;

class UserManagementAuthoritiesPopulatorTest {
    private final AuthenticationDescriptorKey descriptorKey = new AuthenticationDescriptorKey();
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
    private final ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
    private final ConfigurationFieldModel samlAttributeMappingField = Mockito.mock(ConfigurationFieldModel.class);
    private final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);

    @Test
    void testSAMLAttributeName() {
        String attributeName = "SAML_ATTRIBUTE_NAME";
        Mockito.when(samlAttributeMappingField.getFieldValue()).thenReturn(Optional.of(attributeName));
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(descriptorKey)).thenReturn(List.of(configurationModel));
        Mockito.when(configurationModel.getField(AuthenticationDescriptor.KEY_SAML_ROLE_ATTRIBUTE_MAPPING)).thenReturn(Optional.of(samlAttributeMappingField));
        Mockito.when(userAccessor.getUser(Mockito.anyString())).thenReturn(Optional.empty());
        UserManagementAuthoritiesPopulator authoritiesPopulator = new UserManagementAuthoritiesPopulator(descriptorKey, configurationModelConfigurationAccessor, userAccessor);
        assertEquals(attributeName, authoritiesPopulator.getSAMLRoleAttributeName("DEFAULT_ATTRIBUTE"));
    }

    @Test
    void testSAMLAttributeNameNotFound() {
        String attributeName = "DEFAULT_ATTRIBUTE";
        Mockito.when(samlAttributeMappingField.getFieldValue()).thenReturn(Optional.empty());
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(descriptorKey)).thenReturn(List.of(configurationModel));
        Mockito.when(configurationModel.getField(AuthenticationDescriptor.KEY_SAML_ROLE_ATTRIBUTE_MAPPING)).thenReturn(Optional.of(samlAttributeMappingField));
        Mockito.when(userAccessor.getUser(Mockito.anyString())).thenReturn(Optional.empty());
        UserManagementAuthoritiesPopulator authoritiesPopulator = new UserManagementAuthoritiesPopulator(descriptorKey, configurationModelConfigurationAccessor, userAccessor);
        assertEquals(attributeName, authoritiesPopulator.getSAMLRoleAttributeName(attributeName));
    }

    @Test
    void testSAMLAttributeConfigurationNotFound() {
        String attributeName = "DEFAULT_ATTRIBUTE";
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(descriptorKey)).thenReturn(List.of());
        Mockito.when(userAccessor.getUser(Mockito.anyString())).thenReturn(Optional.empty());
        UserManagementAuthoritiesPopulator authoritiesPopulator = new UserManagementAuthoritiesPopulator(descriptorKey, configurationModelConfigurationAccessor, userAccessor);
        assertEquals(attributeName, authoritiesPopulator.getSAMLRoleAttributeName(attributeName));
    }

}
