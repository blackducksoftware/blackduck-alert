package com.synopsys.integration.alert.provider.blackduck.processor.message.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionComponentLicensesView;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;

public class BlackDuckMessageAttributesUtilsTest {

    @Test
    public void testGetUsageLinkableItemsNullUsageIncluded() {
        ProjectVersionComponentView projectVersionComponentView = new ProjectVersionComponentView();
        List<UsageType> listWithNull = new ArrayList();
        listWithNull.add(UsageType.PREREQUISITE);
        listWithNull.add(null);
        projectVersionComponentView.setUsages(listWithNull);
        String usage = BlackDuckMessageAttributesUtils.extractUsage(projectVersionComponentView);
        assertEquals(UsageType.PREREQUISITE.prettyPrint(), usage);
    }

    @Test
    public void testGetUsageLinkableItems() {
        ProjectVersionComponentView projectVersionComponentView = new ProjectVersionComponentView();
        projectVersionComponentView.setUsages(List.of(UsageType.PREREQUISITE, UsageType.SOURCE_CODE));
        String usage = BlackDuckMessageAttributesUtils.extractUsage(projectVersionComponentView);
        assertEquals(UsageType.PREREQUISITE.prettyPrint(), usage);
    }

    @Test
    public void testGetLicenseLinkableItemsNullIncluded() {
        String licenseValue = "License Display";
        ProjectVersionComponentView projectVersionComponentView = new ProjectVersionComponentView();
        List<ProjectVersionComponentLicensesView> projectVersionComponentLicensesViews = new ArrayList();
        ProjectVersionComponentLicensesView projectVersionComponentLicensesView = new ProjectVersionComponentLicensesView();
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
        ProjectVersionComponentView projectVersionComponentView = new ProjectVersionComponentView();
        List<ProjectVersionComponentLicensesView> projectVersionComponentLicensesViews = new ArrayList();
        ProjectVersionComponentLicensesView projectVersionComponentLicensesView1 = new ProjectVersionComponentLicensesView();
        projectVersionComponentLicensesView1.setLicenseDisplay(licenseValue);
        projectVersionComponentLicensesViews.add(projectVersionComponentLicensesView1);

        ProjectVersionComponentLicensesView projectVersionComponentLicensesView2 = new ProjectVersionComponentLicensesView();
        projectVersionComponentLicensesView2.setLicenseDisplay("License Display 2");
        projectVersionComponentLicensesViews.add(projectVersionComponentLicensesView2);

        projectVersionComponentView.setLicenses(projectVersionComponentLicensesViews);

        LinkableItem licenseLinkableItem = BlackDuckMessageAttributesUtils.extractLicense(projectVersionComponentView);
        assertEquals(licenseValue, licenseLinkableItem.getValue());
    }
}
