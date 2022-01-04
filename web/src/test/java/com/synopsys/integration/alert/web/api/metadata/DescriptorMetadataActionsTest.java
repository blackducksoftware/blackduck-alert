package com.synopsys.integration.alert.web.api.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.web.api.metadata.model.DescriptorsResponseModel;

public class DescriptorMetadataActionsTest {
    private final Set<Descriptor> descriptors = createComprehensiveSetOfDescriptors();
    private final DescriptorMetadataActions actions = createDescriptorController();

    @Test
    public void getDescriptorsWithoutPermissionTest() {
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.doReturn(true).when(authorizationManager).hasPermissions(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class));
        Mockito.doReturn(false).when(authorizationManager).hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class));

        DescriptorMetadataActions actions = new DescriptorMetadataActions(descriptors, authorizationManager);
        ActionResponse<DescriptorsResponseModel> response = actions.getDescriptorsByPermissions(null, null, null);
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        List<AccessOperation> allAccessOperations = response.getContent()
            .stream()
            .map(DescriptorsResponseModel::getDescriptors)
            .flatMap(Set::stream)
            .map(DescriptorMetadata::getOperations)
            .flatMap(Set::stream)
            .collect(Collectors.toList());
        assertEquals(0, allAccessOperations.size());
    }

    @Test
    public void getDescriptorsWithNoParametersTest() {
        ActionResponse<DescriptorsResponseModel> response = actions.getDescriptorsByPermissions(null, null, null);
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        Set<DescriptorMetadata> descriptorMetadata = response.getContent().get().getDescriptors();
        assertEquals(descriptors.size() * 2, descriptorMetadata.size());
    }

    @Test
    public void getDescriptorsWithInvalidParametersTest() {
        ActionResponse<DescriptorsResponseModel> response1 = actions.getDescriptorsByPermissions("x not a real name x", null, null);
        assertTrue(response1.isSuccessful());
        assertTrue(response1.hasContent());
        Set<DescriptorMetadata> descriptorMetadata1 = response1.getContent().get().getDescriptors();
        assertEquals(0, descriptorMetadata1.size());

        ActionResponse<DescriptorsResponseModel> response2 = actions.getDescriptorsByPermissions(null, "x not a real type x", null);
        assertTrue(response2.isSuccessful());
        assertTrue(response2.hasContent());
        Set<DescriptorMetadata> descriptorMetadata2 = response2.getContent().get().getDescriptors();
        assertEquals(0, descriptorMetadata2.size());

        ActionResponse<DescriptorsResponseModel> response3 = actions.getDescriptorsByPermissions(null, null, "x not a real context x");
        assertTrue(response3.isSuccessful());
        assertTrue(response3.hasContent());
        Set<DescriptorMetadata> descriptorMetadata3 = response3.getContent().get().getDescriptors();
        assertEquals(0, descriptorMetadata3.size());
    }

    @Test
    public void getDescriptorsWithNameOnlyTest() {
        String channelName = getNamePrefix(DescriptorType.CHANNEL) + "_2";
        ActionResponse<DescriptorsResponseModel> response1 = actions.getDescriptorsByPermissions(channelName, null, null);
        assertTrue(response1.isSuccessful());
        assertTrue(response1.hasContent());
        Set<DescriptorMetadata> descriptorMetadata1 = response1.getContent().get().getDescriptors();
        assertEquals(2, descriptorMetadata1.size());

        String componentName = getNamePrefix(DescriptorType.COMPONENT) + "_2";
        ActionResponse<DescriptorsResponseModel> response2 = actions.getDescriptorsByPermissions(componentName, null, null);
        assertTrue(response2.isSuccessful());
        assertTrue(response2.hasContent());
        Set<DescriptorMetadata> descriptorMetadata2 = response2.getContent().get().getDescriptors();
        assertEquals(2, descriptorMetadata2.size());

        String providerName = getNamePrefix(DescriptorType.PROVIDER) + "_2";
        ActionResponse<DescriptorsResponseModel> response3 = actions.getDescriptorsByPermissions(providerName, null, null);
        assertTrue(response3.isSuccessful());
        assertTrue(response3.hasContent());
        Set<DescriptorMetadata> descriptorMetadata3 = response3.getContent().get().getDescriptors();
        assertEquals(2, descriptorMetadata3.size());
    }

    @Test
    public void getDescriptorsWithTypeOnlyTest() {
        ActionResponse<DescriptorsResponseModel> response1 = actions.getDescriptorsByPermissions(null, DescriptorType.CHANNEL.name(), null);
        assertTrue(response1.isSuccessful());
        assertTrue(response1.hasContent());
        Set<DescriptorMetadata> descriptorMetadata1 = response1.getContent().get().getDescriptors();
        assertEquals((descriptors.size() / 3) * 2, descriptorMetadata1.size());

        ActionResponse<DescriptorsResponseModel> response2 = actions.getDescriptorsByPermissions(null, DescriptorType.COMPONENT.name(), null);
        assertTrue(response2.isSuccessful());
        assertTrue(response2.hasContent());
        Set<DescriptorMetadata> descriptorMetadata2 = response2.getContent().get().getDescriptors();
        assertEquals((descriptors.size() / 3) * 2, descriptorMetadata2.size());

        ActionResponse<DescriptorsResponseModel> response3 = actions.getDescriptorsByPermissions(null, DescriptorType.PROVIDER.name(), null);
        assertTrue(response3.isSuccessful());
        assertTrue(response3.hasContent());
        Set<DescriptorMetadata> descriptorMetadata3 = response3.getContent().get().getDescriptors();
        assertEquals((descriptors.size() / 3) * 2, descriptorMetadata3.size());
    }

    @Test
    public void getDescriptorsWithContextOnlyTest() {
        ActionResponse<DescriptorsResponseModel> response1 = actions.getDescriptorsByPermissions(null, null, ConfigContextEnum.GLOBAL.name());
        assertTrue(response1.isSuccessful());
        assertTrue(response1.hasContent());
        Set<DescriptorMetadata> descriptorMetadata1 = response1.getContent().get().getDescriptors();
        assertEquals(descriptors.size(), descriptorMetadata1.size());

        ActionResponse<DescriptorsResponseModel> response2 = actions.getDescriptorsByPermissions(null, null, ConfigContextEnum.DISTRIBUTION.name());
        assertTrue(response2.isSuccessful());
        assertTrue(response2.hasContent());
        Set<DescriptorMetadata> descriptorMetadata2 = response2.getContent().get().getDescriptors();
        assertEquals(descriptors.size(), descriptorMetadata2.size());
    }

    @Test
    public void getDescriptorsWithTypeAndContextTest() {
        DescriptorType type1 = DescriptorType.CHANNEL;
        ConfigContextEnum context1 = ConfigContextEnum.GLOBAL;
        ActionResponse<DescriptorsResponseModel> response1 = actions.getDescriptorsByPermissions(null, type1.name(), context1.name());
        int expectedSize = descriptors.size() / (DescriptorType.values().length);
        assertTrue(response1.isSuccessful());
        assertTrue(response1.hasContent());
        Set<DescriptorMetadata> descriptorMetadata1 = response1.getContent().get().getDescriptors();
        assertEquals(expectedSize, descriptorMetadata1.size());

        DescriptorType type2 = DescriptorType.COMPONENT;
        ConfigContextEnum context2 = ConfigContextEnum.GLOBAL;
        ActionResponse<DescriptorsResponseModel> response2 = actions.getDescriptorsByPermissions(null, type2.name(), context2.name());
        assertTrue(response2.isSuccessful());
        assertTrue(response2.hasContent());
        Set<DescriptorMetadata> descriptorMetadata2 = response2.getContent().get().getDescriptors();
        assertEquals(expectedSize, descriptorMetadata2.size());

        DescriptorType type3 = DescriptorType.PROVIDER;
        ConfigContextEnum context3 = ConfigContextEnum.GLOBAL;
        ActionResponse<DescriptorsResponseModel> response3 = actions.getDescriptorsByPermissions(null, type3.name(), context3.name());
        assertTrue(response3.isSuccessful());
        assertTrue(response3.hasContent());
        Set<DescriptorMetadata> descriptorMetadata3 = response3.getContent().get().getDescriptors();
        assertEquals(expectedSize, descriptorMetadata3.size());

        DescriptorType type4 = DescriptorType.CHANNEL;
        ConfigContextEnum context4 = ConfigContextEnum.DISTRIBUTION;
        ActionResponse<DescriptorsResponseModel> response4 = actions.getDescriptorsByPermissions(null, type4.name(), context4.name());
        assertTrue(response4.isSuccessful());
        assertTrue(response4.hasContent());
        Set<DescriptorMetadata> descriptorMetadata4 = response4.getContent().get().getDescriptors();
        assertEquals(expectedSize, descriptorMetadata4.size());

        DescriptorType type5 = DescriptorType.COMPONENT;
        ConfigContextEnum context5 = ConfigContextEnum.DISTRIBUTION;
        ActionResponse<DescriptorsResponseModel> response5 = actions.getDescriptorsByPermissions(null, type5.name(), context5.name());
        assertTrue(response5.isSuccessful());
        assertTrue(response5.hasContent());
        Set<DescriptorMetadata> descriptorMetadata5 = response5.getContent().get().getDescriptors();
        assertEquals(expectedSize, descriptorMetadata5.size());

        DescriptorType type6 = DescriptorType.PROVIDER;
        ConfigContextEnum context6 = ConfigContextEnum.DISTRIBUTION;
        ActionResponse<DescriptorsResponseModel> response6 = actions.getDescriptorsByPermissions(null, type6.name(), context6.name());
        assertTrue(response6.isSuccessful());
        assertTrue(response6.hasContent());
        Set<DescriptorMetadata> descriptorMetadata6 = response6.getContent().get().getDescriptors();
        assertEquals(expectedSize, descriptorMetadata6.size());
    }

    @Test
    public void getDescriptorsWithNameAndContextTest() {
        String componentName1 = getNamePrefix(DescriptorType.CHANNEL) + "_4";
        ConfigContextEnum context1 = ConfigContextEnum.GLOBAL;
        ActionResponse<DescriptorsResponseModel> response1 = actions.getDescriptorsByPermissions(componentName1, null, context1.name());
        assertTrue(response1.isSuccessful());
        assertTrue(response1.hasContent());
        Set<DescriptorMetadata> descriptorMetadata1 = response1.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata1.size());

        String componentName2 = getNamePrefix(DescriptorType.COMPONENT) + "_4";
        ConfigContextEnum context2 = ConfigContextEnum.GLOBAL;
        ActionResponse<DescriptorsResponseModel> response2 = actions.getDescriptorsByPermissions(componentName2, null, context2.name());
        assertTrue(response2.isSuccessful());
        assertTrue(response2.hasContent());
        Set<DescriptorMetadata> descriptorMetadata2 = response2.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata2.size());

        String componentName3 = getNamePrefix(DescriptorType.PROVIDER) + "_4";
        ConfigContextEnum context3 = ConfigContextEnum.GLOBAL;
        ActionResponse<DescriptorsResponseModel> response3 = actions.getDescriptorsByPermissions(componentName3, null, context3.name());
        assertTrue(response3.isSuccessful());
        assertTrue(response3.hasContent());
        Set<DescriptorMetadata> descriptorMetadata3 = response3.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata3.size());

        ConfigContextEnum context4 = ConfigContextEnum.DISTRIBUTION;
        ActionResponse<DescriptorsResponseModel> response4 = actions.getDescriptorsByPermissions(componentName1, null, context4.name());
        assertTrue(response4.isSuccessful());
        assertTrue(response4.hasContent());
        Set<DescriptorMetadata> descriptorMetadata4 = response4.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata4.size());

        ConfigContextEnum context5 = ConfigContextEnum.DISTRIBUTION;
        ActionResponse<DescriptorsResponseModel> response5 = actions.getDescriptorsByPermissions(componentName2, null, context5.name());
        assertTrue(response5.isSuccessful());
        assertTrue(response5.hasContent());
        Set<DescriptorMetadata> descriptorMetadata5 = response5.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata5.size());

        ConfigContextEnum context6 = ConfigContextEnum.DISTRIBUTION;
        ActionResponse<DescriptorsResponseModel> response6 = actions.getDescriptorsByPermissions(componentName3, null, context6.name());
        assertTrue(response6.isSuccessful());
        assertTrue(response6.hasContent());
        Set<DescriptorMetadata> descriptorMetadata6 = response6.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata6.size());
    }

    @Test
    public void getDescriptorsWithAllParametersTest() {
        DescriptorType type1 = DescriptorType.CHANNEL;
        String componentName1 = getNamePrefix(type1) + "_2";
        ConfigContextEnum context1 = ConfigContextEnum.GLOBAL;
        ActionResponse<DescriptorsResponseModel> response1 = actions.getDescriptorsByPermissions(componentName1, type1.name(), context1.name());
        assertTrue(response1.isSuccessful());
        assertTrue(response1.hasContent());
        Set<DescriptorMetadata> descriptorMetadata1 = response1.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata1.size());
    }

    private DescriptorMetadataActions createDescriptorController() {
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.doReturn(true).when(authorizationManager).isReadOnly(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class));
        Mockito.doReturn(true).when(authorizationManager).hasPermissions(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class));
        Mockito.doReturn(true).when(authorizationManager).hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class));
        Mockito.doReturn(true).when(authorizationManager).hasDeletePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class));
        Mockito.doReturn(true).when(authorizationManager).hasWritePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class));
        Mockito.doReturn(true).when(authorizationManager).hasCreatePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class));
        Mockito.doReturn(true).when(authorizationManager).hasExecutePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class));

        Mockito.doReturn(true).when(authorizationManager).hasReadPermission(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(true).when(authorizationManager).hasDeletePermission(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(true).when(authorizationManager).hasWritePermission(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(true).when(authorizationManager).hasCreatePermission(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(true).when(authorizationManager).hasExecutePermission(Mockito.anyString(), Mockito.anyString());
        DescriptorMetadataActions actions = new DescriptorMetadataActions(descriptors, authorizationManager);
        return actions;
    }

    private Set<Descriptor> createComprehensiveSetOfDescriptors() {
        Set<Descriptor> descriptors = new HashSet<>();
        descriptors.addAll(createDescriptorsOfType(DescriptorType.PROVIDER));
        descriptors.addAll(createDescriptorsOfType(DescriptorType.CHANNEL));
        descriptors.addAll(createDescriptorsOfType(DescriptorType.COMPONENT));
        return descriptors;
    }

    private Set<Descriptor> createDescriptorsOfType(DescriptorType descriptorType) {
        String namePrefix = getNamePrefix(descriptorType);
        Descriptor d1 = new TestDescriptor(namePrefix + "_1", descriptorType);
        Descriptor d2 = new TestDescriptor(namePrefix + "_2", descriptorType, ConfigContextEnum.GLOBAL);
        Descriptor d3 = new TestDescriptor(namePrefix + "_3", descriptorType, ConfigContextEnum.DISTRIBUTION);
        Descriptor d4 = new TestDescriptor(namePrefix + "_4", descriptorType, ConfigContextEnum.GLOBAL, ConfigContextEnum.DISTRIBUTION);

        return Set.of(d1, d2, d3, d4);
    }

    private String getNamePrefix(DescriptorType descriptorType) {
        return descriptorType.name().toLowerCase();
    }

    private class TestDescriptor extends Descriptor {
        private final List<ConfigContextEnum> contexts;

        public TestDescriptor(String descriptorName, DescriptorType type, ConfigContextEnum... contexts) {
            super(new DescriptorKey(descriptorName, descriptorName) {}, type, null != contexts ? Set.of(contexts) : Set.of());
            if (contexts != null) {
                this.contexts = Arrays.asList(contexts);
            } else {
                this.contexts = List.of();
            }
        }

        @Override
        public boolean hasConfigForType(ConfigContextEnum actionApiType) {
            return contexts.contains(actionApiType);
        }

        @Override
        public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
            return Optional.empty();
        }

        @Override
        public Optional<DistributionConfigurationValidator> getDistributionValidator() {
            return Optional.empty();
        }

    }

}
