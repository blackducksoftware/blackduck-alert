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
package com.synopsys.integration.alert.channel.email.actions;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannel;
import com.synopsys.integration.alert.channel.email.EmailProperties;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.CategoryKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class EmailGlobalTestAction extends TestAction {
    private final EmailChannel emailChannel;

    @Autowired
    public EmailGlobalTestAction(final EmailChannel emailChannel) {
        this.emailChannel = emailChannel;
    }

    @Override
    public void testConfig(final TestConfigModel testConfig) throws IntegrationException {
        Set<String> emailAddresses = Set.of();
        final String testEmailAddress = testConfig.getDestination().orElse(null);
        if (StringUtils.isNotBlank(testEmailAddress)) {
            try {
                final InternetAddress emailAddr = new InternetAddress(testEmailAddress);
                emailAddr.validate();
            } catch (final AddressException ex) {
                throw new AlertException(String.format("%s is not a valid email address. %s", testEmailAddress, ex.getMessage()));
            }
            emailAddresses = Set.of(testEmailAddress);
        }
        final FieldAccessor fieldAccessor = testConfig.getFieldAccessor();
        final EmailProperties emailProperties = new EmailProperties(fieldAccessor);

        final SortedSet<LinkableItem> set = new TreeSet<>();
        final LinkableItem linkableItem = new LinkableItem("Message", "This is a test message from the Alert global email configuration.", null);
        set.add(linkableItem);
        final CategoryItem categoryItem = new CategoryItem(CategoryKey.from("TYPE"), null, 1L, set);
        final SortedSet<CategoryItem> categoryItems = new TreeSet<>();
        categoryItems.add(categoryItem);
        final AggregateMessageContent messageContent = new AggregateMessageContent("Message Content", "Test from Alert", categoryItems);
        emailChannel.sendMessage(emailProperties, emailAddresses, "Test from Alert", "Global Configuration", "", MessageContentGroup.singleton(messageContent));
    }

}
