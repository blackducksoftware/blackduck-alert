/**
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

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.processor.api.extract.model.CombinableModel;

public class BomComponentDetails extends AlertSerializableModel implements CombinableModel<BomComponentDetails> {
    private final LinkableItem component;
    private final LinkableItem componentVersion;
    private final List<ComponentConcern> componentConcerns;

    private final LinkableItem license;
    private final String usage;

    // TODO add callback url

    public BomComponentDetails(LinkableItem component, LinkableItem componentVersion, List<ComponentConcern> componentConcerns, LinkableItem license, String usage) {
        this.component = component;
        this.componentVersion = componentVersion;
        this.componentConcerns = componentConcerns;
        this.license = license;
        this.usage = usage;
    }

    public LinkableItem getComponent() {
        return component;
    }

    public LinkableItem getComponentVersion() {
        return componentVersion;
    }

    public List<ComponentConcern> getComponentConcerns() {
        return componentConcerns;
    }

    public LinkableItem getLicense() {
        return license;
    }

    public String getUsage() {
        return usage;
    }

    public boolean hasComponentConcerns() {
        return !componentConcerns.isEmpty();
    }

    @Override
    public List<BomComponentDetails> combine(BomComponentDetails otherDetails) {
        List<BomComponentDetails> uncombinedDetails = List.of(this, otherDetails);

        if (!component.equals(otherDetails.component)) {
            return uncombinedDetails;
        }

        // If one is null and the other is not, they represent different component-versions and cannot be combined.
        if (null == componentVersion && otherDetails.componentVersion != null) {
            return uncombinedDetails;
        }
        // If one is not null and it does not equal the other, they represent different component-versions and cannot be combined.
        else if (null != componentVersion && !componentVersion.equals(otherDetails.componentVersion)) {
            return uncombinedDetails;
        }

        // Either both component-versions are null, or they are equal to each other.
        // Either way, their component-concerns are candidates for combination.
        return combineComponentConcerns(otherDetails.componentConcerns);
    }

    private List<BomComponentDetails> combineComponentConcerns(List<ComponentConcern> otherDetailsComponentConcerns) {
        List<ComponentConcern> combinedComponentConcerns = CombinableModel.combine(componentConcerns, otherDetailsComponentConcerns);
        BomComponentDetails combinedBomComponentDetails = new BomComponentDetails(component, componentVersion, combinedComponentConcerns, license, usage);
        return List.of(combinedBomComponentDetails);
    }

}
