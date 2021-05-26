package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.mock.MockCustomCertificateRepository;
import com.synopsys.integration.alert.database.certificates.CustomCertificateRepository;

public class DefaultCustomCertificateAccessorTest {
    private final String alias = "alias-test";
    private final String content = "content-test";
    private final OffsetDateTime testDate = DateUtils.createCurrentDateTimestamp();

    private final CustomCertificateModel expectedCustomCertificateModel = new CustomCertificateModel(alias, content, DateUtils.formatDate(testDate, DateUtils.UTC_DATE_FORMAT_TO_MINUTE));

    @Test
    public void getCertificatesTest() {
        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository(alias, content, testDate);

        DefaultCustomCertificateAccessor customCertificateAccessor = new DefaultCustomCertificateAccessor(customCertificateRepository);
        List<CustomCertificateModel> customCertificateModelList = customCertificateAccessor.getCertificates();

        assertEquals(1, customCertificateModelList.size());
        CustomCertificateModel customCertificateModel = customCertificateModelList.get(0);
        testCustomCertificateModel(expectedCustomCertificateModel, customCertificateModel);
    }

    @Test
    public void getCertificateTest() {
        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository(alias, content, testDate);

        DefaultCustomCertificateAccessor customCertificateAccessor = new DefaultCustomCertificateAccessor(customCertificateRepository);
        Optional<CustomCertificateModel> customCertificateModelOptional = customCertificateAccessor.getCertificate(0L);
        Optional<CustomCertificateModel> customCertificateModelOptionalEmpty = customCertificateAccessor.getCertificate(9L);

        assertTrue(customCertificateModelOptional.isPresent());
        assertFalse(customCertificateModelOptionalEmpty.isPresent());
        CustomCertificateModel customCertificateModel = customCertificateModelOptional.get();
        testCustomCertificateModel(expectedCustomCertificateModel, customCertificateModel);
    }

    @Test
    public void storeCertificateTest() throws Exception {
        CustomCertificateModel certificateModel = new CustomCertificateModel(alias, content, testDate.toString());

        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository();

        DefaultCustomCertificateAccessor customCertificateAccessor = new DefaultCustomCertificateAccessor(customCertificateRepository);
        CustomCertificateModel customCertificateModel = customCertificateAccessor.storeCertificate(certificateModel);

        testCustomCertificateModel(expectedCustomCertificateModel, customCertificateModel);
    }

    @Test
    public void storeCertificateIdDoesNotExistTest() {
        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository();
        DefaultCustomCertificateAccessor customCertificateAccessor = new DefaultCustomCertificateAccessor(customCertificateRepository);

        try {
            CustomCertificateModel certificateModel = new CustomCertificateModel(9L, alias, content, testDate.toString());
            customCertificateAccessor.storeCertificate(certificateModel);
            fail();
        } catch (AlertConfigurationException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void deleteCertificateByAliasTest() {
        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository(alias, content, testDate);
        DefaultCustomCertificateAccessor customCertificateAccessor = new DefaultCustomCertificateAccessor(customCertificateRepository);
        List<CustomCertificateModel> customCertificateModelList = customCertificateAccessor.getCertificates();

        assertEquals(1, customCertificateModelList.size());
        CustomCertificateModel customCertificateModel = customCertificateModelList.get(0);
        testCustomCertificateModel(expectedCustomCertificateModel, customCertificateModel);

        customCertificateAccessor.deleteCertificate(alias);
        customCertificateModelList = customCertificateAccessor.getCertificates();

        assertTrue(customCertificateModelList.isEmpty());
    }

    @Test
    public void deleteCertificateByIdTest() {
        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository(alias, content, testDate);
        DefaultCustomCertificateAccessor customCertificateAccessor = new DefaultCustomCertificateAccessor(customCertificateRepository);
        List<CustomCertificateModel> customCertificateModelList = customCertificateAccessor.getCertificates();

        assertEquals(1, customCertificateModelList.size());
        CustomCertificateModel customCertificateModel = customCertificateModelList.get(0);
        testCustomCertificateModel(expectedCustomCertificateModel, customCertificateModel);

        customCertificateAccessor.deleteCertificate(0L);
        customCertificateModelList = customCertificateAccessor.getCertificates();

        assertTrue(customCertificateModelList.isEmpty());
    }

    private void testCustomCertificateModel(CustomCertificateModel expected, CustomCertificateModel actual) {
        assertEquals(expected.getAlias(), actual.getAlias());
        assertEquals(expected.getCertificateContent(), actual.getCertificateContent());
        assertEquals(expected.getLastUpdated(), actual.getLastUpdated());
    }

}
