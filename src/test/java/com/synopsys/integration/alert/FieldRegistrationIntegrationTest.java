package com.synopsys.integration.alert;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.workflow.upgrade.DescriptorRegistrator;

public class FieldRegistrationIntegrationTest extends AlertIntegrationTest {
    @Autowired
    protected DescriptorRegistrator descriptorRegistrator;

    public void registerDescriptors() {
        descriptorRegistrator.registerDescriptors();
    }
}
