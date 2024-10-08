package com.blackduck.integration.alert.component.certificates.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blackduck.integration.alert.component.certificates.web.ClientCertificateConfigurationValidator;
import com.synopsys.integration.alert.api.certificates.AlertClientCertificateManager;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;

@ExtendWith(SpringExtension.class)
class ClientCertificateConfigurationValidatorTest {
    private ClientCertificateConfigurationValidator validator;

    @Mock
    private AlertClientCertificateManager alertClientCertificateManager;

    @BeforeEach
    void setUp() {
        validator = new ClientCertificateConfigurationValidator(alertClientCertificateManager);
    }

    @Test
    void validate() {
        Mockito.when(alertClientCertificateManager.validateCertificate(Mockito.any())).thenReturn(true);
        ValidationResponseModel responseModel = validator.validate(new ClientCertificateModel("key_password", "key_content", "certificate_content"));
        assertFalse(responseModel.hasErrors());
        Collection<AlertFieldStatus> alertFieldStatuses = responseModel.getErrors().values();
        assertEquals(0, alertFieldStatuses.size());
    }

    @Test
    void validateEmptyKeyPassword() {
        Mockito.when(alertClientCertificateManager.validateCertificate(Mockito.any())).thenReturn(false);
        ValidationResponseModel responseModel = validator.validate(new ClientCertificateModel(null, "key_content", "certificate_content"));
        assertTrue(responseModel.hasErrors());
        Collection<AlertFieldStatus> alertFieldStatuses = responseModel.getErrors().values();
        assertEquals(1, alertFieldStatuses.size());

        ValidationResponseModel responseModel1 = validator.validate(new ClientCertificateModel("", "key_content", "certificate_content"));
        Collection<AlertFieldStatus> alertFieldStatuses1 = responseModel1.getErrors().values();
        assertEquals(1, alertFieldStatuses1.size());
    }

    @Test
    void validateEmptyKeyContent() {
        Mockito.when(alertClientCertificateManager.validateCertificate(Mockito.any())).thenReturn(false);
        ValidationResponseModel responseModel = validator.validate(new ClientCertificateModel("key_password", null, "certificate_content"));
        assertTrue(responseModel.hasErrors());
        Collection<AlertFieldStatus> alertFieldStatuses = responseModel.getErrors().values();
        assertEquals(1, alertFieldStatuses.size());

        ValidationResponseModel responseModel1 = validator.validate(new ClientCertificateModel("key_password", "", "certificate_content"));
        Collection<AlertFieldStatus> alertFieldStatuses1 = responseModel1.getErrors().values();
        assertEquals(1, alertFieldStatuses1.size());
    }

    @Test
    void validateEmptyCertificateContent() {
        Mockito.when(alertClientCertificateManager.validateCertificate(Mockito.any())).thenReturn(false);
        ValidationResponseModel responseModel = validator.validate(new ClientCertificateModel("key_password", "key_content", null));
        assertTrue(responseModel.hasErrors());
        Collection<AlertFieldStatus> alertFieldStatuses = responseModel.getErrors().values();
        assertEquals(1, alertFieldStatuses.size());

        ValidationResponseModel responseModel1 = validator.validate(new ClientCertificateModel("key_password", "key_content", ""));
        assertTrue(responseModel.hasErrors());
        Collection<AlertFieldStatus> alertFieldStatuses1 = responseModel1.getErrors().values();
        assertEquals(1, alertFieldStatuses1.size());
    }

    @Test
    void validateEmptyAllFields() {
        Mockito.when(alertClientCertificateManager.validateCertificate(Mockito.any())).thenReturn(false);
        ValidationResponseModel responseModel = validator.validate(new ClientCertificateModel(null, null, null));
        assertTrue(responseModel.hasErrors());
        Collection<AlertFieldStatus> alertFieldStatuses = responseModel.getErrors().values();
        assertEquals(3, alertFieldStatuses.size());

        ValidationResponseModel responseModel1 = validator.validate(new ClientCertificateModel("", "", ""));
        Collection<AlertFieldStatus> alertFieldStatuses1 = responseModel1.getErrors().values();
        assertEquals(3, alertFieldStatuses1.size());
    }

    @Test
    void validateInvalidCertificate() {
        Mockito.when(alertClientCertificateManager.validateCertificate(Mockito.any())).thenReturn(false);
        ValidationResponseModel responseModel = validator.validate(new ClientCertificateModel("key_password", "key_content", "certificate_content"));
        assertTrue(responseModel.hasErrors());
        Collection<AlertFieldStatus> alertFieldStatuses = responseModel.getErrors().values();
        assertEquals(0, alertFieldStatuses.size());
    }
}
