package com.synopsys.integration.alert.provider.blackduck.controller;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.database.api.ProviderDataAccessor;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectEntity;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckDataActions;

public class BlackDuckDataActionsTest {

    @Test
    public void testGetHubProjectsNoProjects() throws Exception {
        final ProviderDataAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();

        final BlackDuckDataActions blackDuckDataActions = new BlackDuckDataActions(blackDuckProjectRepositoryAccessor);
        final List<com.synopsys.integration.alert.common.persistence.model.ProviderProject> blackDuckProjects = blackDuckDataActions.getBlackDuckProjects();
        assertEquals(0, blackDuckProjects.size());
    }

    @Test
    public void testGetHubProjects() throws Exception {
        final ProviderDataAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();

        final String projectName = "projectName";
        final String description = "Description";
        final String href = "href";
        final String projectOwnerEmail = "projectOwner";
        final String provider = BlackDuckProvider.COMPONENT_NAME;

        blackDuckProjectRepositoryAccessor.saveEntity(new ProviderProjectEntity(projectName, description, href, projectOwnerEmail, provider));

        final BlackDuckDataActions blackDuckDataActions = new BlackDuckDataActions(blackDuckProjectRepositoryAccessor);
        final List<com.synopsys.integration.alert.common.persistence.model.ProviderProject> blackDuckProjects = blackDuckDataActions.getBlackDuckProjects();
        assertEquals(1, blackDuckProjects.size());
        final com.synopsys.integration.alert.common.persistence.model.ProviderProject blackDuckProject = blackDuckProjects.get(0);
        assertEquals(projectName, blackDuckProject.getName());
        assertEquals(description, blackDuckProject.getDescription());
        assertEquals(href, blackDuckProject.getHref());
        assertEquals(projectOwnerEmail, blackDuckProject.getProjectOwnerEmail());
    }
}
