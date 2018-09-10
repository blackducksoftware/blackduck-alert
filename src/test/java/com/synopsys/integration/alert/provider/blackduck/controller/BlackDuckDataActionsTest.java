package com.synopsys.integration.alert.provider.blackduck.controller;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckGroupEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckGroupRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckGroupRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.model.BlackDuckGroup;
import com.synopsys.integration.alert.provider.blackduck.model.BlackDuckProject;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckDataActions;

public class BlackDuckDataActionsTest {

    @Test
    public void testGetHubGroupsNoGroups() throws Exception {
        final BlackDuckGroupRepositoryAccessor blackDuckGroupRepositoryAccessor = new MockBlackDuckGroupRepositoryAccessor();
        final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();

        final BlackDuckDataActions blackDuckDataActions = new BlackDuckDataActions(blackDuckGroupRepositoryAccessor, blackDuckProjectRepositoryAccessor);
        final List<BlackDuckGroup> blackDuckGroups = blackDuckDataActions.getBlackDuckGroups();
        assertEquals(0, blackDuckGroups.size());
    }

    @Test
    public void testGetHubGroups() throws Exception {
        final BlackDuckGroupRepositoryAccessor blackDuckGroupRepositoryAccessor = new MockBlackDuckGroupRepositoryAccessor();
        final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();

        final Boolean active = true;
        final String groupName = "Group";
        final String href = "href";

        blackDuckGroupRepositoryAccessor.saveEntity(new BlackDuckGroupEntity(groupName, active, href));

        final BlackDuckDataActions blackDuckDataActions = new BlackDuckDataActions(blackDuckGroupRepositoryAccessor, blackDuckProjectRepositoryAccessor);
        final List<BlackDuckGroup> blackDuckGroups = blackDuckDataActions.getBlackDuckGroups();
        assertEquals(1, blackDuckGroups.size());
        final BlackDuckGroup blackDuckGroup = blackDuckGroups.get(0);
        assertEquals(active, blackDuckGroup.getActive());
        assertEquals(groupName, blackDuckGroup.getName());
        assertEquals(href, blackDuckGroup.getHref());
    }

    @Test
    public void testGetHubProjectsNoProjects() throws Exception {
        final BlackDuckGroupRepositoryAccessor blackDuckGroupRepositoryAccessor = new MockBlackDuckGroupRepositoryAccessor();
        final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();

        final BlackDuckDataActions blackDuckDataActions = new BlackDuckDataActions(blackDuckGroupRepositoryAccessor, blackDuckProjectRepositoryAccessor);
        final List<BlackDuckProject> blackDuckProjects = blackDuckDataActions.getBlackDuckProjects();
        assertEquals(0, blackDuckProjects.size());
    }

    @Test
    public void testGetHubProjects() throws Exception {
        final BlackDuckGroupRepositoryAccessor blackDuckGroupRepositoryAccessor = new MockBlackDuckGroupRepositoryAccessor();
        final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();

        final String projectName = "projectName";
        final String description = "Description";
        final String href = "href";

        blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity(projectName, description, href));

        final BlackDuckDataActions blackDuckDataActions = new BlackDuckDataActions(blackDuckGroupRepositoryAccessor, blackDuckProjectRepositoryAccessor);
        final List<BlackDuckProject> blackDuckProjects = blackDuckDataActions.getBlackDuckProjects();
        assertEquals(1, blackDuckProjects.size());
        final BlackDuckProject blackDuckProject = blackDuckProjects.get(0);
        assertEquals(projectName, blackDuckProject.getName());
        assertEquals(description, blackDuckProject.getDescription());
        assertEquals(href, blackDuckProject.getHref());
    }
}
