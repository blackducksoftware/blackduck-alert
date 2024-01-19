package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.ClientCertificateAccessor;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.mock.MockClientCertificateRepository;

class DefaultClientCertificateAccessorTest {
    private ClientCertificateAccessor clientCertificateAccessor;

    private ClientCertificateModel certificateModel;

    @BeforeEach
    void init() {
        MockClientCertificateRepository mockClientCertificateRepository = new MockClientCertificateRepository();
        clientCertificateAccessor = new DefaultClientCertificateAccessor(mockClientCertificateRepository);
        certificateModel = new ClientCertificateModel(null, "alias", UUID.randomUUID(), "certificate_content",
                DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
    }

    @Test
    void getCertificate() throws AlertConfigurationException {
        ClientCertificateModel savedModel = clientCertificateAccessor.saveCertificate(certificateModel);
        ClientCertificateModel queryModel = clientCertificateAccessor.getCertificate(savedModel.getId())
                .orElseThrow(() -> new AlertConfigurationException("Model does not exist."));

        assertEquals(savedModel.getId(), queryModel.getId());
    }

    @Test
    void getCertificates() throws AlertConfigurationException {
        ClientCertificateModel firstSavedModel = clientCertificateAccessor.saveCertificate(certificateModel);
        ClientCertificateModel secondSavedModel =
                clientCertificateAccessor.saveCertificate(new ClientCertificateModel(null, "alias1", UUID.randomUUID(), "certificate_content1",
                        DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)));

        List<ClientCertificateModel> certificateModelList = clientCertificateAccessor.getCertificates();
        assertEquals(2, certificateModelList.size());
        assertTrue(certificateModelList.stream().anyMatch(model -> model.getId().equals(firstSavedModel.getId())));
        assertTrue(certificateModelList.stream().anyMatch(model -> model.getId().equals(secondSavedModel.getId())));
    }

    @Test
    void saveCertificateUpdates() throws AlertConfigurationException {
        ClientCertificateModel savedModel = clientCertificateAccessor.saveCertificate(certificateModel);

        ClientCertificateModel changedModel = new ClientCertificateModel(savedModel.getId(), "new_alias", savedModel.getPrivateKeyId(),
                "new_certificate_content", DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        ClientCertificateModel updatedModel = clientCertificateAccessor.saveCertificate(changedModel);

        assertEquals(changedModel.getId(), updatedModel.getId());
        assertEquals(changedModel.getAlias(), updatedModel.getAlias());
        assertEquals(changedModel.getCertificateContent(), updatedModel.getCertificateContent());

        assertNotEquals(certificateModel.getAlias(), updatedModel.getAlias());
        assertNotEquals(certificateModel.getCertificateContent(), updatedModel.getCertificateContent());
    }

    @Test
    void saveCertificateUpdatesByAlias() throws AlertConfigurationException {
        ClientCertificateModel savedModel = clientCertificateAccessor.saveCertificate(certificateModel);
        // Null id here to search by alias
        ClientCertificateModel changedModel = new ClientCertificateModel(null, "new_alias", savedModel.getPrivateKeyId(),
                "new_certificate_content", DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        ClientCertificateModel updatedModel = clientCertificateAccessor.saveCertificate(changedModel);

        assertEquals(changedModel.getAlias(), updatedModel.getAlias());
        assertEquals(changedModel.getCertificateContent(), updatedModel.getCertificateContent());

        assertNotEquals(certificateModel.getAlias(), updatedModel.getAlias());
        assertNotEquals(certificateModel.getCertificateContent(), updatedModel.getCertificateContent());
    }

    @Test
    void saveCertificateThrowsOnNonexistentId() throws AlertConfigurationException {
        ClientCertificateModel savedModel = clientCertificateAccessor.saveCertificate(certificateModel);
        // A new random id here
        ClientCertificateModel changedModel = new ClientCertificateModel(UUID.randomUUID(), "new_alias", savedModel.getPrivateKeyId(),
                "new_certificate_content", DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        assertThrows(AlertConfigurationException.class, () -> clientCertificateAccessor.saveCertificate(changedModel));
    }

    @Test
    void deleteCertificateByAlias() throws AlertConfigurationException {
        ClientCertificateModel savedModel = clientCertificateAccessor.saveCertificate(certificateModel);
        clientCertificateAccessor.deleteCertificate(savedModel.getAlias());

        assertTrue(clientCertificateAccessor.getCertificate(savedModel.getId()).isEmpty());
    }

    @Test
    void deleteCertificateById() throws AlertConfigurationException {
        ClientCertificateModel savedModel = clientCertificateAccessor.saveCertificate(certificateModel);
        clientCertificateAccessor.deleteCertificate(savedModel.getId());

        assertTrue(clientCertificateAccessor.getCertificate(savedModel.getId()).isEmpty());
    }
}
