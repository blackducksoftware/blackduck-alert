package com.synopsys.integration.alert.web.api.home;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SAMLEnabledResponseModelTest {
    @Test
    public void testSAMLEnabledTrue() {
        SAMLEnabledResponseModel model = new SAMLEnabledResponseModel(Boolean.TRUE);
        assertTrue(model.getSamlEnabled());
    }

    @Test
    public void testSAMLEnabledFalse() {
        SAMLEnabledResponseModel model = new SAMLEnabledResponseModel();
        assertFalse(model.getSamlEnabled());
    }
}
