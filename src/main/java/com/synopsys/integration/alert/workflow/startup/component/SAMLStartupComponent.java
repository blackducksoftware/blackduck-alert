package com.synopsys.integration.alert.workflow.startup.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.web.security.authentication.saml.SAMLManager;

@Component
@Order(6)
public class SAMLStartupComponent extends StartupComponent {
    private final SAMLManager samlManager;

    @Autowired
    public SAMLStartupComponent(final SAMLManager samlManager) {
        this.samlManager = samlManager;
    }

    @Override
    public void run() {
        samlManager.initializeSAML();
    }
}
