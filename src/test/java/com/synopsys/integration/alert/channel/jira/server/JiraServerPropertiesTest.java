package com.synopsys.integration.alert.channel.jira.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.jira.common.server.configuration.JiraServerRestConfig;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;

public class JiraServerPropertiesTest {
    @Test
    public void testBuildConfigException() {
        try {
            JiraServerProperties properties = new JiraServerProperties(null, null, null, false);
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
    public void testBuildConfig() {
        try {
            String url = "http://localhost:2990";
            String password = "password";
            String user = "user";
            boolean pluginCheckDisabled = true;
            JiraServerProperties properties = new JiraServerProperties(url, password, user, pluginCheckDisabled);
            assertEquals(url, properties.getUrl());
            assertEquals(password, properties.getPassword());
            assertEquals(user, properties.getUsername());
            assertEquals(pluginCheckDisabled, properties.isPluginCheckDisabled());
            JiraServerRestConfig config = properties.createJiraServerConfig();
            assertNotNull(config);
        } catch (IssueTrackerException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    public void testServerServiceFactory() {
        try {
            JiraServerProperties properties = new JiraServerProperties("http://localhost:2990", "password", "user", false);
            JiraServerServiceFactory serviceFactory = properties.createJiraServicesServerFactory(LoggerFactory.getLogger(getClass()), new Gson());
            assertNotNull(serviceFactory);
        } catch (IssueTrackerException ex) {
            ex.printStackTrace();
            fail();
        }
    }

}
