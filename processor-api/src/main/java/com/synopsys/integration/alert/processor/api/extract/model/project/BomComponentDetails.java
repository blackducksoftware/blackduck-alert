/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model.project;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.CombinableModel;

public class BomComponentDetails extends AbstractBomComponentDetails implements CombinableModel<BomComponentDetails> {
    private final List<ComponentConcern> componentConcerns;

    public BomComponentDetails(
        LinkableItem component,
        @Nullable LinkableItem componentVersion,
        ComponentVulnerabilities componentVulnerabilities,
        List<ComponentPolicy> componentPolicies,
        List<ComponentConcern> componentConcerns,
        LinkableItem license,
        String usage,
        List<LinkableItem> additionalAttributes,
        String blackDuckIssuesUrl
    ) {
        super(component, componentVersion, componentVulnerabilities, componentPolicies, license, usage, additionalAttributes, blackDuckIssuesUrl);
        this.componentConcerns = componentConcerns;
    }

    public List<ComponentConcern> getComponentConcerns() {
        return componentConcerns;
    }

    public boolean hasComponentConcerns() {
        return !componentConcerns.isEmpty();
    }

    @Override
    public List<BomComponentDetails> combine(BomComponentDetails otherDetails) {
        List<BomComponentDetails> uncombinedDetails = List.of(this, otherDetails);

        if (!getComponent().equals(otherDetails.getComponent())) {
            return uncombinedDetails;
        }

        LinkableItem nullableComponentVersion = getComponentVersion().orElse(null);
        LinkableItem nullableOtherComponentVersion = otherDetails.getComponentVersion().orElse(null);
        if (!EqualsBuilder.reflectionEquals(nullableComponentVersion, nullableOtherComponentVersion)) {
            return uncombinedDetails;
        }

        if (hasComponentConcerns() != otherDetails.hasComponentConcerns()) {
            return uncombinedDetails;
        }

        return combineComponentConcerns(otherDetails.componentConcerns);
    }

    private List<BomComponentDetails> combineComponentConcerns(List<ComponentConcern> otherDetailsComponentConcerns) {
        List<ComponentConcern> combinedComponentConcerns = CombinableModel.combine(componentConcerns, otherDetailsComponentConcerns);
        BomComponentDetails combinedBomComponentDetails = new BomComponentDetails(
            getComponent(),
            getComponentVersion().orElse(null),
            getComponentVulnerabilities(),
            getComponentPolicies(),
            combinedComponentConcerns,
            getLicense(),
            getUsage(),
            getAdditionalAttributes(),
            getBlackDuckIssuesUrl()
        );
        return List.of(combinedBomComponentDetails);
    }

}
