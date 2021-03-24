/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.convert;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

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
        LinkedList<String> componentConcernSectionPieces = new LinkedList<>();

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

        if (!componentConcernSectionPieces.isEmpty() && !StringUtils.endsWith(componentConcernSectionPieces.getLast(), formatter.getLineSeparator())) {
            componentConcernSectionPieces.add(formatter.getLineSeparator());
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
