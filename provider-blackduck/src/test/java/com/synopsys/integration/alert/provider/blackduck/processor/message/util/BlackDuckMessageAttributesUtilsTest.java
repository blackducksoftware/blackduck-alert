package com.synopsys.integration.alert.provider.blackduck.processor.message.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionComponentVersionLicensesView;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;

public class BlackDuckMessageAttributesUtilsTest {

    @Test
    public void testGetUsageLinkableItemsNullUsageIncluded() {
        ProjectVersionComponentVersionView projectVersionComponentView = new ProjectVersionComponentVersionView();
        List<UsageType> listWithNull = new ArrayList<>();
        listWithNull.add(UsageType.PREREQUISITE);
        listWithNull.add(null);
        projectVersionComponentView.setUsages(listWithNull);
        String usage = BlackDuckMessageAttributesUtils.extractUsage(projectVersionComponentView);
        assertEquals(UsageType.PREREQUISITE.prettyPrint(), usage);
    }

    @Test
    public void testGetUsageLinkableItems() {
        ProjectVersionComponentVersionView projectVersionComponentView = new ProjectVersionComponentVersionView();
        projectVersionComponentView.setUsages(List.of(UsageType.PREREQUISITE, UsageType.SOURCE_CODE));
        String usage = BlackDuckMessageAttributesUtils.extractUsage(projectVersionComponentView);
        assertEquals(UsageType.PREREQUISITE.prettyPrint(), usage);
    }

    @Test
    public void testGetLicenseLinkableItemsNullIncluded() {
        String licenseValue = "License Display";
        ProjectVersionComponentVersionView projectVersionComponentView = new ProjectVersionComponentVersionView();
        List<ProjectVersionComponentVersionLicensesView> projectVersionComponentLicensesViews = new ArrayList<>();
        ProjectVersionComponentVersionLicensesView projectVersionComponentLicensesView = new ProjectVersionComponentVersionLicensesView();
        projectVersionComponentLicensesView.setLicenseDisplay(licenseValue);
        projectVersionComponentLicensesViews.add(projectVersionComponentLicensesView);
        projectVersionComponentLicensesViews.add(null);
        projectVersionComponentView.setLicenses(projectVersionComponentLicensesViews);
        LinkableItem licenseLinkableItem = BlackDuckMessageAttributesUtils.extractLicense(projectVersionComponentView);
        assertEquals(licenseValue, licenseLinkableItem.getValue());
    }

    @Test
    public void testGetLicenseLinkableItems() {
        String licenseValue = "License Display 1";
        ProjectVersionComponentVersionView projectVersionComponentView = new ProjectVersionComponentVersionView();
        List<ProjectVersionComponentVersionLicensesView> projectVersionComponentLicensesViews = new ArrayList<>();
        ProjectVersionComponentVersionLicensesView projectVersionComponentLicensesView1 = new ProjectVersionComponentVersionLicensesView();
        projectVersionComponentLicensesView1.setLicenseDisplay(licenseValue);
        projectVersionComponentLicensesViews.add(projectVersionComponentLicensesView1);

        ProjectVersionComponentVersionLicensesView projectVersionComponentLicensesView2 = new ProjectVersionComponentVersionLicensesView();
        projectVersionComponentLicensesView2.setLicenseDisplay("License Display 2");
        projectVersionComponentLicensesViews.add(projectVersionComponentLicensesView2);

        projectVersionComponentView.setLicenses(projectVersionComponentLicensesViews);

        LinkableItem licenseLinkableItem = BlackDuckMessageAttributesUtils.extractLicense(projectVersionComponentView);
        assertEquals(licenseValue, licenseLinkableItem.getValue());
    }

}
