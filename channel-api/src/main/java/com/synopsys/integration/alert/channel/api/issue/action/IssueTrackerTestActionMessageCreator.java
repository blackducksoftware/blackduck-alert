/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.action;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

@Component
public class IssueTrackerTestActionMessageCreator {
    private static final Long PROVIDER_CONFIG_ID = 0L;
    private static final String PROVIDER_LABEL = "Provider Label";
    private static final String PROVIDER_CONFIG_NAME = "Provider Config Name";

    private static final String PROJECT_LABEL = "Project Label";
    private static final String PROJECT_NAME = "Project Name";

    private static final String PROJECT_VERSION_LABEL = "Project-Version Label";
    private static final String PROJECT_VERSION_NAME = "Project-Version Name";

    private static final String COMPONENT_LABEL = "Component Label";
    private static final String COMPONENT_NAME = "Component Name";

    private static final String COMPONENT_VERSION_LABEL = "Component-Version Label";
    private static final String COMPONENT_VERSION_NAME = "Component-Version Name";

    private static final String POLICY_NAME = "Policy Name";

    private static final String LICENSE_LABEL = "License Label";
    private static final String LICENSE_NAME = "License Name";

    private static final String USAGE_SUMMARY = "Example Usage Summary";

    public ProjectMessage createComponentConcernProjectMessage(ItemOperation operation) {
        ProviderDetails providerDetails = createTestMessageProviderDetails();

        LinkableItem projectItem = new LinkableItem(PROJECT_LABEL, PROJECT_NAME);
        LinkableItem projectVersionItem = new LinkableItem(PROJECT_VERSION_LABEL, PROJECT_VERSION_NAME);

        BomComponentDetails bomComponent = createBomComponent(operation);

        return ProjectMessage.componentConcern(providerDetails, projectItem, projectVersionItem, List.of(bomComponent));
    }

    public ProviderDetails createTestMessageProviderDetails() {
        LinkableItem providerItem = new LinkableItem(PROVIDER_LABEL, PROVIDER_CONFIG_NAME);
        return new ProviderDetails(PROVIDER_CONFIG_ID, providerItem);
    }

    private BomComponentDetails createBomComponent(ItemOperation operation) {
        LinkableItem componentItem = new LinkableItem(COMPONENT_LABEL, COMPONENT_NAME);
        LinkableItem componentVersionItem = new LinkableItem(COMPONENT_VERSION_LABEL, COMPONENT_VERSION_NAME);

        ComponentConcern componentConcern = createComponentConcern(operation);

        LinkableItem licenseItem = new LinkableItem(LICENSE_LABEL, LICENSE_NAME);

        return new BomComponentDetails(
            componentItem,
            componentVersionItem,
            List.of(componentConcern),
            licenseItem,
            USAGE_SUMMARY,
            List.of(),
            ""
        );
    }

    private ComponentConcern createComponentConcern(ItemOperation operation) {
        return ComponentConcern.policy(operation, POLICY_NAME);
    }

}
