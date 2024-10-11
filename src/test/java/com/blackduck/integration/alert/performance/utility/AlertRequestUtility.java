/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.performance.utility;

import com.blackduck.integration.exception.IntegrationException;

public interface AlertRequestUtility {
    String executeGetRequest(String path, String error) throws IntegrationException;

    String executePostRequest(String path, String requestBody, String error) throws IntegrationException;

    String executePutRequest(String path, String requestBody, String error) throws IntegrationException;

}
