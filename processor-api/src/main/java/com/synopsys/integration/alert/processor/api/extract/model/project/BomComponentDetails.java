/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.processor.api.extract.model.project;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.CombinableModel;

public class BomComponentDetails extends AbstractBomComponentDetails implements CombinableModel<BomComponentDetails> {
    private final List<ComponentConcern> componentConcerns;

    public BomComponentDetails(
        LinkableItem component,
        @Nullable LinkableItem componentVersion,
        List<ComponentConcern> componentConcerns,
        LinkableItem license,
        String usage,
        List<LinkableItem> additionalAttributes,
        String blackDuckIssuesUrl
    ) {
        super(component, componentVersion, license, usage, additionalAttributes, blackDuckIssuesUrl);
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

        Optional<LinkableItem> optionalComponentVersion = getComponentVersion();
        if (optionalComponentVersion.isPresent()) {
            LinkableItem componentVersion = optionalComponentVersion.get();
            if (!componentVersion.equals(otherDetails.getComponentVersion().orElse(null))) {
                return uncombinedDetails;
            }
        } else if (otherDetails.getComponentVersion().isPresent()) {
            return uncombinedDetails;
        }

        // Either both component-versions are missing, or they are equal to each other.
        // Either way, their component-concerns are candidates for combination.
        return combineComponentConcerns(otherDetails.componentConcerns);
    }

    private List<BomComponentDetails> combineComponentConcerns(List<ComponentConcern> otherDetailsComponentConcerns) {
        List<ComponentConcern> combinedComponentConcerns = CombinableModel.combine(componentConcerns, otherDetailsComponentConcerns);
        BomComponentDetails combinedBomComponentDetails = new BomComponentDetails(
            getComponent(),
            getComponentVersion().orElse(null),
            combinedComponentConcerns, getLicense(),
            getUsage(),
            getAdditionalAttributes(),
            getBlackDuckIssuesUrl()
        );
        return List.of(combinedBomComponentDetails);
    }

}
