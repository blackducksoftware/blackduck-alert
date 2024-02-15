package com.synopsys.integration.alert.component.certificates.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;

@ExtendWith(SpringExtension.class)
class ClientCertificateConfigurationValidatorTest {
    private ClientCertificateConfigurationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ClientCertificateConfigurationValidator();
    }

    @Test
    void validate() {
        ValidationResponseModel responseModel = validator.validate(new ClientCertificateModel("key_password", "key_content", "certificate_content"));
        Collection<AlertFieldStatus> alertFieldStatuses = responseModel.getErrors().values();
        assertEquals(0, alertFieldStatuses.size());
    }

    @Test
    void validateEmptyKeyPassword() {
        ValidationResponseModel responseModel = validator.validate(new ClientCertificateModel(null, "key_content", "certificate_content"));
        Collection<AlertFieldStatus> alertFieldStatuses = responseModel.getErrors().values();
        assertEquals(1, alertFieldStatuses.size());

        ValidationResponseModel responseModel1 = validator.validate(new ClientCertificateModel("", "key_content", "certificate_content"));
        Collection<AlertFieldStatus> alertFieldStatuses1 = responseModel1.getErrors().values();
        assertEquals(1, alertFieldStatuses1.size());
    }

    @Test
    void validateEmptyKeyContent() {
        ValidationResponseModel responseModel = validator.validate(new ClientCertificateModel("key_password", null, "certificate_content"));
        Collection<AlertFieldStatus> alertFieldStatuses = responseModel.getErrors().values();
        assertEquals(1, alertFieldStatuses.size());

        ValidationResponseModel responseModel1 = validator.validate(new ClientCertificateModel("key_password", "", "certificate_content"));
        Collection<AlertFieldStatus> alertFieldStatuses1 = responseModel1.getErrors().values();
        assertEquals(1, alertFieldStatuses1.size());
    }

    @Test
    void validateEmptyCertificateContent() {
        ValidationResponseModel responseModel = validator.validate(new ClientCertificateModel("key_password", "key_content", null));
        Collection<AlertFieldStatus> alertFieldStatuses = responseModel.getErrors().values();
        assertEquals(1, alertFieldStatuses.size());

        ValidationResponseModel responseModel1 = validator.validate(new ClientCertificateModel("key_password", "key_content", ""));
        Collection<AlertFieldStatus> alertFieldStatuses1 = responseModel1.getErrors().values();
        assertEquals(1, alertFieldStatuses1.size());
    }

    @Test
    void validateEmptyAllFields() {
        ValidationResponseModel responseModel = validator.validate(new ClientCertificateModel(null, null, null));
        Collection<AlertFieldStatus> alertFieldStatuses = responseModel.getErrors().values();
        assertEquals(3, alertFieldStatuses.size());

        ValidationResponseModel responseModel1 = validator.validate(new ClientCertificateModel("", "", ""));
        Collection<AlertFieldStatus> alertFieldStatuses1 = responseModel1.getErrors().values();
        assertEquals(3, alertFieldStatuses1.size());
    }
}
