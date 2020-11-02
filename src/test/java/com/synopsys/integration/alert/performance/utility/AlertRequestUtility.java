package com.synopsys.integration.alert.performance.utility;

import com.synopsys.integration.exception.IntegrationException;

public interface AlertRequestUtility {
    public String executeGetRequest(String path, String error) throws IntegrationException;

    public String executePostRequest(String path, String requestBody, String error) throws IntegrationException;

    public String executePutRequest(String path, String requestBody, String error) throws IntegrationException;
}
