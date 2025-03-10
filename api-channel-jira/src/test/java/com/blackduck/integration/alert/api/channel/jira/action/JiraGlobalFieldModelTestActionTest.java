/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.action;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.common.message.model.MessageResult;
import com.blackduck.integration.exception.IntegrationException;

public class JiraGlobalFieldModelTestActionTest {
    private static final String EXPECTED_EXCEPTION = "Expected an exception to be thrown";

    private JiraGlobalFieldModelTestAction testAction;

    @BeforeEach
    public void init() throws IntegrationException {
        testAction = Mockito.mock(JiraGlobalFieldModelTestAction.class);
        Mockito.when(testAction.testConfig(Mockito.anyString(), Mockito.any(), Mockito.any())).thenCallRealMethod();
    }

    @Test
    public void testConfigGetIssuesTest() throws IntegrationException {
        Mockito.doReturn(false).when(testAction).canUserGetIssues(Mockito.any());

        try {
            testAction.testConfig("0", null, null);
            fail(EXPECTED_EXCEPTION);
        } catch (IntegrationException e) {
            // Pass
        }
    }

    @Test
    public void testConfigNonAdminTest() throws IntegrationException {
        Mockito.doReturn(false).when(testAction).canUserGetIssues(Mockito.any());
        Mockito.doReturn(true).when(testAction).isAppCheckEnabled(Mockito.any());
        Mockito.doReturn(false).when(testAction).isUserAdmin(Mockito.any());

        try {
            testAction.testConfig("0", null, null);
            fail(EXPECTED_EXCEPTION);
        } catch (IntegrationException e) {
            // Pass
        }
    }

    @Test
    public void testConfigAppMissingTest() throws IntegrationException {
        Mockito.doReturn(false).when(testAction).canUserGetIssues(Mockito.any());
        Mockito.doReturn(true).when(testAction).isAppCheckEnabled(Mockito.any());
        Mockito.doReturn(true).when(testAction).isUserAdmin(Mockito.any());
        Mockito.doReturn(true).when(testAction).isAppMissing(Mockito.any());

        try {
            testAction.testConfig("0", null, null);
            fail(EXPECTED_EXCEPTION);
        } catch (IntegrationException e) {
            // Pass
        }
    }

    @Test
    public void testConfigExceptionTest() throws IntegrationException {
        String exceptionMessage = "fake exception message";
        Mockito.doThrow(new IntegrationException(exceptionMessage)).when(testAction).canUserGetIssues(Mockito.any());

        try {
            testAction.testConfig("0", null, null);
            fail(EXPECTED_EXCEPTION);
        } catch (IntegrationException e) {
            assertTrue(e.getMessage().contains(exceptionMessage));
        }
    }

    @Test
    public void testConfigSuccessTest() throws IntegrationException {
        Mockito.doReturn(true).when(testAction).canUserGetIssues(Mockito.any());
        Mockito.doReturn(false).when(testAction).isAppCheckEnabled(Mockito.any());

        MessageResult messageResult = testAction.testConfig("0", null, null);
        assertNotNull(messageResult, "Expected a non-null message result");
    }

}
