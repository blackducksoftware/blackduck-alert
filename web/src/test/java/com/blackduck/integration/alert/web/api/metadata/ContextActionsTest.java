/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.metadata;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.web.api.metadata.model.ConfigContextsResponseModel;

public class ContextActionsTest {
    @Test
    public void getContextsTest() {
        ContextActions actions = new ContextActions();
        ActionResponse<ConfigContextsResponseModel> response = actions.getAll();
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        ConfigContextEnum[] contexts = response.getContent().get().configContexts;
        assertArrayEquals(ConfigContextEnum.values(), contexts);
    }

}
