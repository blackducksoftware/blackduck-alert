package com.synopsys.integration.alert.web.controller.metadata;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;

public class ContextControllerTest {
    @Test
    public void getContextsTest() {
        final ContextController contextController = new ContextController();
        assertArrayEquals(ConfigContextEnum.values(), contextController.getContexts());
    }
}
