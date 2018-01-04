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
package com.blackducksoftware.integration.hub.alert.mock.model.global;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalSchedulingRepository;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHubConfigRestModel;
import com.google.gson.JsonObject;

public class MockGlobalHubRestModel extends MockGlobalRestModelUtil<GlobalHubConfigRestModel> {
    private final String hubUrl;
    private final String hubTimeout;
    private final String hubUsername;
    private final String hubPassword;
    private final boolean hubPasswordIsSet;
    private final String hubProxyHost;
    private final String hubProxyPort;
    private final String hubProxyUsername;
    private final String hubProxyPassword;
    private final boolean hubProxyPasswordIsSet;
    private final String hubAlwaysTrustCertificate;
    private final String id;

    public MockGlobalHubRestModel() {
        this("HubUrl", "444", "HubUsername", "HubPassword", false, "HubProxyHost", "555", "HubProxyUsername", "HubProxyPassword", true, "true", "1");
    }

    private MockGlobalHubRestModel(final String hubUrl, final String hubTimeout, final String hubUsername, final String hubPassword, final boolean hubPasswordIsSet, final String hubProxyHost, final String hubProxyPort,
            final String hubProxyUsername, final String hubProxyPassword, final boolean hubProxyPasswordIsSet, final String hubAlwaysTrustCertificate, final String id) {
        this.hubUrl = hubUrl;
        this.hubTimeout = hubTimeout;
        this.hubUsername = hubUsername;
        this.hubPassword = hubPassword;
        this.hubPasswordIsSet = hubPasswordIsSet;
        this.hubProxyHost = hubProxyHost;
        this.hubProxyPort = hubProxyPort;
        this.hubProxyUsername = hubProxyUsername;
        this.hubProxyPassword = hubProxyPassword;
        this.hubProxyPasswordIsSet = hubProxyPasswordIsSet;
        this.hubAlwaysTrustCertificate = hubAlwaysTrustCertificate;
        this.id = id;
    }

    public GlobalProperties createTestGlobalProperties() {
        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final GlobalSchedulingRepository mockedScheduledRepository = Mockito.mock(GlobalSchedulingRepository.class);
        return createTestGlobalProperties(mockedGlobalRepository, mockedScheduledRepository);
    }

    public GlobalProperties createTestGlobalProperties(final GlobalHubRepository globalRepository, final GlobalSchedulingRepository globalSchedulingRepository) {
        final TestGlobalProperties globalProperties = new TestGlobalProperties(globalRepository, globalSchedulingRepository);
        globalProperties.setHubUrl(hubUrl);
        globalProperties.setHubTrustCertificate(Boolean.valueOf(hubAlwaysTrustCertificate));
        globalProperties.setHubProxyHost(hubProxyHost);
        globalProperties.setHubProxyPort(hubProxyPort);
        globalProperties.setHubProxyUsername(hubProxyUsername);
        globalProperties.setHubProxyPassword(hubProxyPassword);
        return globalProperties;
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public String getHubTimeout() {
        return hubTimeout;
    }

    public String getHubUsername() {
        return hubUsername;
    }

    public String getHubPassword() {
        return hubPassword;
    }

    public String getHubProxyHost() {
        return hubProxyHost;
    }

    public String getHubProxyPort() {
        return hubProxyPort;
    }

    public String getHubProxyUsername() {
        return hubProxyUsername;
    }

    public String getHubProxyPassword() {
        return hubProxyPassword;
    }

    public String getHubAlwaysTrustCertificate() {
        return hubAlwaysTrustCertificate;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public GlobalHubConfigRestModel createGlobalRestModel() {
        final GlobalHubConfigRestModel restModel = new GlobalHubConfigRestModel(id, hubUrl, hubTimeout, hubUsername, hubPassword, hubPasswordIsSet, hubProxyHost, hubProxyPort, hubProxyUsername, hubProxyPassword, hubProxyPasswordIsSet,
                hubAlwaysTrustCertificate);
        return restModel;
    }

    @Override
    public GlobalHubConfigRestModel createEmptyGlobalRestModel() {
        return new GlobalHubConfigRestModel();
    }

    @Override
    public String getGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("hubUrl", hubUrl);
        json.addProperty("hubTimeout", hubTimeout);
        json.addProperty("hubUsername", hubUsername);
        json.addProperty("hubPasswordIsSet", hubPasswordIsSet);
        json.addProperty("hubProxyHost", hubProxyHost);
        json.addProperty("hubProxyPort", hubProxyPort);
        json.addProperty("hubProxyUsername", hubProxyUsername);
        json.addProperty("hubProxyPasswordIsSet", hubProxyPasswordIsSet);
        json.addProperty("hubAlwaysTrustCertificate", hubAlwaysTrustCertificate);
        json.addProperty("id", id);
        return json.toString();
    }

    @Override
    public String getEmptyGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("hubPasswordIsSet", false);
        json.addProperty("hubProxyPasswordIsSet", false);
        return json.toString();
    }

}
