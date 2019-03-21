package com.synopsys.integration.alert.web.controller.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;

public class DescriptorControllerTest {
    private final Set<Descriptor> descriptors = createComprehensiveSetOfDescriptors();
    private final DescriptorController controller = new DescriptorController(descriptors);

    @Test
    public void getDescriptorsWithNoParametersTest() {
        final Set<DescriptorMetadata> descriptorMetadata = controller.getDescriptors(null, null, null);
        assertEquals(descriptors.size(), descriptorMetadata.size());
    }

    @Test
    public void getDescriptorsWithInvalidParametersTest() {
        final Set<DescriptorMetadata> descriptorMetadata1 = controller.getDescriptors("x not a real name x", null, null);
        assertEquals(0, descriptorMetadata1.size());

        final Set<DescriptorMetadata> descriptorMetadata2 = controller.getDescriptors(null, "x not a real type x", null);
        assertEquals(0, descriptorMetadata2.size());

        final Set<DescriptorMetadata> descriptorMetadata3 = controller.getDescriptors(null, null, "x not a real context x");
        assertEquals(0, descriptorMetadata3.size());
    }

    @Test
    public void getDescriptorsWithNameOnlyTest() {
        final String componentName = getNamePrefix(DescriptorType.CHANNEL) + "_2";
        final Set<DescriptorMetadata> descriptorMetadata1 = controller.getDescriptors(componentName, null, null);
        assertEquals(1, descriptorMetadata1.size());

        final String channelName = getNamePrefix(DescriptorType.COMPONENT) + "_2";
        final Set<DescriptorMetadata> descriptorMetadata2 = controller.getDescriptors(channelName, null, null);
        assertEquals(1, descriptorMetadata2.size());

        final String providerName = getNamePrefix(DescriptorType.PROVIDER) + "_2";
        final Set<DescriptorMetadata> descriptorMetadata3 = controller.getDescriptors(providerName, null, null);
        assertEquals(1, descriptorMetadata3.size());
    }

    @Test
    public void getDescriptorsWithTypeOnlyTest() {
        final Set<DescriptorMetadata> descriptorMetadata1 = controller.getDescriptors(null, DescriptorType.CHANNEL.name(), null);
        assertEquals(descriptors.size() / 3, descriptorMetadata1.size());

        final Set<DescriptorMetadata> descriptorMetadata2 = controller.getDescriptors(null, DescriptorType.COMPONENT.name(), null);
        assertEquals(descriptors.size() / 3, descriptorMetadata2.size());

        final Set<DescriptorMetadata> descriptorMetadata3 = controller.getDescriptors(null, DescriptorType.PROVIDER.name(), null);
        assertEquals(descriptors.size() / 3, descriptorMetadata3.size());
    }

    @Test
    public void getDescriptorsWithContextOnlyTest() {
        final Set<DescriptorMetadata> descriptorMetadata1 = controller.getDescriptors(null, null, ConfigContextEnum.GLOBAL.name());
        assertEquals(descriptors.size() / 2, descriptorMetadata1.size());

        final Set<DescriptorMetadata> descriptorMetadata2 = controller.getDescriptors(null, null, ConfigContextEnum.DISTRIBUTION.name());
        assertEquals(descriptors.size() / 2, descriptorMetadata2.size());
    }

    @Test
    public void getDescriptorsWithTypeAndContextTest() {
        final DescriptorType type1 = DescriptorType.CHANNEL;
        final ConfigContextEnum context1 = ConfigContextEnum.GLOBAL;
        final Set<DescriptorMetadata> descriptorMetadata1 = controller.getDescriptors(null, type1.name(), context1.name());
        assertEquals(descriptors.size() / (3 * 2), descriptorMetadata1.size());

        final DescriptorType type2 = DescriptorType.COMPONENT;
        final ConfigContextEnum context2 = ConfigContextEnum.GLOBAL;
        final Set<DescriptorMetadata> descriptorMetadata2 = controller.getDescriptors(null, type2.name(), context2.name());
        assertEquals(descriptors.size() / (3 * 2), descriptorMetadata2.size());

        final DescriptorType type3 = DescriptorType.PROVIDER;
        final ConfigContextEnum context3 = ConfigContextEnum.GLOBAL;
        final Set<DescriptorMetadata> descriptorMetadata3 = controller.getDescriptors(null, type3.name(), context3.name());
        assertEquals(descriptors.size() / (3 * 2), descriptorMetadata3.size());

        final DescriptorType type4 = DescriptorType.CHANNEL;
        final ConfigContextEnum context4 = ConfigContextEnum.DISTRIBUTION;
        final Set<DescriptorMetadata> descriptorMetadata4 = controller.getDescriptors(null, type4.name(), context4.name());
        assertEquals(descriptors.size() / (3 * 2), descriptorMetadata4.size());

        final DescriptorType type5 = DescriptorType.COMPONENT;
        final ConfigContextEnum context5 = ConfigContextEnum.DISTRIBUTION;
        final Set<DescriptorMetadata> descriptorMetadata5 = controller.getDescriptors(null, type5.name(), context5.name());
        assertEquals(descriptors.size() / (3 * 2), descriptorMetadata5.size());

        final DescriptorType type6 = DescriptorType.PROVIDER;
        final ConfigContextEnum context6 = ConfigContextEnum.DISTRIBUTION;
        final Set<DescriptorMetadata> descriptorMetadata6 = controller.getDescriptors(null, type6.name(), context6.name());
        assertEquals(descriptors.size() / (3 * 2), descriptorMetadata6.size());
    }

    @Test
    public void getDescriptorsWithNameAndContextTest() {
        final String componentName1 = getNamePrefix(DescriptorType.CHANNEL) + "_4";
        final ConfigContextEnum context1 = ConfigContextEnum.GLOBAL;
        final Set<DescriptorMetadata> descriptorMetadata1 = controller.getDescriptors(componentName1, null, context1.name());
        assertEquals(1, descriptorMetadata1.size());

        final String componentName2 = getNamePrefix(DescriptorType.COMPONENT) + "_4";
        final ConfigContextEnum context2 = ConfigContextEnum.GLOBAL;
        final Set<DescriptorMetadata> descriptorMetadata2 = controller.getDescriptors(componentName2, null, context2.name());
        assertEquals(1, descriptorMetadata2.size());

        final String componentName3 = getNamePrefix(DescriptorType.PROVIDER) + "_4";
        final ConfigContextEnum context3 = ConfigContextEnum.GLOBAL;
        final Set<DescriptorMetadata> descriptorMetadata3 = controller.getDescriptors(componentName3, null, context3.name());
        assertEquals(1, descriptorMetadata3.size());

        final ConfigContextEnum context4 = ConfigContextEnum.DISTRIBUTION;
        final Set<DescriptorMetadata> descriptorMetadata4 = controller.getDescriptors(componentName1, null, context4.name());
        assertEquals(1, descriptorMetadata4.size());

        final ConfigContextEnum context5 = ConfigContextEnum.DISTRIBUTION;
        final Set<DescriptorMetadata> descriptorMetadata5 = controller.getDescriptors(componentName2, null, context5.name());
        assertEquals(1, descriptorMetadata5.size());

        final ConfigContextEnum context6 = ConfigContextEnum.DISTRIBUTION;
        final Set<DescriptorMetadata> descriptorMetadata6 = controller.getDescriptors(componentName3, null, context6.name());
        assertEquals(1, descriptorMetadata6.size());
    }

    @Test
    public void getDescriptorsWithAllParametersTest() {
        final DescriptorType type1 = DescriptorType.CHANNEL;
        final String componentName1 = getNamePrefix(type1) + "_2";
        final ConfigContextEnum context1 = ConfigContextEnum.GLOBAL;
        final Set<DescriptorMetadata> descriptorMetadata1 = controller.getDescriptors(componentName1, type1.name(), context1.name());
        assertEquals(1, descriptorMetadata1.size());
    }

    private Set<Descriptor> createComprehensiveSetOfDescriptors() {
        final Set<Descriptor> descriptors = new HashSet<>();
        descriptors.addAll(createDescriptorsOfType(DescriptorType.PROVIDER));
        descriptors.addAll(createDescriptorsOfType(DescriptorType.CHANNEL));
        descriptors.addAll(createDescriptorsOfType(DescriptorType.COMPONENT));
        return descriptors;
    }

    private Set<Descriptor> createDescriptorsOfType(final DescriptorType descriptorType) {
        final String namePrefix = getNamePrefix(descriptorType);
        final Descriptor d1 = new TestDescriptor(namePrefix + "_1", descriptorType);
        final Descriptor d2 = new TestDescriptor(namePrefix + "_2", descriptorType, ConfigContextEnum.GLOBAL);
        final Descriptor d3 = new TestDescriptor(namePrefix + "_3", descriptorType, ConfigContextEnum.DISTRIBUTION);
        final Descriptor d4 = new TestDescriptor(namePrefix + "_4", descriptorType, ConfigContextEnum.GLOBAL, ConfigContextEnum.DISTRIBUTION);

        return Set.of(d1, d2, d3, d4);
    }

    private String getNamePrefix(final DescriptorType descriptorType) {
        return descriptorType.name().toLowerCase();
    }

    private class TestDescriptor extends Descriptor {
        private final List<ConfigContextEnum> contexts;

        public TestDescriptor(final String name, final DescriptorType type, final ConfigContextEnum... contexts) {
            super(name, type);
            if (contexts != null) {
                this.contexts = Arrays.asList(contexts);
            } else {
                this.contexts = List.of();
            }
        }

        @Override
        public Set<DefinedFieldModel> getAllDefinedFields(final ConfigContextEnum context) {
            return Set.of();
        }

        @Override
        public boolean hasUIConfigForType(final ConfigContextEnum actionApiType) {
            return contexts.contains(actionApiType);
        }

        @Override
        public boolean hasUIConfigs() {
            return true;
        }

        @Override
        public Optional<UIConfig> getUIConfig(final ConfigContextEnum context) {
            if (!contexts.contains(context)) {
                return Optional.empty();
            }
            final String descriptorName = getName();
            final DescriptorType descriptorType = getType();

            return Optional.of(new UIConfig("Label", "description", "urlName", "fontAwesomeIcon") {

                @Override
                public List<ConfigField> createFields() {
                    return List.of();
                }
            });
        }
    }
}
