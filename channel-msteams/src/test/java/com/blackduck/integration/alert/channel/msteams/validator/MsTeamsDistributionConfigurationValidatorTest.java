package com.blackduck.integration.alert.channel.msteams.validator;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.blackduck.integration.alert.api.channel.CommonChannelDistributionValidator;
import com.blackduck.integration.alert.channel.msteams.descriptor.MsTeamsDescriptor;
import com.blackduck.integration.alert.test.common.FieldModelUtils;
import com.blackduck.integration.alert.test.common.ValidationConstants;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.alert.common.rest.model.JobFieldModel;

public class MsTeamsDistributionConfigurationValidatorTest {
    public static final String EXPECTED_VALID_WEBHOOK_URL = "https://www.example.com/webhook";
    public static final String INVALID_WEBHOOK_URL = "bad_url";

    public static Stream<Arguments> getFieldsAndExpectedErrors() {
        return Stream.of(
            Arguments.of(
                Map.of(MsTeamsDescriptor.KEY_WEBHOOK, FieldModelUtils.createFieldValue(EXPECTED_VALID_WEBHOOK_URL)),
                Set.of()
            ),
            Arguments.of(
                Map.of(),
                Set.of(AlertFieldStatus.error(MsTeamsDescriptor.KEY_WEBHOOK, ConfigurationFieldValidator.REQUIRED_FIELD_MISSING_MESSAGE))
            ),
            Arguments.of(
                Map.of(MsTeamsDescriptor.KEY_WEBHOOK, FieldModelUtils.createFieldValue(INVALID_WEBHOOK_URL)),
                Set.of(AlertFieldStatus.error(MsTeamsDescriptor.KEY_WEBHOOK, "no protocol: " + INVALID_WEBHOOK_URL))
            )
        );
    }

    @MethodSource("getFieldsAndExpectedErrors")
    @ParameterizedTest()
    public void testValidate(Map<String, FieldValueModel> fieldModelMap, Set<AlertFieldStatus> expectedValidationErrors) {
        JobFieldModel testJobFieldModel = FieldModelUtils.createJobFieldModel(ValidationConstants.COMMON_CHANNEL_FIELDS, fieldModelMap);

        CommonChannelDistributionValidator commonChannelDistributionValidator = new CommonChannelDistributionValidator();
        MsTeamsDistributionConfigurationValidator msTeamsDistributionConfigurationValidator = new MsTeamsDistributionConfigurationValidator(commonChannelDistributionValidator);

        Set<AlertFieldStatus> validationErrors = msTeamsDistributionConfigurationValidator.validate(testJobFieldModel);

        Assertions.assertEquals(expectedValidationErrors, validationErrors);
    }
}
