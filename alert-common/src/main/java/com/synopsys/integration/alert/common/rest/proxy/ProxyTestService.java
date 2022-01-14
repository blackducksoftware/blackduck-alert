/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.proxy;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.message.model.ConfigurationTestResult;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

//TODO: This should be in the component subproject but currently requires several dependencies not available to that subproject.
@Component
public class ProxyTestService {
    private static final int DEFAULT_TIMEOUT_IN_SECONDS = 5 * 60 * 1000;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AlertProperties alertProperties;
    private final ProxyManager proxyManager;
    private final Gson gson;

    @Autowired
    public ProxyTestService(AlertProperties alertProperties, ProxyManager proxyManager, Gson gson) {
        this.alertProperties = alertProperties;
        this.proxyManager = proxyManager;
        this.gson = gson;
    }

    public ConfigurationTestResult pingHost(String testUrl, SettingsProxyModel settingsProxyModel) {
        ProxyInfo proxyInfo = proxyManager.createProxyInfo(settingsProxyModel);
        IntHttpClient client = createIntHttpClient(proxyInfo);

        try {
            HttpUrl httpUrl = new HttpUrl(testUrl);
            Request testRequest = new Request.Builder(httpUrl).build();
            Response response = client.execute(testRequest);
            if (RestConstants.OK_200 >= response.getStatusCode() && response.getStatusCode() < RestConstants.MULT_CHOICE_300) {
                logger.info("Successfully pinged {}!", testUrl);
                return ConfigurationTestResult.success();
            } else {
                return ConfigurationTestResult.failure(String.format("Could not ping: %s. Status Message: %s. Status code: %s", testUrl, response.getStatusMessage(), response.getStatusCode()));
            }
        } catch (IntegrationException e) {
            logger.error(e.getMessage(), e);
            return ConfigurationTestResult.failure(e.getMessage());
        }
    }

    private IntHttpClient createIntHttpClient(ProxyInfo proxyInfo) {
        Optional<Boolean> alertTrustCertificate = alertProperties.getAlertTrustCertificate();
        return new IntHttpClient(new Slf4jIntLogger(logger), gson, DEFAULT_TIMEOUT_IN_SECONDS, alertTrustCertificate.orElse(Boolean.FALSE), proxyInfo);
    }
}
