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
package com.blackducksoftware.integration.alert;

import org.apache.commons.lang3.StringUtils;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.config.AlertEnvironment;
import com.blackducksoftware.integration.alert.config.GlobalProperties;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubRepository;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.BlackduckRestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.google.gson.Gson;

public class TestGlobalProperties extends GlobalProperties {
    private final TestProperties testProperties;
    private Integer hubTimeout;
    private String hubApiKey;
    private String productVersionOverride;

    public TestGlobalProperties() {
        this(new AlertEnvironment(), Mockito.mock(GlobalHubRepository.class));
    }

    public TestGlobalProperties(final AlertEnvironment alertEnvironment, final GlobalHubRepository globalHubRepository) {
        this(alertEnvironment, globalHubRepository, 400);
    }

    public TestGlobalProperties(final AlertEnvironment alertEnvironment, final GlobalHubRepository globalHubRepository, final Integer hubTimeout) {
        super(alertEnvironment, globalHubRepository, new Gson());
        this.hubTimeout = hubTimeout;

        testProperties = new TestProperties();
        setHubTimeout(Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_HUB_TIMEOUT)));
        setHubTrustCertificate(Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_TRUST_HTTPS_CERT)));
        setHubUrl(testProperties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL));
    }

    public void setHubTimeout(final Integer hubTimeout) {
        this.hubTimeout = hubTimeout;
    }

    @Override
    public Integer getHubTimeout() {
        return hubTimeout;
    }

    @Override
    public String getHubApiKey() {
        return hubApiKey;
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
    public BlackduckRestConnection createRestConnection(final IntLogger intLogger) throws AlertException {
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
        final BlackduckRestConnection restConnection = hubServerConfig.createCredentialsRestConnection(logger);
        return new HubServicesFactory(HubServicesFactory.createDefaultGson(), HubServicesFactory.createDefaultJsonParser(), restConnection, logger);
    }

}
