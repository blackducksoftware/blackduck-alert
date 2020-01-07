/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.polaris;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.provider.polaris.model.AlertPolarisNotificationTypeEnum;

//@Component
//@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PolarisCollector {
    private final PolarisProperties polarisProperties;

    @Autowired
    public PolarisCollector(PolarisProperties polarisProperties) {
        this.polarisProperties = polarisProperties;
    }

    protected LinkableItem getProviderItem() {
        final String polarisUrl = polarisProperties.getUrl().orElse(null);
        return new LinkableItem(ProviderMessageContent.LABEL_PROVIDER, "Polaris", polarisUrl);
    }

    protected Collection<ComponentItem> getComponentItems(Object jsonFieldAccessor, List<Object> notificationFields, AlertNotificationWrapper notificationContent) {
        final List<Object> countFields = List.of(); // getIntegerFields(notificationFields);
        final Optional<Object> optionalIssueTypeField = Optional.empty(); // getStringFields(notificationFields)
        //                                                                       .stream()
        //                                                                       .filter(field -> PolarisContent.LABEL_ISSUE_TYPE.equals(field.getLabel()))
        //                                                                       .findFirst();
        ComponentItem.Builder builder = new ComponentItem.Builder();
        //        final SortedSet<LinkableItem> attributes = new TreeSet<>();
        //        if (optionalIssueTypeField.isPresent()) {
        //            final JsonField<String> issueTypeField = optionalIssueTypeField.get();
        //            final String issueType = jsonFieldAccessor.getFirst(issueTypeField).orElse("<unknown>");
        //            final LinkableItem issueTypeItem = new LinkableItem(issueTypeField.getLabel(), issueType);
        //            attributes.add(issueTypeItem);
        //            builder.applyComponentAttribute(issueTypeItem);
        //        }
        //
        //        for (final JsonField<Integer> field : countFields) {
        //            final String label = field.getLabel();
        //            final Integer currentCount = jsonFieldAccessor.getFirst(field).orElse(0);
        //            final LinkableItem countItem = new LinkableItem(label, currentCount.toString());
        //            if (PolarisContent.JSON_FIELD_CHANGED_COUNT.equals(label)) {
        //                countItem.setNumericValueFlag(true);
        //            }
        //            attributes.add(countItem);
        //        }
        //
        //        final ItemOperation operation = getOperationFromNotificationType(notificationContent.getNotificationType());
        //        builder.applyAllComponentAttributes(attributes)
        //            .applyCategory(notificationContent.getNotificationType())
        //            .applyOperation(operation)
        //            .applyNotificationId(notificationContent.getId());

        try {
            ComponentItem item = builder.build();
            return List.of(item);
        } catch (AlertException ex) {
            return List.of();
        }
    }

    private ItemOperation getOperationFromNotificationType(final String notificationType) {
        if (AlertPolarisNotificationTypeEnum.ISSUE_COUNT_INCREASED.name().equals(notificationType)) {
            return ItemOperation.ADD;
        }
        return ItemOperation.DELETE;
    }

}
