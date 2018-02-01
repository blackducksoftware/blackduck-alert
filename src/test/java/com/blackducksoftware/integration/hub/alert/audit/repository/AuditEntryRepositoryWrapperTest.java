package com.blackducksoftware.integration.hub.alert.audit.repository;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.alert.OutputLogger;
import com.blackducksoftware.integration.hub.alert.audit.mock.MockAuditEntryEntity;

public class AuditEntryRepositoryWrapperTest {
    private OutputLogger outputLogger;

    @Before
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @After
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    @Test
    public void testFindFirstByCommonConfigIdOrderByTimeLastSentDesc() throws IOException {
        final MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        final AuditEntryRepository repository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(repository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(Mockito.anyLong())).thenReturn(mockAuditEntryEntity.createEntity());
        final AuditEntryRepositoryWrapper auditEntryRepositoryWrapper = new AuditEntryRepositoryWrapper(repository) {

            @Override
            public AuditEntryEntity decryptSensitiveData(final AuditEntryEntity entity) throws EncryptionException {
                throw new EncryptionException();
            }
        };

        final AuditEntryEntity actual = auditEntryRepositoryWrapper.findFirstByCommonConfigIdOrderByTimeLastSentDesc(1L);

        assertNull(actual);
        assertTrue(outputLogger.isLineContainingText("Error finding common distribution config"));
    }
}
