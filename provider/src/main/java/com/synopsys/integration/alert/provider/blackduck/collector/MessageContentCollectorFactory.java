/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.workflow.processor.message.MessageContentProcessor;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.BlackDuckMessageBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

@Component
public class MessageContentCollectorFactory {
    private final List<MessageContentProcessor> messageContentProcessors;
    private final List<BlackDuckMessageBuilder> messageBuilders;

    public MessageContentCollectorFactory(List<MessageContentProcessor> messageContentProcessors, List<BlackDuckMessageBuilder> messageBuilders) {
        this.messageContentProcessors = messageContentProcessors;
        this.messageBuilders = messageBuilders;
    }

    public BlackDuckMessageContentCollector createCollector(BlackDuckServicesFactory blackDuckServicesFactory) {
        return new BlackDuckMessageContentCollector(blackDuckServicesFactory, messageContentProcessors, messageBuilders);
    }
}
