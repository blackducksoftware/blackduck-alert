package com.synopsys.integration.alert.channel.email.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.CommonChannelDistributionValidator;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;

class EmailDistributionConfigurationValidatorTest {
    static final Set<AlertFieldStatus> REQUIRED_FIELDS_ERRORS = Set.of(
        AlertFieldStatus.error(ChannelDescriptor.KEY_CHANNEL_NAME, ConfigurationFieldValidator.REQUIRED_FIELD_MISSING_MESSAGE),
        AlertFieldStatus.error(ChannelDescriptor.KEY_NAME, ConfigurationFieldValidator.REQUIRED_FIELD_MISSING_MESSAGE),
        AlertFieldStatus.error(ChannelDescriptor.KEY_FREQUENCY, ConfigurationFieldValidator.REQUIRED_FIELD_MISSING_MESSAGE),
        AlertFieldStatus.error(ChannelDescriptor.KEY_PROVIDER_TYPE, ConfigurationFieldValidator.REQUIRED_FIELD_MISSING_MESSAGE)
    );

    static final AlertFieldStatus KEY_FREQUENCY_INVALID_OPTION_ERROR = AlertFieldStatus.error(
        ChannelDescriptor.KEY_FREQUENCY,
        ConfigurationFieldValidator.INVALID_OPTION_MESSAGE
    );

    static final AlertFieldStatus KEY_EMAIL_ADDITIONAL_ADDRESSES_ERROR = AlertFieldStatus.error(
        EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES,
        String.format("Must be set if %s is set", EmailDescriptor.LABEL_ADDITIONAL_ADDRESSES)
    );

    static final List<String> VALID_FREQUENCY_TYPES = Arrays.stream(FrequencyType.values())
        .map(FrequencyType::name)
        .collect(Collectors.toList());

    @Test
    void requiredFieldsPresentTest() {
        Map<String, FieldValueModel> keyToValues = getRequiredKeyToVales(VALID_FREQUENCY_TYPES);
        JobFieldModel jobFieldModel = createJobFieldModel(keyToValues);
        Set<AlertFieldStatus> validate = getValidateData(jobFieldModel);

        REQUIRED_FIELDS_ERRORS.forEach(error ->
            assertFalse(validate.contains(error), String.format("Should not contain error for: %s", error.getFieldName())
            ));
        assertFalse(validate.contains(KEY_FREQUENCY_INVALID_OPTION_ERROR));
        assertEquals(0, validate.size());
    }

    @Test
    void requiredFieldsNotPresentTest() {
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put("test", new FieldValueModel(VALID_FREQUENCY_TYPES, true));

        JobFieldModel jobFieldModel = createJobFieldModel(keyToValues);
        Set<AlertFieldStatus> validate = getValidateData(jobFieldModel);

        REQUIRED_FIELDS_ERRORS.forEach(error ->
            assertTrue(validate.contains(error), String.format("Missing required error: %s", error.getFieldName())
            ));
        assertEquals(4, validate.size());
    }

    @Test
    void invalidKeyFrequencyOptionsTest() {
        List<String> values = new ArrayList<>(VALID_FREQUENCY_TYPES);
        values.add("testing");

        Map<String, FieldValueModel> keyToValues = getRequiredKeyToVales(values);
        JobFieldModel jobFieldModel = createJobFieldModel(keyToValues);
        Set<AlertFieldStatus> validate = getValidateData(jobFieldModel);

        assertTrue(validate.contains(KEY_FREQUENCY_INVALID_OPTION_ERROR));
        assertEquals(1, validate.size());
    }

    @Test
    void additionalEmailsOnlyTrueErrorTest() {
        Map<String, FieldValueModel> keyToValues = getRequiredKeyToVales(VALID_FREQUENCY_TYPES);
        keyToValues.put(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY, new FieldValueModel(new HashSet<>(List.of("true")), true));

        JobFieldModel jobFieldModel = createJobFieldModel(keyToValues);
        Set<AlertFieldStatus> validate = getValidateData(jobFieldModel);

        assertTrue(validate.contains(KEY_EMAIL_ADDITIONAL_ADDRESSES_ERROR));
        assertEquals(1, validate.size());
    }

    @Test
    void additionalEmailsOnlyTrueNoErrorTest() {
        Map<String, FieldValueModel> keyToValues = getRequiredKeyToVales(VALID_FREQUENCY_TYPES);
        keyToValues.put(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY, new FieldValueModel(new HashSet<>(List.of("true")), true));
        keyToValues.put(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, new FieldValueModel(new HashSet<>(List.of("true")), true));
        keyToValues.put(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, new FieldValueModel(new HashSet<>(List.of("true")), true));

        JobFieldModel jobFieldModel = createJobFieldModel(keyToValues);
        Set<AlertFieldStatus> validate = getValidateData(jobFieldModel);

        assertFalse(validate.contains(KEY_EMAIL_ADDITIONAL_ADDRESSES_ERROR));
        assertEquals(0, validate.size());
    }

    @Test
    void projectOwnerOnlyAlsoTrueTest() {
        Set<AlertFieldStatus> expectedValidationErrors = new HashSet<>(List.of(
            AlertFieldStatus.error(
                EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY,
                String.format("Cannot be set if %s is already set", EmailDescriptor.LABEL_PROJECT_OWNER_ONLY)
            ),
            AlertFieldStatus.error(EmailDescriptor.KEY_PROJECT_OWNER_ONLY, String.format("Cannot be set if %s is already set", EmailDescriptor.LABEL_ADDITIONAL_ADDRESSES_ONLY))
        ));

        Map<String, FieldValueModel> keyToValues = getRequiredKeyToVales(VALID_FREQUENCY_TYPES);
        keyToValues.put(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY, new FieldValueModel(new HashSet<>(List.of("true")), true));
        keyToValues.put(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, new FieldValueModel(new HashSet<>(List.of("true")), true));
        keyToValues.put(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, new FieldValueModel(new HashSet<>(List.of("true")), true));
        keyToValues.put(EmailDescriptor.KEY_PROJECT_OWNER_ONLY, new FieldValueModel(new HashSet<>(List.of("true")), true));

        JobFieldModel jobFieldModel = createJobFieldModel(keyToValues);
        Set<AlertFieldStatus> validate = getValidateData(jobFieldModel);

        expectedValidationErrors.forEach(error ->
            assertTrue(validate.contains(error), String.format("Missing required error: %s", error.getFieldName())
            ));
        assertFalse(validate.contains(KEY_EMAIL_ADDITIONAL_ADDRESSES_ERROR));
        assertEquals(2, validate.size());
    }

    private JobFieldModel createJobFieldModel(Map<String, FieldValueModel> keyToValues) {
        FieldModel fieldModel = new FieldModel("test-configId", "test-descriptorName", "test-context", "test-createdAt", "test-lastUpdated", keyToValues);
        Set<FieldModel> fieldModels = new HashSet<>();
        fieldModels.add(fieldModel);

        return new JobFieldModel("test-jobId", fieldModels, null);
    }

    private Map<String, FieldValueModel> getRequiredKeyToVales(List<String> values) {
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(ChannelDescriptor.KEY_CHANNEL_NAME, new FieldValueModel(values, true));
        keyToValues.put(ChannelDescriptor.KEY_NAME, new FieldValueModel(values, true));
        keyToValues.put(ChannelDescriptor.KEY_FREQUENCY, new FieldValueModel(values, true));
        keyToValues.put(ChannelDescriptor.KEY_PROVIDER_TYPE, new FieldValueModel(values, true));
        return keyToValues;
    }

    private Set<AlertFieldStatus> getValidateData(JobFieldModel jobFieldModel) {
        CommonChannelDistributionValidator commonChannelDistributionValidator = new CommonChannelDistributionValidator();
        EmailDistributionConfigurationValidator emailDistributionConfigurationValidator = new EmailDistributionConfigurationValidator(commonChannelDistributionValidator);
        return emailDistributionConfigurationValidator.validate(jobFieldModel);
    }
}
