/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.workflow.combiner;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

@Component
public class DefaultMessageCombiner extends AbstractMessageCombiner {
    @Override
    protected LinkedHashSet<ComponentItem> gatherComponentItems(Collection<ProviderMessageContent> groupedMessages) {
        List<ComponentItem> allComponentItems = groupedMessages
                                                    .stream()
                                                    .map(ProviderMessageContent::getComponentItems)
                                                    .flatMap(Set::stream)
                                                    .collect(Collectors.toList());
        return combineComponentItems(allComponentItems);
    }

}
