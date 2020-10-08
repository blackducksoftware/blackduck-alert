package com.synopsys.integration.alert.web.api.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.web.api.metadata.model.DescriptorsResponseModel;

public class DescriptorMetadataActionsTest {
    private final Set<Descriptor> descriptors = createComprehensiveSetOfDescriptors();
    private final DescriptorMetadataActions actions = createDescriptorController();

    @Test
    public void getDescriptorsWithoutPermissionTest() {
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.doReturn(true).when(authorizationManager).hasPermissions(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(false).when(authorizationManager).hasReadPermission(Mockito.anyString(), Mockito.anyString());

        DescriptorMetadataActions actions = new DescriptorMetadataActions(descriptors, authorizationManager);
        ActionResponse<DescriptorsResponseModel> response = actions.getDescriptorsByPermissions(null, null, null);
        Assertions.assertTrue(response.isSuccessful());
        Assertions.assertTrue(response.hasContent());
        Set<DescriptorMetadata> descriptorMetadata = response.getContent().get().getDescriptors();
        assertEquals(0, descriptorMetadata.size());
    }

    @Test
    public void getDescriptorsWithNoParametersTest() {
        ActionResponse<DescriptorsResponseModel> response = actions.getDescriptorsByPermissions(null, null, null);
        Assertions.assertTrue(response.isSuccessful());
        Assertions.assertTrue(response.hasContent());
        Set<DescriptorMetadata> descriptorMetadata = response.getContent().get().getDescriptors();
        assertEquals(descriptors.size(), descriptorMetadata.size());
    }

    @Test
    public void getDescriptorsWithInvalidParametersTest() {
        ActionResponse<DescriptorsResponseModel> response1 = actions.getDescriptorsByPermissions("x not a real name x", null, null);
        Assertions.assertTrue(response1.isSuccessful());
        Assertions.assertTrue(response1.hasContent());
        Set<DescriptorMetadata> descriptorMetadata1 = response1.getContent().get().getDescriptors();
        assertEquals(0, descriptorMetadata1.size());

        ActionResponse<DescriptorsResponseModel> response2 = actions.getDescriptorsByPermissions(null, "x not a real type x", null);
        Assertions.assertTrue(response2.isSuccessful());
        Assertions.assertTrue(response2.hasContent());
        Set<DescriptorMetadata> descriptorMetadata2 = response2.getContent().get().getDescriptors();
        assertEquals(0, descriptorMetadata2.size());

        ActionResponse<DescriptorsResponseModel> response3 = actions.getDescriptorsByPermissions(null, null, "x not a real context x");
        Assertions.assertTrue(response3.isSuccessful());
        Assertions.assertTrue(response3.hasContent());
        Set<DescriptorMetadata> descriptorMetadata3 = response3.getContent().get().getDescriptors();
        assertEquals(0, descriptorMetadata3.size());
    }

    @Test
    public void getDescriptorsWithNameOnlyTest() {
        String componentName = getNamePrefix(DescriptorType.CHANNEL) + "_2";
        ActionResponse<DescriptorsResponseModel> response1 = actions.getDescriptorsByPermissions(componentName, null, null);
        Assertions.assertTrue(response1.isSuccessful());
        Assertions.assertTrue(response1.hasContent());
        Set<DescriptorMetadata> descriptorMetadata1 = response1.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata1.size());

        String channelName = getNamePrefix(DescriptorType.COMPONENT) + "_2";
        ActionResponse<DescriptorsResponseModel> response2 = actions.getDescriptorsByPermissions(channelName, null, null);
        Assertions.assertTrue(response2.isSuccessful());
        Assertions.assertTrue(response2.hasContent());
        Set<DescriptorMetadata> descriptorMetadata2 = response2.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata2.size());

        String providerName = getNamePrefix(DescriptorType.PROVIDER) + "_2";
        ActionResponse<DescriptorsResponseModel> response3 = actions.getDescriptorsByPermissions(providerName, null, null);
        Assertions.assertTrue(response3.isSuccessful());
        Assertions.assertTrue(response3.hasContent());
        Set<DescriptorMetadata> descriptorMetadata3 = response3.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata3.size());
    }

    @Test
    public void getDescriptorsWithTypeOnlyTest() {
        ActionResponse<DescriptorsResponseModel> response1 = actions.getDescriptorsByPermissions(null, DescriptorType.CHANNEL.name(), null);
        Assertions.assertTrue(response1.isSuccessful());
        Assertions.assertTrue(response1.hasContent());
        Set<DescriptorMetadata> descriptorMetadata1 = response1.getContent().get().getDescriptors();
        assertEquals(descriptors.size() / 3, descriptorMetadata1.size());

        ActionResponse<DescriptorsResponseModel> response2 = actions.getDescriptorsByPermissions(null, DescriptorType.COMPONENT.name(), null);
        Assertions.assertTrue(response2.isSuccessful());
        Assertions.assertTrue(response2.hasContent());
        Set<DescriptorMetadata> descriptorMetadata2 = response2.getContent().get().getDescriptors();
        assertEquals(descriptors.size() / 3, descriptorMetadata2.size());

        ActionResponse<DescriptorsResponseModel> response3 = actions.getDescriptorsByPermissions(null, DescriptorType.PROVIDER.name(), null);
        Assertions.assertTrue(response3.isSuccessful());
        Assertions.assertTrue(response3.hasContent());
        Set<DescriptorMetadata> descriptorMetadata3 = response3.getContent().get().getDescriptors();
        assertEquals(descriptors.size() / 3, descriptorMetadata3.size());
    }

    @Test
    public void getDescriptorsWithContextOnlyTest() {
        ActionResponse<DescriptorsResponseModel> response1 = actions.getDescriptorsByPermissions(null, null, ConfigContextEnum.GLOBAL.name());
        Assertions.assertTrue(response1.isSuccessful());
        Assertions.assertTrue(response1.hasContent());
        Set<DescriptorMetadata> descriptorMetadata1 = response1.getContent().get().getDescriptors();
        assertEquals(descriptors.size() / 2, descriptorMetadata1.size());

        ActionResponse<DescriptorsResponseModel> response2 = actions.getDescriptorsByPermissions(null, null, ConfigContextEnum.DISTRIBUTION.name());
        Assertions.assertTrue(response2.isSuccessful());
        Assertions.assertTrue(response2.hasContent());
        Set<DescriptorMetadata> descriptorMetadata2 = response2.getContent().get().getDescriptors();
        assertEquals(descriptors.size() / 2, descriptorMetadata2.size());
    }

    @Test
    public void getDescriptorsWithTypeAndContextTest() {
        DescriptorType type1 = DescriptorType.CHANNEL;
        ConfigContextEnum context1 = ConfigContextEnum.GLOBAL;
        ActionResponse<DescriptorsResponseModel> response1 = actions.getDescriptorsByPermissions(null, type1.name(), context1.name());
        int expectedSize = descriptors.size() / (DescriptorType.values().length * ConfigContextEnum.values().length);
        Assertions.assertTrue(response1.isSuccessful());
        Assertions.assertTrue(response1.hasContent());
        Set<DescriptorMetadata> descriptorMetadata1 = response1.getContent().get().getDescriptors();
        assertEquals(expectedSize, descriptorMetadata1.size());

        DescriptorType type2 = DescriptorType.COMPONENT;
        ConfigContextEnum context2 = ConfigContextEnum.GLOBAL;
        ActionResponse<DescriptorsResponseModel> response2 = actions.getDescriptorsByPermissions(null, type2.name(), context2.name());
        Assertions.assertTrue(response2.isSuccessful());
        Assertions.assertTrue(response2.hasContent());
        Set<DescriptorMetadata> descriptorMetadata2 = response2.getContent().get().getDescriptors();
        assertEquals(expectedSize, descriptorMetadata2.size());

        DescriptorType type3 = DescriptorType.PROVIDER;
        ConfigContextEnum context3 = ConfigContextEnum.GLOBAL;
        ActionResponse<DescriptorsResponseModel> response3 = actions.getDescriptorsByPermissions(null, type3.name(), context3.name());
        Assertions.assertTrue(response3.isSuccessful());
        Assertions.assertTrue(response3.hasContent());
        Set<DescriptorMetadata> descriptorMetadata3 = response3.getContent().get().getDescriptors();
        assertEquals(expectedSize, descriptorMetadata3.size());

        DescriptorType type4 = DescriptorType.CHANNEL;
        ConfigContextEnum context4 = ConfigContextEnum.DISTRIBUTION;
        ActionResponse<DescriptorsResponseModel> response4 = actions.getDescriptorsByPermissions(null, type4.name(), context4.name());
        Assertions.assertTrue(response4.isSuccessful());
        Assertions.assertTrue(response4.hasContent());
        Set<DescriptorMetadata> descriptorMetadata4 = response4.getContent().get().getDescriptors();
        assertEquals(expectedSize, descriptorMetadata4.size());

        DescriptorType type5 = DescriptorType.COMPONENT;
        ConfigContextEnum context5 = ConfigContextEnum.DISTRIBUTION;
        ActionResponse<DescriptorsResponseModel> response5 = actions.getDescriptorsByPermissions(null, type5.name(), context5.name());
        Assertions.assertTrue(response5.isSuccessful());
        Assertions.assertTrue(response5.hasContent());
        Set<DescriptorMetadata> descriptorMetadata5 = response5.getContent().get().getDescriptors();
        assertEquals(expectedSize, descriptorMetadata5.size());

        DescriptorType type6 = DescriptorType.PROVIDER;
        ConfigContextEnum context6 = ConfigContextEnum.DISTRIBUTION;
        ActionResponse<DescriptorsResponseModel> response6 = actions.getDescriptorsByPermissions(null, type6.name(), context6.name());
        Assertions.assertTrue(response6.isSuccessful());
        Assertions.assertTrue(response6.hasContent());
        Set<DescriptorMetadata> descriptorMetadata6 = response6.getContent().get().getDescriptors();
        assertEquals(expectedSize, descriptorMetadata6.size());
    }

    @Test
    public void getDescriptorsWithNameAndContextTest() {
        String componentName1 = getNamePrefix(DescriptorType.CHANNEL) + "_4";
        ConfigContextEnum context1 = ConfigContextEnum.GLOBAL;
        ActionResponse<DescriptorsResponseModel> response1 = actions.getDescriptorsByPermissions(componentName1, null, context1.name());
        Assertions.assertTrue(response1.isSuccessful());
        Assertions.assertTrue(response1.hasContent());
        Set<DescriptorMetadata> descriptorMetadata1 = response1.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata1.size());

        String componentName2 = getNamePrefix(DescriptorType.COMPONENT) + "_4";
        ConfigContextEnum context2 = ConfigContextEnum.GLOBAL;
        ActionResponse<DescriptorsResponseModel> response2 = actions.getDescriptorsByPermissions(componentName2, null, context2.name());
        Assertions.assertTrue(response2.isSuccessful());
        Assertions.assertTrue(response2.hasContent());
        Set<DescriptorMetadata> descriptorMetadata2 = response2.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata2.size());

        String componentName3 = getNamePrefix(DescriptorType.PROVIDER) + "_4";
        ConfigContextEnum context3 = ConfigContextEnum.GLOBAL;
        ActionResponse<DescriptorsResponseModel> response3 = actions.getDescriptorsByPermissions(componentName3, null, context3.name());
        Assertions.assertTrue(response3.isSuccessful());
        Assertions.assertTrue(response3.hasContent());
        Set<DescriptorMetadata> descriptorMetadata3 = response3.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata3.size());

        ConfigContextEnum context4 = ConfigContextEnum.DISTRIBUTION;
        ActionResponse<DescriptorsResponseModel> response4 = actions.getDescriptorsByPermissions(componentName1, null, context4.name());
        Assertions.assertTrue(response4.isSuccessful());
        Assertions.assertTrue(response4.hasContent());
        Set<DescriptorMetadata> descriptorMetadata4 = response4.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata4.size());

        ConfigContextEnum context5 = ConfigContextEnum.DISTRIBUTION;
        ActionResponse<DescriptorsResponseModel> response5 = actions.getDescriptorsByPermissions(componentName2, null, context5.name());
        Assertions.assertTrue(response5.isSuccessful());
        Assertions.assertTrue(response5.hasContent());
        Set<DescriptorMetadata> descriptorMetadata5 = response5.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata5.size());

        ConfigContextEnum context6 = ConfigContextEnum.DISTRIBUTION;
        ActionResponse<DescriptorsResponseModel> response6 = actions.getDescriptorsByPermissions(componentName3, null, context6.name());
        Assertions.assertTrue(response6.isSuccessful());
        Assertions.assertTrue(response6.hasContent());
        Set<DescriptorMetadata> descriptorMetadata6 = response6.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata6.size());
    }

    @Test
    public void getDescriptorsWithAllParametersTest() {
        DescriptorType type1 = DescriptorType.CHANNEL;
        String componentName1 = getNamePrefix(type1) + "_2";
        ConfigContextEnum context1 = ConfigContextEnum.GLOBAL;
        ActionResponse<DescriptorsResponseModel> response1 = actions.getDescriptorsByPermissions(componentName1, type1.name(), context1.name());
        Assertions.assertTrue(response1.isSuccessful());
        Assertions.assertTrue(response1.hasContent());
        Set<DescriptorMetadata> descriptorMetadata1 = response1.getContent().get().getDescriptors();
        assertEquals(1, descriptorMetadata1.size());
    }

    private DescriptorMetadataActions createDescriptorController() {
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.doReturn(true).when(authorizationManager).isReadOnly(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(true).when(authorizationManager).hasPermissions(Mockito.anyString(), Mockito.anyString());
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
            super(new DescriptorKey() {
                @Override
                public String getUniversalKey() {
                    return descriptorName;
                }

                @Override
                public String getDisplayName() {
                    return descriptorName;
                }
            }, type);
            if (contexts != null) {
                this.contexts = Arrays.asList(contexts);
            } else {
                this.contexts = List.of();
            }
        }

        @Override
        public Set<DefinedFieldModel> getAllDefinedFields(ConfigContextEnum context) {
            return Set.of();
        }

        @Override
        public boolean hasUIConfigForType(ConfigContextEnum actionApiType) {
            return contexts.contains(actionApiType);
        }

        @Override
        public boolean hasUIConfigs() {
            return true;
        }

        @Override
        public Optional<UIConfig> getUIConfig(ConfigContextEnum context) {
            if (!contexts.contains(context)) {
                return Optional.empty();
            }

            return Optional.of(new UIConfig("Label", "description", "urlName") {

                @Override
                public List<ConfigField> createFields() {
                    return List.of();
                }

            });
        }

    }

}
