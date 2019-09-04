/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.msteams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.channel.util.FreemarkerDataModel;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

public class MsTeamsMessage implements FreemarkerDataModel {
    private Set<LinkableItem> providers = new HashSet<>();
    private List<MsTeamsSection> sections = new ArrayList<>();

    public void addAllContent(MsTeamsMessage other) {
        providers.addAll(other.providers);
        sections.addAll(other.sections);
    }

    public void addContent(ProviderMessageContent providerMessageContent) {
        providers.add(providerMessageContent.getProvider());

        MsTeamsSection msTeamsSection = new MsTeamsSection();
        msTeamsSection.setProvider(providerMessageContent.getProvider().getValue());
        msTeamsSection.setTopic(providerMessageContent.getTopic().getValue());
        providerMessageContent.getSubTopic()
            .map(LinkableItem::getValue)
            .ifPresent(msTeamsSection::setSubTopic);
        sections.add(msTeamsSection);

        List<MsTeamsComponent> components = new ArrayList<>(providerMessageContent.getComponentItems().size());
        for (ComponentItem componentItem : providerMessageContent.getComponentItems()) {
            MsTeamsComponent msTeamsComponent = new MsTeamsComponent();
            msTeamsComponent.setCategory(componentItem.getCategory());
            msTeamsComponent.setOperation(componentItem.getOperation().toString());

            StringBuilder componentTextBuilder = new StringBuilder(componentItem.getComponent().getValue());
            componentItem.getSubComponent().map(item -> "/" + item.getValue()).ifPresent(componentTextBuilder::append);
            msTeamsComponent.setText(componentTextBuilder.toString());

            String allAttributeDetails = createDetails(componentItem.getComponentAttributes());
            msTeamsComponent.setAllAttributeDetails(allAttributeDetails);

            components.add(msTeamsComponent);
        }
        msTeamsSection.setComponents(components);
    }

    private String createDetails(Set<LinkableItem> componentAttributes) {
        //TODO determine if we should be including the URLs
        return componentAttributes
                   .stream()
                   .map(linkableItem -> String.format("%s: %s", linkableItem.getName(), linkableItem.getValue()))
                   .collect(Collectors.joining(", "));
    }

    public int getProviderCount() {
        return providers.size();
    }

    public List<MsTeamsSection> getSections() {
        return sections;
    }

}
