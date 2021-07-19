package com.synopsys.integration.alert.channel.msteams.validator;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.synopsys.integration.alert.api.channel.CommonChannelDistributionValidator;
import com.synopsys.integration.alert.channel.msteams.descriptor.MsTeamsDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.test.common.FieldModelUtils;

public class MsTeamsDistributionConfigurationValidatorTest {
    public static Stream<Arguments> getModelAndExpectedErrors() {
        Map<String, FieldValueModel> commonFields = Map.of(ChannelDistributionUIConfig.KEY_CHANNEL_NAME, FieldModelUtils.createFieldValue("Channel Name"),
            ChannelDistributionUIConfig.KEY_NAME, FieldModelUtils.createFieldValue("Name"),
            ChannelDistributionUIConfig.KEY_FREQUENCY, FieldModelUtils.createFieldValue(FrequencyType.REAL_TIME.toString()),
            ChannelDistributionUIConfig.KEY_PROVIDER_NAME, FieldModelUtils.createFieldValue("Provider Name")
        );

        return Stream.of(
            Arguments.of(
                FieldModelUtils.createJobFieldModel(
                    commonFields,
                    Map.of(MsTeamsDescriptor.KEY_WEBHOOK, FieldModelUtils.createFieldValue("https://www.example.com/webhook"))
                ),
                Set.of()
            ),
            Arguments.of(
                FieldModelUtils.createJobFieldModel(commonFields),
                Set.of(AlertFieldStatus.error(MsTeamsDescriptor.KEY_WEBHOOK, ConfigurationFieldValidator.REQUIRED_FIELD_MISSING_MESSAGE))
            ),
            Arguments.of(
                FieldModelUtils.createJobFieldModel(
                    commonFields,
                    Map.of(MsTeamsDescriptor.KEY_WEBHOOK, FieldModelUtils.createFieldValue("bad_url"))
                ),
                Set.of(AlertFieldStatus.error(MsTeamsDescriptor.KEY_WEBHOOK, "no protocol: bad_url"))
            )
        );
    }

    @MethodSource("getModelAndExpectedErrors")
    @ParameterizedTest()
    public void testValidate(JobFieldModel testJobFieldModel, Set<AlertFieldStatus> expectedValidationErrors) {
        CommonChannelDistributionValidator commonChannelDistributionValidator = new CommonChannelDistributionValidator();
        MsTeamsDistributionConfigurationValidator msTeamsDistributionConfigurationValidator = new MsTeamsDistributionConfigurationValidator(commonChannelDistributionValidator);

        Set<AlertFieldStatus> validationErrors = msTeamsDistributionConfigurationValidator.validate(testJobFieldModel);

        Assertions.assertEquals(expectedValidationErrors, validationErrors);
    }
}
