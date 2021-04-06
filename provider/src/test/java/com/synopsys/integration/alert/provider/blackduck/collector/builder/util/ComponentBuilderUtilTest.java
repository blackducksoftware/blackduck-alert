package com.synopsys.integration.alert.provider.blackduck.collector.builder.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionComponentLicensesView;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;

public class ComponentBuilderUtilTest {

    @Test
    public void testGetUsageLinkableItemsNullUsageIncluded() {
        ProjectVersionComponentView projectVersionComponentView = new ProjectVersionComponentView();
        List<UsageType> listWithNull = new ArrayList();
        listWithNull.add(UsageType.PREREQUISITE);
        listWithNull.add(null);
        projectVersionComponentView.setUsages(listWithNull);
        List<LinkableItem> usageLinkableItems = ComponentBuilderUtil.getUsageLinkableItems(projectVersionComponentView);
        assertEquals(1, usageLinkableItems.size());
    }

    @Test
    public void testGetUsageLinkableItems() {
        ProjectVersionComponentView projectVersionComponentView = new ProjectVersionComponentView();
        projectVersionComponentView.setUsages(List.of(UsageType.PREREQUISITE, UsageType.SOURCE_CODE));
        List<LinkableItem> usageLinkableItems = ComponentBuilderUtil.getUsageLinkableItems(projectVersionComponentView);
        assertEquals(2, usageLinkableItems.size());
    }

    @Test
    public void testGetLicenseLinkableItemsNullIncluded() {
        ProjectVersionComponentView projectVersionComponentView = new ProjectVersionComponentView();
        List<ProjectVersionComponentLicensesView> projectVersionComponentLicensesViews = new ArrayList();
        ProjectVersionComponentLicensesView projectVersionComponentLicensesView = new ProjectVersionComponentLicensesView();
        projectVersionComponentLicensesView.setLicenseDisplay("License Display");
        projectVersionComponentLicensesViews.add(projectVersionComponentLicensesView);
        projectVersionComponentLicensesViews.add(null);
        projectVersionComponentView.setLicenses(projectVersionComponentLicensesViews);
        List<LinkableItem> licenseLinkableItems = ComponentBuilderUtil.getLicenseLinkableItems(projectVersionComponentView);
        assertEquals(1, licenseLinkableItems.size());
    }

    @Test
    public void testGetLicenseLinkableItems() {
        ProjectVersionComponentView projectVersionComponentView = new ProjectVersionComponentView();
        List<ProjectVersionComponentLicensesView> projectVersionComponentLicensesViews = new ArrayList();
        ProjectVersionComponentLicensesView projectVersionComponentLicensesView1 = new ProjectVersionComponentLicensesView();
        projectVersionComponentLicensesView1.setLicenseDisplay("License Display 1");
        projectVersionComponentLicensesViews.add(projectVersionComponentLicensesView1);

        ProjectVersionComponentLicensesView projectVersionComponentLicensesView2 = new ProjectVersionComponentLicensesView();
        projectVersionComponentLicensesView2.setLicenseDisplay("License Display 2");
        projectVersionComponentLicensesViews.add(projectVersionComponentLicensesView2);

        projectVersionComponentView.setLicenses(projectVersionComponentLicensesViews);

        List<LinkableItem> licenseLinkableItems = ComponentBuilderUtil.getLicenseLinkableItems(projectVersionComponentView);
        assertEquals(2, licenseLinkableItems.size());
    }
}
