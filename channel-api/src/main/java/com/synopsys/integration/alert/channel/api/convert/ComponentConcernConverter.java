/*
 * channel-api
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
package com.synopsys.integration.alert.channel.api.convert;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;

public class ComponentConcernConverter {
    private final ChannelMessageFormatter formatter;

    public ComponentConcernConverter(ChannelMessageFormatter formatter) {
        this.formatter = formatter;
    }

    public List<String> gatherComponentConcernSectionPieces(List<ComponentConcern> componentConcerns) {
        List<String> componentConcernSectionPieces = new LinkedList<>();

        String nonBreakingSpace = formatter.getNonBreakingSpace();
        String indent = nonBreakingSpace + nonBreakingSpace;
        String doubleIndent = indent + indent;

        Set<ComponentConcern> sortedConcerns = new TreeSet<>(componentConcerns);
        ComponentConcernType currentType = null;
        ItemOperation currentOperation = null;
        ComponentConcernSeverity currentSeverity = null;
        for (ComponentConcern componentConcern : sortedConcerns) {
            if (!componentConcern.getType().equals(currentType)) {
                currentType = componentConcern.getType();
                currentOperation = null;

                componentConcernSectionPieces.add(formatter.getLineSeparator());
                componentConcernSectionPieces.add(formatter.encode(currentType.name()));
            }

            if (!componentConcern.getOperation().equals(currentOperation)) {
                currentOperation = componentConcern.getOperation();
                currentSeverity = null;

                componentConcernSectionPieces.add(formatter.getLineSeparator());
                componentConcernSectionPieces.add(indent);
                componentConcernSectionPieces.add(formatter.encode(currentOperation.name()));
            }

            if (!componentConcern.getSeverity().equals(currentSeverity)) {
                currentSeverity = componentConcern.getSeverity();

                componentConcernSectionPieces.add(formatter.getLineSeparator());
                componentConcernSectionPieces.add(doubleIndent);
                componentConcernSectionPieces.add(formatter.encode(currentSeverity.name()));
                componentConcernSectionPieces.add(formatter.getLineSeparator());
            }

            String concernString = createComponentConcernString(componentConcern);
            componentConcernSectionPieces.add(concernString);
        }
        return componentConcernSectionPieces;
    }

    private String createComponentConcernString(ComponentConcern componentConcern) {
        String encodedName = formatter.encode(componentConcern.getName());
        Optional<String> concernUrl = componentConcern.getUrl();
        if (concernUrl.isPresent()) {
            String encodedUrl = formatter.encode(concernUrl.get());
            return String.format("[%s]", formatter.createLink(encodedName, encodedUrl));
        } else {
            return String.format("%s-%s%s%s", formatter.getNonBreakingSpace(), formatter.getNonBreakingSpace(), encodedName, formatter.getLineSeparator());
        }
    }

}
