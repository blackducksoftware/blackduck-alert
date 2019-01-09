package com.synopsys.integration.alert.workflow.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.model.DefinedFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class DescriptorRegistratorTestIT extends AlertIntegrationTest {

    @Autowired
    BaseDescriptorAccessor descriptorAccessor;

    @Autowired
    HipChatDescriptor hipChatDescriptor;

    @BeforeEach
    @AfterEach
    public void unregisterDescriptor() throws AlertDatabaseConstraintException {
        descriptorAccessor.unregisterDescriptor(hipChatDescriptor.getName());
    }

    @Test
    public void registerDescriptorsTest() throws AlertDatabaseConstraintException {
        descriptorAccessor.unregisterDescriptor(hipChatDescriptor.getName());
        final Optional<RegisteredDescriptorModel> descriptor = descriptorAccessor.getRegisteredDescriptorByName(hipChatDescriptor.getName());
        assertTrue(descriptor.isEmpty());

        final DescriptorRegistrator descriptorRegistrator = new DescriptorRegistrator(descriptorAccessor, List.of(hipChatDescriptor));
        descriptorRegistrator.registerDescriptors();

        final Optional<RegisteredDescriptorModel> foundDescriptor = descriptorAccessor.getRegisteredDescriptorByName(hipChatDescriptor.getName());
        assertTrue(foundDescriptor.isPresent());

        final List<DefinedFieldModel> globalFields = descriptorAccessor.getFieldsForDescriptorById(foundDescriptor.get().getId(), ConfigContextEnum.GLOBAL);
        final List<DefinedFieldModel> distributionFields = descriptorAccessor.getFieldsForDescriptorById(foundDescriptor.get().getId(), ConfigContextEnum.DISTRIBUTION);

        assertEquals(hipChatDescriptor.getKeys(ConfigContextEnum.GLOBAL).keySet().size(), globalFields.size());

        //        CommonDistributionUIConfig commonDistributionUIConfig = new CommonDistributionUIConfig();
        //        commonDistributionUIConfig.createCommonConfigFields(Set.of(), Set.of()).size();
        //
        //        ProviderDistributionUIConfig providerDistributionUIConfig = new ProviderDistributionUIConfig();
        //        providerDistributionUIConfig.createCommonConfigFields()

        // FIXME When we find out the best way to add common fields to descriptors, update this accordingly and remove the + 6
        assertEquals(hipChatDescriptor.getKeys(ConfigContextEnum.DISTRIBUTION).keySet().size() + 6, distributionFields.size());
    }

    @Test
    public void addFieldTest() throws AlertDatabaseConstraintException {
        descriptorAccessor.unregisterDescriptor(hipChatDescriptor.getName());
        final Optional<RegisteredDescriptorModel> descriptor = descriptorAccessor.getRegisteredDescriptorByName(hipChatDescriptor.getName());
        assertTrue(descriptor.isEmpty());

        final DescriptorRegistrator descriptorRegistrator = new DescriptorRegistrator(descriptorAccessor, List.of(hipChatDescriptor));
        descriptorRegistrator.registerDescriptors();

        final Optional<RegisteredDescriptorModel> foundDescriptor = descriptorAccessor.getRegisteredDescriptorByName(hipChatDescriptor.getName());
        assertTrue(foundDescriptor.isPresent());

        final List<DefinedFieldModel> globalFields = descriptorAccessor.getFieldsForDescriptorById(foundDescriptor.get().getId(), ConfigContextEnum.GLOBAL);
        assertEquals(hipChatDescriptor.getKeys(ConfigContextEnum.GLOBAL).keySet().size(), globalFields.size());

        final HipChatDescriptor spyHipChatDescriptor = Mockito.spy(hipChatDescriptor);
        final Map<String, Boolean> keys = hipChatDescriptor.getKeys(ConfigContextEnum.GLOBAL);
        keys.put("newkey", true);
        Mockito.doReturn(keys).when(spyHipChatDescriptor).getKeys(ConfigContextEnum.GLOBAL);

        final DescriptorRegistrator spiedDescriptorRegistrator = new DescriptorRegistrator(descriptorAccessor, List.of(spyHipChatDescriptor));
        spiedDescriptorRegistrator.registerDescriptors();

        final List<DefinedFieldModel> updatedGlobalFields = descriptorAccessor.getFieldsForDescriptorById(foundDescriptor.get().getId(), ConfigContextEnum.GLOBAL);

        assertEquals(hipChatDescriptor.getKeys(ConfigContextEnum.GLOBAL).keySet().size() + 1, updatedGlobalFields.size());
    }

    @Test
    public void removeFieldTest() throws AlertDatabaseConstraintException {
        descriptorAccessor.unregisterDescriptor(hipChatDescriptor.getName());
        final Optional<RegisteredDescriptorModel> descriptor = descriptorAccessor.getRegisteredDescriptorByName(hipChatDescriptor.getName());
        assertTrue(descriptor.isEmpty());

        final DescriptorRegistrator descriptorRegistrator = new DescriptorRegistrator(descriptorAccessor, List.of(hipChatDescriptor));
        descriptorRegistrator.registerDescriptors();

        final Optional<RegisteredDescriptorModel> foundDescriptor = descriptorAccessor.getRegisteredDescriptorByName(hipChatDescriptor.getName());
        assertTrue(foundDescriptor.isPresent());

        final List<DefinedFieldModel> globalFields = descriptorAccessor.getFieldsForDescriptorById(foundDescriptor.get().getId(), ConfigContextEnum.GLOBAL);
        assertEquals(hipChatDescriptor.getKeys(ConfigContextEnum.GLOBAL).keySet().size(), globalFields.size());

        final HipChatDescriptor spyHipChatDescriptor = Mockito.spy(hipChatDescriptor);
        final Map<String, Boolean> keys = hipChatDescriptor.getKeys(ConfigContextEnum.GLOBAL);
        keys.remove(HipChatDescriptor.KEY_API_KEY);
        Mockito.doReturn(keys).when(spyHipChatDescriptor).getKeys(ConfigContextEnum.GLOBAL);

        final DescriptorRegistrator spiedDescriptorRegistrator = new DescriptorRegistrator(descriptorAccessor, List.of(spyHipChatDescriptor));
        spiedDescriptorRegistrator.registerDescriptors();

        final List<DefinedFieldModel> updatedGlobalFields = descriptorAccessor.getFieldsForDescriptorById(foundDescriptor.get().getId(), ConfigContextEnum.GLOBAL);

        assertEquals(hipChatDescriptor.getKeys(ConfigContextEnum.GLOBAL).keySet().size() - 1, updatedGlobalFields.size());
    }

}
