/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model.project;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.ListUtils;
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
        List<ComponentPolicy> relevantPolicies,
        List<ComponentConcern> componentConcerns,
        LinkableItem license,
        String usage,
        ComponentUpgradeGuidance componentUpgradeGuidance,
        List<LinkableItem> additionalAttributes,
        @Nullable String blackDuckIssuesUrl
    ) {
        super(component, componentVersion, componentVulnerabilities, relevantPolicies, license, usage, componentUpgradeGuidance, additionalAttributes, blackDuckIssuesUrl);
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

        return combineComponentConcerns(otherDetails);
    }

    private List<BomComponentDetails> combineComponentConcerns(BomComponentDetails otherDetails) {
        List<ComponentConcern> combinedComponentConcerns = CombinableModel.combine(getComponentConcerns(), otherDetails.getComponentConcerns());
        List<ComponentPolicy> componentPolicies = CombinableModel.combine(getRelevantPolicies(), otherDetails.getRelevantPolicies());
        List<LinkableItem> combineAdditionalAttributes = combineAdditionalAttributes(otherDetails, combinedComponentConcerns);
        BomComponentDetails combinedBomComponentDetails = new BomComponentDetails(
            getComponent(),
            getComponentVersion().orElse(null),
            getComponentVulnerabilities(),
            componentPolicies,
            combinedComponentConcerns,
            getLicense(),
            getUsage(),
            getComponentUpgradeGuidance(),
            combineAdditionalAttributes,
            getBlackDuckIssuesUrl()
        );
        return List.of(combinedBomComponentDetails);
    }

    private List<LinkableItem> combineAdditionalAttributes(BomComponentDetails otherDetails, List<ComponentConcern> combinedComponentConcerns) {
        if (ListUtils.isEqualList(getComponentConcerns(), combinedComponentConcerns)) {
            return getAdditionalAttributes();
        } else if (ListUtils.isEqualList(otherDetails.getComponentConcerns(), combinedComponentConcerns)) {
            return otherDetails.getAdditionalAttributes();
        }

        Set<LinkableItem> combinedAdditionalAttributes = new LinkedHashSet<>(getAdditionalAttributes());
        combinedAdditionalAttributes.addAll(otherDetails.getAdditionalAttributes());
        return new ArrayList<>(combinedAdditionalAttributes);
    }

}
