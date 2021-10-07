package com.synopsys.integration.alert.component.authentication.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;

public class UserManagementAuthoritiesPopulatorTest {
    private static final String TEST_USERNAME = "testUserName";
    private final AuthenticationDescriptorKey descriptorKey = new AuthenticationDescriptorKey();
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
    private final ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
    private final ConfigurationFieldModel roleMappingField = Mockito.mock(ConfigurationFieldModel.class);
    private final ConfigurationFieldModel samlAttributeMappingField = Mockito.mock(ConfigurationFieldModel.class);
    private final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);

    @Test
    public void testSAMLAttributeName() {
        String attributeName = "SAML_ATTRIBUTE_NAME";
        Mockito.when(samlAttributeMappingField.getFieldValue()).thenReturn(Optional.of(attributeName));
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(Mockito.eq(descriptorKey))).thenReturn(List.of(configurationModel));
        Mockito.when(configurationModel.getField(Mockito.eq(AuthenticationDescriptor.KEY_SAML_ROLE_ATTRIBUTE_MAPPING))).thenReturn(Optional.of(samlAttributeMappingField));
        Mockito.when(userAccessor.getUser(Mockito.anyString())).thenReturn(Optional.empty());
        UserManagementAuthoritiesPopulator authoritiesPopulator = new UserManagementAuthoritiesPopulator(descriptorKey, configurationModelConfigurationAccessor, userAccessor);
        assertEquals(attributeName, authoritiesPopulator.getSAMLRoleAttributeName("DEFAULT_ATTRIBUTE"));
    }

    @Test
    public void testSAMLAttributeNameNotFound() {
        String attributeName = "DEFAULT_ATTRIBUTE";
        Mockito.when(samlAttributeMappingField.getFieldValue()).thenReturn(Optional.empty());
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(Mockito.eq(descriptorKey))).thenReturn(List.of(configurationModel));
        Mockito.when(configurationModel.getField(Mockito.eq(AuthenticationDescriptor.KEY_SAML_ROLE_ATTRIBUTE_MAPPING))).thenReturn(Optional.of(samlAttributeMappingField));
        Mockito.when(userAccessor.getUser(Mockito.anyString())).thenReturn(Optional.empty());
        UserManagementAuthoritiesPopulator authoritiesPopulator = new UserManagementAuthoritiesPopulator(descriptorKey, configurationModelConfigurationAccessor, userAccessor);
        assertEquals(attributeName, authoritiesPopulator.getSAMLRoleAttributeName(attributeName));
    }

    @Test
    public void testSAMLAttributeConfigurationNotFound() {
        String attributeName = "DEFAULT_ATTRIBUTE";
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(Mockito.eq(descriptorKey))).thenReturn(List.of());
        Mockito.when(userAccessor.getUser(Mockito.anyString())).thenReturn(Optional.empty());
        UserManagementAuthoritiesPopulator authoritiesPopulator = new UserManagementAuthoritiesPopulator(descriptorKey, configurationModelConfigurationAccessor, userAccessor);
        assertEquals(attributeName, authoritiesPopulator.getSAMLRoleAttributeName(attributeName));
    }

}
