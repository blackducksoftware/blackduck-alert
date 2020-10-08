package com.synopsys.integration.alert.web.api.home;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SAMLEnabledResponseModelTest {
    @Test
    public void testSAMLEnabledTrue() {
        SAMLEnabledResponseModel model = new SAMLEnabledResponseModel(Boolean.TRUE);
        Assertions.assertTrue(model.getSamlEnabled());
    }

    @Test
    public void testSAMLEnabledFalse() {
        SAMLEnabledResponseModel model = new SAMLEnabledResponseModel();
        Assertions.assertFalse(model.getSamlEnabled());
    }
}
