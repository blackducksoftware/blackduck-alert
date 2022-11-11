package com.synopsys.integration.alert.channel.azure.boards.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.oauth.OAuthRequestValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;
import com.synopsys.integration.alert.test.common.channel.GlobalConfigurationValidatorAsserter;

class AzureBoardsGlobalConfigurationFieldModelValidatorTest {

    /*
     * organizationName: required
     * client key: required
     * client secret: required
     * oauthRequestValidator: can't be running when validating
     */

    @Test
    void verifyValidConfig() {
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter();
        validatorAsserter.assertValid();
    }

    @Test
    void missingOrganizationName() {
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter();
        validatorAsserter.assertMissingValue(AzureBoardsDescriptor.KEY_ORGANIZATION_NAME);
    }

    @Test
    void missingClientKey() {
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter();
        validatorAsserter.assertMissingValue(AzureBoardsDescriptor.KEY_CLIENT_ID);
    }

    @Test
    void missingClientSecret() {
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter();
        validatorAsserter.assertMissingValue(AzureBoardsDescriptor.KEY_CLIENT_SECRET);
    }

    @Test
    void oauthRequestIsRunningError() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        UUID requestKey = oAuthRequestValidator.generateRequestKey();
        oAuthRequestValidator.addAuthorizationRequest(requestKey, UUID.randomUUID());

        AzureBoardsGlobalConfigurationFieldModelValidator azureBoardsGlobalConfigurationValidator = new AzureBoardsGlobalConfigurationFieldModelValidator(oAuthRequestValidator);
        Set<AlertFieldStatus> fieldStatuses = azureBoardsGlobalConfigurationValidator.validate(new FieldModel(
            new AzureBoardsChannelKey().getUniversalKey(),
            ConfigContextEnum.GLOBAL.name(),
            createValidKeyToValues()
        ));

        assertEquals(1, fieldStatuses.size());

        AlertFieldStatus alertFieldStatus = fieldStatuses.stream().findFirst().orElse(null);
        assertNotNull(alertFieldStatus);
        assertEquals(AzureBoardsDescriptor.KEY_OAUTH, alertFieldStatus.getFieldName());
    }

    private GlobalConfigurationValidatorAsserter createValidatorAsserter() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        return createValidatorAsserter(oAuthRequestValidator);
    }

    private GlobalConfigurationValidatorAsserter createValidatorAsserter(OAuthRequestValidator oAuthRequestValidator) {
        return new GlobalConfigurationValidatorAsserter(
            new AzureBoardsChannelKey().getUniversalKey(),
            new AzureBoardsGlobalConfigurationFieldModelValidator(oAuthRequestValidator),
            createValidKeyToValues()
        );
    }

    private Map<String, FieldValueModel> createValidKeyToValues() {
        Map<String, FieldValueModel> keyToValues = new HashMap<>();

        FieldValueModel organizationName = new FieldValueModel(List.of("organizationName"), true);
        FieldValueModel clientKey = new FieldValueModel(List.of("clientKey"), true);
        FieldValueModel clientSecret = new FieldValueModel(List.of("clientSecret"), true);
        keyToValues.put(AzureBoardsDescriptor.KEY_ORGANIZATION_NAME, organizationName);
        keyToValues.put(AzureBoardsDescriptor.KEY_CLIENT_ID, clientKey);
        keyToValues.put(AzureBoardsDescriptor.KEY_CLIENT_SECRET, clientSecret);

        return keyToValues;
    }
}
