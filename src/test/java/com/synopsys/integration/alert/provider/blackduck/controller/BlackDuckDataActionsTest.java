package com.synopsys.integration.alert.provider.blackduck.controller;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.model.BlackDuckProject;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckDataActions;

public class BlackDuckDataActionsTest {

    @Test
    public void testGetHubProjectsNoProjects() throws Exception {
        final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();

        final BlackDuckDataActions blackDuckDataActions = new BlackDuckDataActions(blackDuckProjectRepositoryAccessor);
        final List<BlackDuckProject> blackDuckProjects = blackDuckDataActions.getBlackDuckProjects();
        assertEquals(0, blackDuckProjects.size());
    }

    @Test
    public void testGetHubProjects() throws Exception {
        final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();

        final String projectName = "projectName";
        final String description = "Description";
        final String href = "href";
        final String projectOwnerEmail = "projectOwner";

        blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity(projectName, description, href, projectOwnerEmail));

        final BlackDuckDataActions blackDuckDataActions = new BlackDuckDataActions(blackDuckProjectRepositoryAccessor);
        final List<BlackDuckProject> blackDuckProjects = blackDuckDataActions.getBlackDuckProjects();
        assertEquals(1, blackDuckProjects.size());
        final BlackDuckProject blackDuckProject = blackDuckProjects.get(0);
        assertEquals(projectName, blackDuckProject.getName());
        assertEquals(description, blackDuckProject.getDescription());
        assertEquals(href, blackDuckProject.getHref());
        assertEquals(projectOwnerEmail, blackDuckProject.getProjectOwnerEmail());
    }
}
