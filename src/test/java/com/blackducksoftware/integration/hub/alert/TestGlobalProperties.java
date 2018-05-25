/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert;

import org.apache.commons.lang3.StringUtils;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.google.gson.Gson;

public class TestGlobalProperties extends GlobalProperties {
    private Integer hubTimeout;
    private String hubApiKey;
    private String productVersionOverride;

    private final TestProperties testProperties;

    public TestGlobalProperties() {
        this(Mockito.mock(GlobalHubRepositoryWrapper.class));
    }

    public TestGlobalProperties(final GlobalHubRepositoryWrapper globalHubRepositoryWrapper) {
        this(globalHubRepositoryWrapper, 400);
    }

    public TestGlobalProperties(final GlobalHubRepositoryWrapper globalHubRepositoryWrapper, final Integer hubTimeout) {
        super(globalHubRepositoryWrapper, new Gson());
        this.hubTimeout = hubTimeout;

        testProperties = new TestProperties();
        setHubTimeout(Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_HUB_TIMEOUT)));
        setHubTrustCertificate(Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_TRUST_HTTPS_CERT)));
        setHubUrl(testProperties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL));
    }

    public void setHubTimeout(final Integer hubTimeout) {
        this.hubTimeout = hubTimeout;
    }

    public void setHubApiKey(final String hubApiKey) {
        this.hubApiKey = hubApiKey;
    }

    public void setProductVersionOverride(final String productVersionOverride) {
        this.productVersionOverride = productVersionOverride;
    }

    @Override
    public String getProductVersion() {
        if (StringUtils.isNotBlank(productVersionOverride)) {
            return productVersionOverride;
        } else {
            return super.getProductVersion();
        }
    }

    @Override
    public RestConnection createRestConnection(final IntLogger intLogger) throws AlertException {
        setHubUrl(testProperties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL));
        setHubTrustCertificate(true);
        return super.createRestConnection(intLogger);
    }

    @Override
    public GlobalHubConfigEntity getHubConfig() {
        return new GlobalHubConfigEntity(Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_HUB_TIMEOUT)), testProperties.getProperty(TestPropertyKey.TEST_HUB_API_KEY));
    }

    @Override
    public HubServerConfig createHubServerConfig(final IntLogger logger, final int hubTimeout, final String hubUsername, final String hubPassword) throws AlertException {
        return createHubServerConfigWithCredentials(logger);
    }

    public HubServerConfig createHubServerConfigWithCredentials(final IntLogger logger) throws NumberFormatException, AlertException {
        return super.createHubServerConfig(logger, Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_HUB_TIMEOUT)), testProperties.getProperty(TestPropertyKey.TEST_USERNAME),
                testProperties.getProperty(TestPropertyKey.TEST_PASSWORD));
    }

    public HubServicesFactory createHubServicesFactoryWithCredential(final IntLogger logger) throws Exception {
        setHubUrl(testProperties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL));
        setHubTrustCertificate(true);
        final HubServerConfig hubServerConfig = createHubServerConfigWithCredentials(logger);
        final RestConnection restConnection = hubServerConfig.createCredentialsRestConnection(logger);
        return new HubServicesFactory(restConnection);
    }

}
