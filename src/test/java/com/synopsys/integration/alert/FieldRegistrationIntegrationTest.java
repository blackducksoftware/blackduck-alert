package com.synopsys.integration.alert;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.DescriptorRegistrar;

public class FieldRegistrationIntegrationTest extends AlertIntegrationTest {
    @Autowired
    protected DescriptorRegistrar descriptorRegistrar;

    public void registerDescriptors() throws AlertDatabaseConstraintException {
        descriptorRegistrar.registerDescriptors();
    }
}
