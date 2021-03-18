/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.channel;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.attachment.compatibility.ComponentItem;
import com.synopsys.integration.alert.channel.email.attachment.compatibility.ProviderMessageContent;
import com.synopsys.integration.alert.channel.util.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.test.common.TestAlertProperties;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public abstract class AbstractChannelTest {
    protected Gson gson;
    protected TestProperties properties;
    protected ContentConverter contentConverter;
    protected AuditAccessor auditAccessor;

    @BeforeEach
    public void init() {
        gson = new Gson();
        properties = new TestProperties();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
        auditAccessor = Mockito.mock(AuditAccessor.class);
    }

    public ProviderMessageContent createMessageContent(String testName) throws AlertException {
        LinkableItem linkableItem1 = new LinkableItem("First Linkable Item", "Value 1", "https://google.com");
        LinkableItem linkableItem2 = new LinkableItem("Second Linkable Item", "Value 2", "https://google.com");

        final String nameKey = "Same Key";
        LinkableItem linkableItem3 = new LinkableItem(nameKey, "Value", "https://google.com");
        LinkableItem linkableItem4 = new LinkableItem(nameKey, "No Link Value");
        LinkableItem linkableItem5 = new LinkableItem(nameKey, "Other Value", "https://google.com");

        ComponentItem.Builder componentBuilder1 = new ComponentItem.Builder();
        ComponentItem.Builder componentBuilder2 = new ComponentItem.Builder();
        ComponentItem.Builder componentBuilder3 = new ComponentItem.Builder();

        componentBuilder1
            .applyComponentData("component", "componentValue")
            .applyCategoryItem("categoryItem", "categoryValue")
            .applyCategory("category")
            .applyNotificationId(1L)
            .applyOperation(ItemOperation.ADD)
            .applyComponentAttribute(linkableItem1)
            .applyComponentAttribute(linkableItem2);

        componentBuilder2
            .applyComponentData("component", "componentValue")
            .applyCategoryItem("categoryItem", "categoryValue")
            .applyCategory("category")
            .applyNotificationId(2L)
            .applyOperation(ItemOperation.UPDATE)
            .applyComponentAttribute(linkableItem2);

        componentBuilder3
            .applyComponentData("component", "componentValue")
            .applyCategoryItem("categoryItem", "categoryValue")
            .applyCategory("category")
            .applyNotificationId(1L)
            .applyOperation(ItemOperation.DELETE)
            .applyComponentAttribute(linkableItem3)
            .applyComponentAttribute(linkableItem4)
            .applyComponentAttribute(linkableItem5);

        LinkableItem subTopic = new LinkableItem("Sub Topic", "Sub Topic Value", "https://google.com");

        Collection<ComponentItem> items = new LinkedList<>();
        items.add(componentBuilder1.build());
        items.add(componentBuilder2.build());
        items.add(componentBuilder3.build());

        ProviderMessageContent.Builder providerBuilder = new ProviderMessageContent.Builder();
        providerBuilder
            .applyProvider("testProvider", 1L, "testProviderConfig")
            .applyTopic("Topic", testName, "https://google.com")
            .applySubTopic(subTopic.getLabel(), subTopic.getValue(), subTopic.getUrl().orElse(null))
            .applyAllComponentItems(items);

        return providerBuilder.build();
    }

    public RestChannelUtility createRestChannelUtility() {
        TestAlertProperties testAlertProperties = new TestAlertProperties();
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties, proxyManager);
        return new RestChannelUtility(channelRestConnectionFactory);
    }

}
