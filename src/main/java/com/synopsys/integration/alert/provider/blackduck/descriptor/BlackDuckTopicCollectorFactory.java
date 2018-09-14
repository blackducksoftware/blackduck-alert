/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.workflow.processor.TopicCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckPolicyTopicCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckVulnerabilityTopicCollector;

@Component
public class BlackDuckTopicCollectorFactory {

    private final ObjectFactory<BlackDuckVulnerabilityTopicCollector> vulnerabilityTopicCollectorFactory;
    private final ObjectFactory<BlackDuckPolicyTopicCollector> policyTopicCollectorFactory;

    @Autowired
    public BlackDuckTopicCollectorFactory(final ObjectFactory<BlackDuckVulnerabilityTopicCollector> vulnerabilityTopicCollectorFactory, final ObjectFactory<BlackDuckPolicyTopicCollector> policyTopicCollectorFactory) {
        this.vulnerabilityTopicCollectorFactory = vulnerabilityTopicCollectorFactory;
        this.policyTopicCollectorFactory = policyTopicCollectorFactory;
    }

    public Set<TopicCollector> createTopicCollectors() {
        final Set<TopicCollector> collectorSet = new HashSet<>();
        collectorSet.add(vulnerabilityTopicCollectorFactory.getObject());
        collectorSet.add(policyTopicCollectorFactory.getObject());
        return collectorSet;
    }
}
