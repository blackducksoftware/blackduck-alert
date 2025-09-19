/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.descriptor.JiraCloudChannelKey;
import com.blackduck.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.alert.test.common.channel.GlobalConfigurationValidatorAsserter;

class JiraCloudGlobalConfigurationFieldModelValidatorTest {

    /*
     * Jira url: required, valid url
     * Admin email: required
     * Api token: required
     */

    @Test
    void verifyValidConfig() {
        GlobalConfigurationValidatorAsserter globalConfigurationValidatorAsserter = createGlobalConfigurationValidatorAsserter();
        globalConfigurationValidatorAsserter.assertValid();
    }

    @Test
    void invalidUrl() {
        GlobalConfigurationValidatorAsserter globalConfigurationValidatorAsserter = createGlobalConfigurationValidatorAsserter();
        globalConfigurationValidatorAsserter.assertInvalidValue(JiraCloudDescriptor.KEY_JIRA_URL, "not_a_url");
    }

    @Test
    void missingApiToken() {
        GlobalConfigurationValidatorAsserter globalConfigurationValidatorAsserter = createGlobalConfigurationValidatorAsserter();
        globalConfigurationValidatorAsserter.assertMissingValue(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN);
    }

    @Test
    void invalidTimeout() {
        GlobalConfigurationValidatorAsserter globalConfigurationValidatorAsserter = createGlobalConfigurationValidatorAsserter();
        globalConfigurationValidatorAsserter.assertExceptionThrown(NumberFormatException.class, JiraCloudDescriptor.KEY_JIRA_TIMEOUT, "a string is invalid");
        globalConfigurationValidatorAsserter.assertInvalidValue(JiraCloudDescriptor.KEY_JIRA_TIMEOUT, "-1");
    }

    private GlobalConfigurationValidatorAsserter createGlobalConfigurationValidatorAsserter() {
        return new GlobalConfigurationValidatorAsserter(new JiraCloudChannelKey().getUniversalKey(), new JiraCloudGlobalConfigurationFieldModelValidator(), createValidKeyToValues());
    }

    private Map<String, FieldValueModel> createValidKeyToValues() {
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        FieldValueModel url = new FieldValueModel(List.of("http://url.com"), true);
        FieldValueModel email = new FieldValueModel(List.of("email"), true);
        FieldValueModel apiToken = new FieldValueModel(List.of("apiToken"), true);
        FieldValueModel timeoutInSeconds = new FieldValueModel(List.of("600"), true);

        keyToValues.put(JiraCloudDescriptor.KEY_JIRA_URL, url);
        keyToValues.put(JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS, email);
        keyToValues.put(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN, apiToken);
        keyToValues.put(JiraCloudDescriptor.KEY_JIRA_TIMEOUT, timeoutInSeconds);

        return keyToValues;
    }
}
