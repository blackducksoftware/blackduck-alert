package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.PolarisIssueModel;
import com.synopsys.integration.alert.database.provider.polaris.issue.PolarisIssueEntity;
import com.synopsys.integration.alert.database.provider.polaris.issue.PolarisIssueRepository;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectEntity;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class PolarisIssueAccessorTestIT extends AlertIntegrationTest {
    @Autowired
    private ProviderProjectRepository providerProjectRepository;
    @Autowired
    private PolarisIssueRepository polarisIssueRepository;

    @BeforeEach
    public void init() {
        providerProjectRepository.deleteAllInBatch();
        polarisIssueRepository.deleteAllInBatch();
    }

    @Test
    public void getProjectIssuesHrefBlankTest() {
        getProjectIssuesThrowsExceptionTest(null);
        getProjectIssuesThrowsExceptionTest("");
    }

    @Test
    public void getProjectIssuesTest() throws AlertDatabaseConstraintException {
        final String href = "href";
        final String providerName = "provider name";
        final ProviderProjectEntity projectEntityToSave = new ProviderProjectEntity(null, null, href, null, providerName);
        final ProviderProjectEntity savedProjectEntity = providerProjectRepository.save(projectEntityToSave);

        final Integer previousCount = 0;
        final Integer newCount = 10;
        final String existingIssueType = "Existing issue type";
        final PolarisIssueEntity issueEntity = new PolarisIssueEntity(existingIssueType, previousCount, newCount, savedProjectEntity.getId());
        polarisIssueRepository.save(issueEntity);

        final PolarisIssueAccessor polarisIssueAccessor = new PolarisIssueAccessor(polarisIssueRepository, providerProjectRepository);
        final List<PolarisIssueModel> foundIssues = polarisIssueAccessor.getProjectIssues(href);
        assertEquals(1, foundIssues.size());
        final PolarisIssueModel foundModel = foundIssues.get(0);
        assertEquals(existingIssueType, foundModel.getIssueType());
        assertEquals(previousCount, foundModel.getPreviousIssueCount());
        assertEquals(newCount, foundModel.getCurrentIssueCount());
    }

    @Test
    public void updateIssueTypeBlankArgsTest() {
        assertUpdateIssueTypeThrowsException(null, "ignored", 1);
        assertUpdateIssueTypeThrowsException("", "ignored", 1);
        assertUpdateIssueTypeThrowsException("href", null, 1);
        assertUpdateIssueTypeThrowsException("href", "", 1);
        assertUpdateIssueTypeThrowsException("href", "issueType", null);
    }

    @Test
    public void updateIssueTypesTest() throws AlertDatabaseConstraintException {
        final String href = "href";
        final String providerName = "provider name";
        final ProviderProjectEntity projectEntityToSave = new ProviderProjectEntity("ignored name", "ignored desc", href, "ignored email", providerName);
        final ProviderProjectEntity savedProjectEntity = providerProjectRepository.save(projectEntityToSave);

        final String existingIssueType = "Existing issue type";
        final Integer startingIssueCount = 10;
        final PolarisIssueEntity issueEntity = new PolarisIssueEntity(existingIssueType, 0, startingIssueCount, savedProjectEntity.getId());
        polarisIssueRepository.save(issueEntity);

        final PolarisIssueAccessor polarisIssueAccessor = new PolarisIssueAccessor(polarisIssueRepository, providerProjectRepository);

        final String newIssueType = "New issue type";
        final Integer newIssueNewCount = 5;
        final PolarisIssueModel polarisIssueModel1 = polarisIssueAccessor.updateIssueType(href, newIssueType, newIssueNewCount);
        assertEquals(newIssueType, polarisIssueModel1.getIssueType());
        assertEquals(0, polarisIssueModel1.getPreviousIssueCount().intValue());
        assertEquals(newIssueNewCount, polarisIssueModel1.getCurrentIssueCount());

        final Integer existingIssueNewCount = 20;
        final PolarisIssueModel polarisIssueModel2 = polarisIssueAccessor.updateIssueType(href, existingIssueType, existingIssueNewCount);
        assertEquals(existingIssueType, polarisIssueModel2.getIssueType());
        assertEquals(startingIssueCount, polarisIssueModel2.getPreviousIssueCount());
        assertEquals(existingIssueNewCount, polarisIssueModel2.getCurrentIssueCount());
    }

    private void getProjectIssuesThrowsExceptionTest(final String projectHref) {
        try {
            final PolarisIssueAccessor polarisIssueAccessor = new PolarisIssueAccessor(null, null);
            polarisIssueAccessor.getProjectIssues(projectHref);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
        }
    }

    private void assertUpdateIssueTypeThrowsException(final String projectHref, final String issueType, final Integer newCount) {
        try {
            final PolarisIssueAccessor polarisIssueAccessor = new PolarisIssueAccessor(null, null);
            polarisIssueAccessor.updateIssueType(projectHref, issueType, newCount);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
        }
    }

}
