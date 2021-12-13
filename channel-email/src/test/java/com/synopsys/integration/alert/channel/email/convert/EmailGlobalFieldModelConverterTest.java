package com.synopsys.integration.alert.channel.email.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

public class EmailGlobalFieldModelConverterTest {

    public static final String TEST_AUTH_REQUIRED = "true";
    public static final String TEST_FROM = "test.user@some.company.example.com";
    public static final String TEST_SMTP_HOST = "stmp.server.example.com";
    public static final String TEST_AUTH_PASSWORD = "apassword";
    public static final String TEST_SMTP_PORT = "2025";
    public static final String TEST_AUTH_USER = "auser";
    public static final String TEST_ADDITIONAL_PROPERTY = "mail.smtp.ehlo";

    @Test
    public void validConversionTest() {
        FieldModel fieldModel = createDefaultFieldModel();
        EmailGlobalFieldModelConverter converter = new EmailGlobalFieldModelConverter();
        Optional<EmailGlobalConfigModel> model = converter.convert(fieldModel);
        assertTrue(model.isPresent());
        EmailGlobalConfigModel emailModel = model.get();
        assertEquals(Boolean.TRUE, emailModel.getSmtpAuth().orElse(Boolean.FALSE));
        assertEquals(TEST_AUTH_USER, emailModel.getSmtpUsername().orElse(null));
        assertEquals(TEST_AUTH_PASSWORD, emailModel.getSmtpPassword().orElse(null));
        assertEquals(TEST_SMTP_HOST, emailModel.getSmtpHost().orElse(null));
        assertEquals(Integer.valueOf(TEST_SMTP_PORT), emailModel.getSmtpPort().orElse(null));
        assertEquals(TEST_FROM, emailModel.getSmtpFrom().orElse(null));

        Map<String, String> additionalProperties = emailModel.getAdditionalJavaMailProperties().orElse(Map.of());
        assertEquals(1, additionalProperties.size());
        assertEquals("true", additionalProperties.get(TEST_ADDITIONAL_PROPERTY));

    }

    @Test
    public void invalidPortTest() {
        FieldModel fieldModel = createDefaultFieldModel();
        fieldModel.putField(EmailGlobalFieldModelConverter.EMAIL_PORT_KEY, new FieldValueModel(List.of("twenty-five"), false));
        EmailGlobalFieldModelConverter converter = new EmailGlobalFieldModelConverter();
        Optional<EmailGlobalConfigModel> model = converter.convert(fieldModel);
        assertTrue(model.isEmpty());
    }

    @Test
    public void emptyFieldsTest() {
        FieldModel emptyModel = new FieldModel(ChannelKeys.EMAIL.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        EmailGlobalFieldModelConverter converter = new EmailGlobalFieldModelConverter();
        Optional<EmailGlobalConfigModel> model = converter.convert(emptyModel);
        assertTrue(model.isPresent());
        EmailGlobalConfigModel emailModel = model.get();
        assertTrue(emailModel.getSmtpAuth().isEmpty());
        assertTrue(emailModel.getSmtpUsername().isEmpty());
        assertTrue(emailModel.getSmtpPassword().isEmpty());
        assertTrue(emailModel.getSmtpHost().isEmpty());
        assertTrue(emailModel.getSmtpPort().isEmpty());
        assertTrue(emailModel.getSmtpFrom().isEmpty());
        assertTrue(emailModel.getAdditionalJavaMailProperties().isPresent());
        assertTrue(emailModel.getAdditionalJavaMailProperties().orElseThrow(() -> new AssertionError("Expected an additional properties map.")).isEmpty());
    }

    @Test
    public void invalidEmailPropertyKeysTest() {
        FieldModel fieldModel = createDefaultFieldModel();
        fieldModel.removeField(TEST_ADDITIONAL_PROPERTY);
        fieldModel.putField("invalid.email.field", new FieldValueModel(List.of(), false));
        EmailGlobalFieldModelConverter converter = new EmailGlobalFieldModelConverter();
        Optional<EmailGlobalConfigModel> model = converter.convert(fieldModel);
        assertTrue(model.isPresent());
        EmailGlobalConfigModel emailModel = model.get();
        Map<String, String> additionalProperties = emailModel.getAdditionalJavaMailProperties().orElse(Map.of());
        assertEquals(0, additionalProperties.size());

    }

    private FieldModel createDefaultFieldModel() {
        Map<String, FieldValueModel> fieldValuesMap = new HashMap<>();
        fieldValuesMap.put(EmailGlobalFieldModelConverter.EMAIL_FROM_KEY, new FieldValueModel(List.of(TEST_FROM), false));
        fieldValuesMap.put(EmailGlobalFieldModelConverter.EMAIL_HOST_KEY, new FieldValueModel(List.of(TEST_SMTP_HOST), false));
        fieldValuesMap.put(EmailGlobalFieldModelConverter.EMAIL_PORT_KEY, new FieldValueModel(List.of(TEST_SMTP_PORT), false));
        fieldValuesMap.put(EmailGlobalFieldModelConverter.AUTH_REQUIRED_KEY, new FieldValueModel(List.of(TEST_AUTH_REQUIRED), false));
        fieldValuesMap.put(EmailGlobalFieldModelConverter.AUTH_PASSWORD_KEY, new FieldValueModel(List.of(TEST_AUTH_PASSWORD), false));
        fieldValuesMap.put(EmailGlobalFieldModelConverter.AUTH_USER_KEY, new FieldValueModel(List.of(TEST_AUTH_USER), false));

        fieldValuesMap.put(TEST_ADDITIONAL_PROPERTY, new FieldValueModel(List.of("true"), false));
        return new FieldModel(ChannelKeys.EMAIL.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), fieldValuesMap);
    }

}
