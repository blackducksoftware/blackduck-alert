package com.synopsys.integration.alert.database.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.security.repository.SaltMappingRepository;
import com.synopsys.integration.test.annotation.DatabaseConnectionTest;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class EncryptionUtilityTestIT {

    @Autowired
    public SaltMappingRepository repository;

    @Test
    public void testEncryption() {
        final EncryptionUtility encryptionUtility = new EncryptionUtility(repository);

        final String propertyKey = "propertyKey";
        final String sensitiveValue = "sensitiveDataText";

        final String encryptedValue = encryptionUtility.encrypt(propertyKey, sensitiveValue);
        assertNotEquals(sensitiveValue, encryptedValue);
    }

    @Test
    public void testDecryption() {
        final EncryptionUtility encryptionUtility = new EncryptionUtility(repository);

        final String propertyKey = "propertyKey";
        final String sensitiveValue = "sensitiveDataText";

        final String encryptedValue = encryptionUtility.encrypt(propertyKey, sensitiveValue);
        final Optional<String> decryptedValue = encryptionUtility.decrypt(propertyKey, encryptedValue);
        assertTrue(decryptedValue.isPresent());
        assertEquals(sensitiveValue, decryptedValue.get());
    }

    @Test
    public void testUnknownPropertyDecryption() {
        final EncryptionUtility encryptionUtility = new EncryptionUtility(repository);

        final String propertyKey = "propertyKey";
        final String sensitiveValue = "sensitiveDataText";
        final String unknownPropertyKey = "unknownProperty";
        final String encryptedValue = encryptionUtility.encrypt(propertyKey, sensitiveValue);
        final Optional<String> decryptedValue = encryptionUtility.decrypt(unknownPropertyKey, encryptedValue);
        assertFalse(decryptedValue.isPresent());
    }
}
