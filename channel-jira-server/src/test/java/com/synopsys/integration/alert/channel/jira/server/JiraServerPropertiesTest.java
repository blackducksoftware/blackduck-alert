package com.synopsys.integration.alert.channel.jira.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.jira.common.server.configuration.JiraServerRestConfig;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.rest.proxy.ProxyInfo;

class JiraServerPropertiesTest {
    @Test
    void testBuildConfigException() {
        try {
            JiraServerProperties properties = new JiraServerProperties(null, null, null, null, null, false, ProxyInfo.NO_PROXY_INFO);
            properties.createJiraServerConfig();
            assertNull(properties.getUrl());
            assertNull(properties.getPassword());
            assertNull(properties.getUsername());
            assertFalse(properties.isPluginCheckDisabled());
            fail();
        } catch (IssueTrackerException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void testBuildBasicAuthConfig() {
        try {
            String url = "http://localhost:2990";
            String password = "password";
            String user = "user";
            boolean pluginCheckDisabled = true;
            JiraServerProperties properties = new JiraServerProperties(
                url,
                JiraServerAuthorizationMethod.BASIC,
                password,
                user,
                null,
                pluginCheckDisabled,
                ProxyInfo.NO_PROXY_INFO
            );
            assertEquals(url, properties.getUrl());
            assertEquals(password, properties.getPassword().orElse("Password missing."));
            assertEquals(user, properties.getUsername().orElse("Username missing."));
            assertTrue(properties.getAccessToken().isEmpty());
            assertEquals(pluginCheckDisabled, properties.isPluginCheckDisabled());
            JiraServerRestConfig config = properties.createJiraServerConfig();
            assertNotNull(config);
        } catch (IssueTrackerException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    void testBuildBearerAuthConfig() {
        try {
            String url = "http://localhost:2990";
            String accessToken = "jiraServerAccessToken";
            boolean pluginCheckDisabled = true;
            JiraServerProperties properties = new JiraServerProperties(
                url,
                JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN,
                null,
                null,
                accessToken,
                pluginCheckDisabled,
                ProxyInfo.NO_PROXY_INFO
            );
            assertEquals(url, properties.getUrl());
            assertEquals(accessToken, properties.getAccessToken().orElse("Missing access token"));
            assertTrue(properties.getUsername().isEmpty());
            assertTrue(properties.getPassword().isEmpty());
            assertEquals(pluginCheckDisabled, properties.isPluginCheckDisabled());
            JiraServerRestConfig config = properties.createJiraServerConfig();
            assertNotNull(config);
        } catch (IssueTrackerException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    void testServerServiceFactory() {
        try {
            JiraServerProperties properties = new JiraServerProperties(
                "http://localhost:2990",
                JiraServerAuthorizationMethod.BASIC,
                "password",
                "user",
                "accesToken",
                false,
                ProxyInfo.NO_PROXY_INFO
            );
            JiraServerServiceFactory serviceFactory = properties.createJiraServicesServerFactory(LoggerFactory.getLogger(getClass()), BlackDuckServicesFactory.createDefaultGson());
            assertNotNull(serviceFactory);
        } catch (IssueTrackerException ex) {
            ex.printStackTrace();
            fail();
        }
    }

}
