package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;
import com.synopsys.integration.alert.database.api.mock.MockCustomCertificateRepository;
import com.synopsys.integration.alert.database.certificates.CustomCertificateRepository;

public class DefaultCustomCertificateAccessorTest {
    private final String alias = "alias-test";
    private final String content = "content-test";

    private CustomCertificateModel expectedCustomCertificateModel = new CustomCertificateModel(alias, content, new Date().toString());

    @Test
    public void getCertificatesTest() {
        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository(alias, content, new Date());

        DefaultCustomCertificateAccessor customCertificateAccessor = new DefaultCustomCertificateAccessor(customCertificateRepository);
        List<CustomCertificateModel> customCertificateModelList = customCertificateAccessor.getCertificates();

        assertEquals(1, customCertificateModelList.size());
        CustomCertificateModel customCertificateModel = customCertificateModelList.get(0);
        testCustomCertificateModel(expectedCustomCertificateModel, customCertificateModel);
    }

    @Test
    public void getCertificateTest() {
        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository(alias, content, new Date());

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
        CustomCertificateModel certificateModel = new CustomCertificateModel(alias, content, new Date().toString());

        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository();

        DefaultCustomCertificateAccessor customCertificateAccessor = new DefaultCustomCertificateAccessor(customCertificateRepository);
        CustomCertificateModel customCertificateModel = customCertificateAccessor.storeCertificate(certificateModel);

        testCustomCertificateModel(expectedCustomCertificateModel, customCertificateModel);
    }

    @Test
    public void storeCertificateNullTest() throws Exception {
        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository();
        DefaultCustomCertificateAccessor customCertificateAccessor = new DefaultCustomCertificateAccessor(customCertificateRepository);
        try {
            customCertificateAccessor.storeCertificate(null);
            fail();
        } catch (AlertDatabaseConstraintException e) {
        }
    }

    @Test
    public void storeCertificateBlankValuesTest() throws Exception {
        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository();
        DefaultCustomCertificateAccessor customCertificateAccessor = new DefaultCustomCertificateAccessor(customCertificateRepository);

        try {
            CustomCertificateModel certificateModel = new CustomCertificateModel("", content, new Date().toString());
            customCertificateAccessor.storeCertificate(certificateModel);
            fail();
        } catch (AlertDatabaseConstraintException e) {
        }

        try {
            CustomCertificateModel certificateModel = new CustomCertificateModel(alias, "", new Date().toString());
            customCertificateAccessor.storeCertificate(certificateModel);
            fail();
        } catch (AlertDatabaseConstraintException e) {
        }
    }

    @Test
    public void storeCertificateIdDoesNotExistTest() throws Exception {
        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository();
        DefaultCustomCertificateAccessor customCertificateAccessor = new DefaultCustomCertificateAccessor(customCertificateRepository);

        try {
            CustomCertificateModel certificateModel = new CustomCertificateModel(9L, alias, content, new Date().toString());
            customCertificateAccessor.storeCertificate(certificateModel);
            fail();
        } catch (AlertDatabaseConstraintException e) {
        }
    }

    @Test
    public void deleteCertificateByAliasTest() throws Exception {
        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository(alias, content, new Date());
        DefaultCustomCertificateAccessor customCertificateAccessor = new DefaultCustomCertificateAccessor(customCertificateRepository);
        List<CustomCertificateModel> customCertificateModelList = customCertificateAccessor.getCertificates();

        assertEquals(1, customCertificateModelList.size());
        CustomCertificateModel customCertificateModel = customCertificateModelList.get(0);
        testCustomCertificateModel(expectedCustomCertificateModel, customCertificateModel);

        customCertificateAccessor.deleteCertificate(alias);
        customCertificateModelList = customCertificateAccessor.getCertificates();

        assertEquals(0, customCertificateModelList.size());
    }

    @Test
    public void deleteCertificateByAliasBlankTest() throws Exception {
        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository(alias, content, new Date());
        DefaultCustomCertificateAccessor customCertificateAccessor = new DefaultCustomCertificateAccessor(customCertificateRepository);

        try {
            customCertificateAccessor.deleteCertificate("");
            fail();
        } catch (AlertDatabaseConstraintException e) {
        }
    }

    @Test
    public void deleteCertificateByIdTest() throws Exception {
        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository(alias, content, new Date());
        DefaultCustomCertificateAccessor customCertificateAccessor = new DefaultCustomCertificateAccessor(customCertificateRepository);
        List<CustomCertificateModel> customCertificateModelList = customCertificateAccessor.getCertificates();

        assertEquals(1, customCertificateModelList.size());
        CustomCertificateModel customCertificateModel = customCertificateModelList.get(0);
        testCustomCertificateModel(expectedCustomCertificateModel, customCertificateModel);

        customCertificateAccessor.deleteCertificate(0L);
        customCertificateModelList = customCertificateAccessor.getCertificates();

        assertEquals(0, customCertificateModelList.size());
    }

    @Test
    public void deleteCertificateByIdNullTest() throws Exception {
        CustomCertificateRepository customCertificateRepository = new MockCustomCertificateRepository(alias, content, new Date());
        DefaultCustomCertificateAccessor customCertificateAccessor = new DefaultCustomCertificateAccessor(customCertificateRepository);

        try {
            Long certificateId = null;
            customCertificateAccessor.deleteCertificate(certificateId);
            fail();
        } catch (AlertDatabaseConstraintException e) {
        }

        try {
            customCertificateAccessor.deleteCertificate(-1L);
            fail();
        } catch (AlertDatabaseConstraintException e) {
        }
    }

    private void testCustomCertificateModel(CustomCertificateModel expected, CustomCertificateModel actual) {
        assertEquals(expected.getAlias(), actual.getAlias());
        assertEquals(expected.getCertificateContent(), actual.getCertificateContent());
    }
}
