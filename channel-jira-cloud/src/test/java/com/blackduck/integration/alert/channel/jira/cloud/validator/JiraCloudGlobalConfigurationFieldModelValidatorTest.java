package com.blackduck.integration.alert.channel.jira.cloud.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.descriptor.JiraCloudChannelKey;
import com.blackduck.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.alert.test.common.channel.GlobalConfigurationValidatorAsserter;

public class JiraCloudGlobalConfigurationFieldModelValidatorTest {

    /*
     * Jira url: required, valid url
     * Admin email: required
     * Api token: required
     */

    @Test
    public void verifyValidConfig() {
        GlobalConfigurationValidatorAsserter globalConfigurationValidatorAsserter = createGlobalConfigurationValidatorAsserter();
        globalConfigurationValidatorAsserter.assertValid();
    }

    @Test
    public void invalidUrl() {
        GlobalConfigurationValidatorAsserter globalConfigurationValidatorAsserter = createGlobalConfigurationValidatorAsserter();
        globalConfigurationValidatorAsserter.assertInvalidValue(JiraCloudDescriptor.KEY_JIRA_URL, "not_a_url");
    }

    @Test
    public void missingApiToken() {
        GlobalConfigurationValidatorAsserter globalConfigurationValidatorAsserter = createGlobalConfigurationValidatorAsserter();
        globalConfigurationValidatorAsserter.assertMissingValue(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN);
    }

    private GlobalConfigurationValidatorAsserter createGlobalConfigurationValidatorAsserter() {
        return new GlobalConfigurationValidatorAsserter(new JiraCloudChannelKey().getUniversalKey(), new JiraCloudGlobalConfigurationFieldModelValidator(), createValidKeyToValues());
    }

    private Map<String, FieldValueModel> createValidKeyToValues() {
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        FieldValueModel url = new FieldValueModel(List.of("http://url.com"), true);
        FieldValueModel email = new FieldValueModel(List.of("email"), true);
        FieldValueModel apiToken = new FieldValueModel(List.of("apiToken"), true);

        keyToValues.put(JiraCloudDescriptor.KEY_JIRA_URL, url);
        keyToValues.put(JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS, email);
        keyToValues.put(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN, apiToken);

        return keyToValues;
    }
}
