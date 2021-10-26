package com.synopsys.integration.alert.channel.jira.server.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;
import com.synopsys.integration.alert.test.common.channel.GlobalConfigurationValidatorAsserter;

public class JiraServerGlobalConfigurationFieldModelValidatorTest {
    /*
     * Jira url: required, valid url
     * username: required
     * password: required
     */

    @Test
    public void verifyValidConfig() {
        GlobalConfigurationValidatorAsserter globalConfigurationValidatorAsserter = createGlobalConfigurationValidatorAsserter();
        globalConfigurationValidatorAsserter.assertValid();
    }

    @Test
    public void invalidUrl() {
        GlobalConfigurationValidatorAsserter globalConfigurationValidatorAsserter = createGlobalConfigurationValidatorAsserter();
        globalConfigurationValidatorAsserter.assertInvalidValue(JiraServerDescriptor.KEY_SERVER_URL, "not_a_url");
    }

    @Test
    public void missingPassword() {
        GlobalConfigurationValidatorAsserter globalConfigurationValidatorAsserter = createGlobalConfigurationValidatorAsserter();
        globalConfigurationValidatorAsserter.assertMissingValue(JiraServerDescriptor.KEY_SERVER_PASSWORD);
    }

    private GlobalConfigurationValidatorAsserter createGlobalConfigurationValidatorAsserter() {
        return new GlobalConfigurationValidatorAsserter(new JiraCloudChannelKey().getUniversalKey(), new JiraServerGlobalConfigurationFieldModelValidator(), createValidKeyToValues());
    }

    private Map<String, FieldValueModel> createValidKeyToValues() {
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        FieldValueModel url = new FieldValueModel(List.of("http://url.com"), true);
        FieldValueModel username = new FieldValueModel(List.of("username"), true);
        FieldValueModel password = new FieldValueModel(List.of("password"), true);

        keyToValues.put(JiraServerDescriptor.KEY_SERVER_URL, url);
        keyToValues.put(JiraServerDescriptor.KEY_SERVER_USERNAME, username);
        keyToValues.put(JiraServerDescriptor.KEY_SERVER_PASSWORD, password);

        return keyToValues;
    }
}
