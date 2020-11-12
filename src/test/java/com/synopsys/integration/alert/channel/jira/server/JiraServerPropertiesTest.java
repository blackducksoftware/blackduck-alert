package com.synopsys.integration.alert.channel.jira.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.jira.common.server.configuration.JiraServerRestConfig;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class JiraServerPropertiesTest {
    @Test
    public void testBuildConfigException() {
        try {
            JiraServerProperties properties = new JiraServerProperties(null, null, null, ProxyInfo.NO_PROXY_INFO);
            properties.createJiraServerConfig();
            assertNull(properties.getUrl());
            assertNull(properties.getPassword());
            assertNull(properties.getUsername());
            fail();
        } catch (IssueTrackerException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testBuildConfig() {
        try {
            final String url = "http://localhost:2990";
            final String password = "password";
            final String user = "user";
            JiraServerProperties properties = new JiraServerProperties(url, password, user, ProxyInfo.NO_PROXY_INFO);
            assertEquals(url, properties.getUrl());
            assertEquals(password, properties.getPassword());
            assertEquals(user, properties.getUsername());
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
            JiraServerProperties properties = new JiraServerProperties("http://localhost:2990", "password", "user", ProxyInfo.NO_PROXY_INFO);
            JiraServerServiceFactory serviceFactory = properties.createJiraServicesServerFactory(LoggerFactory.getLogger(getClass()), new Gson());
            assertNotNull(serviceFactory);
        } catch (IssueTrackerException ex) {
            ex.printStackTrace();
            fail();
        }
    }

}
