/**
 * alert-common
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
package com.synopsys.integration.alert.common.workflow.combiner;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

@Component
public class TopLevelActionCombiner implements MessageCombiner {
    @Override
    public List<ProviderMessageContent> combine(List<ProviderMessageContent> messages) {
        Map<String, List<ProviderMessageContent>> providerMessagesCache = new LinkedHashMap<>();
        for (ProviderMessageContent message : messages) {
            String cacheKey = flattenProviderAndTopic(message);
            if (isTopLevelDelete(message)) {
                List<ProviderMessageContent> cachedMessages = providerMessagesCache.get(cacheKey);
                if (null != cachedMessages) {
                    boolean didUpdate = updateCache(message, cachedMessages);
                    if (didUpdate) {
                        continue;
                    }
                }
            }
            providerMessagesCache.computeIfAbsent(cacheKey, ignored -> new LinkedList<>()).add(message);
        }

        return Stream
                   .of(providerMessagesCache.values())
                   .flatMap(Collection::stream)
                   .flatMap(List::stream)
                   .collect(Collectors.toList());
    }

    private boolean isTopLevelDelete(ProviderMessageContent message) {
        return message.isTopLevelActionOnly() && message.getAction().filter(action -> action.equals(ItemOperation.DELETE)).isPresent();
    }

    private String flattenProviderAndTopic(ProviderMessageContent message) {
        LinkableItem topic = message.getTopic();
        return message.getProvider().getValue() + topic.getName() + topic.getValue();
    }

    private String flattenSubTopic(LinkableItem subTopic) {
        return subTopic.getName() + subTopic.getValue();
    }

    private boolean updateCache(ProviderMessageContent currentMessage, List<ProviderMessageContent> cachedMessages) {
        String subTopicString = currentMessage.getSubTopic().map(this::flattenSubTopic).orElse(null);
        Set<ProviderMessageContent> removalCandidates = new HashSet<>();
        for (ProviderMessageContent cachedMessage : cachedMessages) {
            if (cachedMessage.isTopLevelActionOnly() && !isTopLevelDelete(cachedMessage)) {
                String cachedSubTopic = cachedMessage.getSubTopic().map(this::flattenSubTopic).orElse(null);
                if (null == subTopicString || subTopicString.equals(cachedSubTopic)) {
                    removalCandidates.add(cachedMessage);
                }
            }
        }
        return cachedMessages.removeAll(removalCandidates);
    }

}
