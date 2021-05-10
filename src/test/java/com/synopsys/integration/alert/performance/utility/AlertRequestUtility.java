package com.synopsys.integration.alert.performance.utility;

import com.synopsys.integration.exception.IntegrationException;

public interface AlertRequestUtility {
    String executeGetRequest(String path, String error) throws IntegrationException;

    String executePostRequest(String path, String requestBody, String error) throws IntegrationException;

    String executePutRequest(String path, String requestBody, String error) throws IntegrationException;

}
