package com.blackduck.integration.alert.web.api.metadata;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.DescriptorType;
import com.blackduck.integration.alert.web.api.metadata.model.DescriptorTypesResponseModel;

public class DescriptorTypesActionTest {
    @Test
    public void getTypesTest() {
        DescriptorTypeActions actions = new DescriptorTypeActions();
        ActionResponse<DescriptorTypesResponseModel> response = actions.getAll();
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        DescriptorType[] descriptorTypes = response.getContent().get().descriptorTypes;
        assertArrayEquals(DescriptorType.values(), descriptorTypes);
    }

}
