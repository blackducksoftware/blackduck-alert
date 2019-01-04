package com.synopsys.integration.alert.web.controller.metadata;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.DescriptorType;

public class DescriptorTypeControllerTest {
    @Test
    public void getTypesTest() {
        final DescriptorTypeController descriptorTypeController = new DescriptorTypeController();
        assertArrayEquals(DescriptorType.values(), descriptorTypeController.getTypes());
    }
}
