package com.synopsys.integration.alert.channel.jira.cloud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.jira.common.cloud.configuration.JiraCloudRestConfig;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class JiraCloudPropertiesTest {
    @Test
    public void testBuildConfigException() {
        try {
            JiraCloudProperties properties = new JiraCloudProperties(null, null, null, false, ProxyInfo.NO_PROXY_INFO);
            assertNull(properties.getUrl());
            assertNull(properties.getAccessToken());
            assertNull(properties.getUsername());
            assertFalse(properties.isPluginCheckDisabled());
            properties.createJiraServerConfig();
            fail();
        } catch (IssueTrackerException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testBuildConfig() {
        try {
            String url = "http://localhost:2990";
            String token = "token";
            String user = "user";
            boolean pluginCheckDisabled = true;
            JiraCloudProperties properties = new JiraCloudProperties(url, token, user, pluginCheckDisabled, ProxyInfo.NO_PROXY_INFO);
            assertEquals(url, properties.getUrl());
            assertEquals(token, properties.getAccessToken());
            assertEquals(user, properties.getUsername());
            assertEquals(pluginCheckDisabled, properties.isPluginCheckDisabled());
            JiraCloudRestConfig config = properties.createJiraServerConfig();
            assertNotNull(config);
        } catch (IssueTrackerException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    public void testServerServiceFactory() {
        try {
            JiraCloudProperties properties = new JiraCloudProperties("http://localhost:2990", "token", "user", false, ProxyInfo.NO_PROXY_INFO);
            JiraCloudServiceFactory serviceFactory = properties.createJiraServicesCloudFactory(LoggerFactory.getLogger(getClass()), new Gson());
            assertNotNull(serviceFactory);
        } catch (IssueTrackerException ex) {
            ex.printStackTrace();
            fail();
        }
    }

}
