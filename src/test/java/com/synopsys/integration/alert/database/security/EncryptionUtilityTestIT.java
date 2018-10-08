package com.synopsys.integration.alert.database.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import com.synopsys.integration.alert.common.AlertProperties;
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
    private static final String TEST_PASSWORD = "testPassword";
    @Autowired
    public SaltMappingRepository repository;

    @Test
    public void testEncryption() {
        final AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.of(TEST_PASSWORD));
        final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, repository);

        final String propertyKey = "propertyKey";
        final String sensitiveValue = "sensitiveDataText";

        final String encryptedValue = encryptionUtility.encrypt(propertyKey, sensitiveValue);
        assertNotEquals(sensitiveValue, encryptedValue);
    }

    @Test
    public void testDecryption() {
        final AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.of(TEST_PASSWORD));
        final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, repository);

        final String propertyKey = "propertyKey";
        final String sensitiveValue = "sensitiveDataText";

        final String encryptedValue = encryptionUtility.encrypt(propertyKey, sensitiveValue);
        final Optional<String> decryptedValue = encryptionUtility.decrypt(propertyKey, encryptedValue);
        assertTrue(decryptedValue.isPresent());
        assertEquals(sensitiveValue, decryptedValue.get());
    }

    @Test
    public void testUnknownPropertyDecryption() {
        final AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.of(TEST_PASSWORD));
        final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, repository);

        final String propertyKey = "propertyKey";
        final String sensitiveValue = "sensitiveDataText";
        final String unknownPropertyKey = "unknownProperty";
        final String encryptedValue = encryptionUtility.encrypt(propertyKey, sensitiveValue);
        final Optional<String> decryptedValue = encryptionUtility.decrypt(unknownPropertyKey, encryptedValue);
        assertFalse(decryptedValue.isPresent());
    }

    @Test
    public void testNullPasswordEncryption() {
        try {
            final AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
            Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.empty());
            final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, repository);
            encryptionUtility.encrypt("propertyKey", "sensitiveValue");
            fail();
        } catch (final NullPointerException ex) {
        }
    }

    @Test
    public void testNullPasswordDecryption() {
        try {
            final AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
            Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.empty());
            final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, repository);
            encryptionUtility.decrypt("propertyKey", "sensitiveValue");
            fail();
        } catch (final NullPointerException ex) {
        }
    }

    @Test
    public void testEncryptionNullValue() {
        final AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.of(TEST_PASSWORD));
        final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, repository);
        final String encryptedValue = encryptionUtility.encrypt("propertyKey", null);
        assertNull(encryptedValue);
    }

    @Test
    public void testEncryptionEmptyStringValue() {
        final AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.of(TEST_PASSWORD));
        final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, repository);
        final String propertyKey = "propertyKey";
        final String emptyValue = "";
        final String encryptedValue = encryptionUtility.encrypt(propertyKey, emptyValue);
        assertEquals(emptyValue, encryptedValue);
    }

    @Test
    public void testDecryptionNullValue() {
        final AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.of(TEST_PASSWORD));
        final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, repository);
        final Optional<String> decryptedValue = encryptionUtility.decrypt("propertyKey", null);
        assertFalse(decryptedValue.isPresent());
    }

    @Test
    public void testDecryptionEmptyStringValue() {
        final AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.of(TEST_PASSWORD));
        final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, repository);
        final String propertyKey = "propertyKey";
        final String emptyValue = "";
        final Optional<String> decryptedValue = encryptionUtility.decrypt(propertyKey, emptyValue);
        assertTrue(decryptedValue.isPresent());
        assertEquals(emptyValue, decryptedValue.get());
    }
}
