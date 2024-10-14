/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.mock;

import org.json.JSONException;

public interface MockUtils {

    Long getId();

    void testConfiguration() throws JSONException;

}
