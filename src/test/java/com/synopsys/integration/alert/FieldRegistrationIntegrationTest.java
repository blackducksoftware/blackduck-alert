package com.synopsys.integration.alert;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.workflow.startup.StartupManager;

public class FieldRegistrationIntegrationTest extends AlertIntegrationTest {
    @Autowired
    protected StartupManager startupManager;

    public void registerDescriptors() {
        startupManager.registerDescriptors();
    }
}
