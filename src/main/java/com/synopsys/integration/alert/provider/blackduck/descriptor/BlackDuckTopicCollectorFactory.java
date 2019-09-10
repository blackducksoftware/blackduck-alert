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
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.workflow.MessageContentCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckBomEditCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckLicenseLimitCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckPolicyOverrideCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckPolicyViolationCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckProjectVersionCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckVulnerabilityCollector;

@Component
public class BlackDuckTopicCollectorFactory {
    private final ObjectFactory<BlackDuckVulnerabilityCollector> vulnerabilityTopicCollectorFactory;
    private final ObjectFactory<BlackDuckPolicyViolationCollector> policyViolationTopicCollectorFactory;
    private final ObjectFactory<BlackDuckPolicyOverrideCollector> policyOverrideTopicCollectorFactory;
    private final ObjectFactory<BlackDuckLicenseLimitCollector> licenseLimitTopicCollectorFactory;
    private final ObjectFactory<BlackDuckBomEditCollector> bomEditCollectorFactory;
    private final ObjectFactory<BlackDuckProjectVersionCollector> blackDuckProjectVersionCollectorFactory;

    @Autowired
    public BlackDuckTopicCollectorFactory(ObjectFactory<BlackDuckVulnerabilityCollector> vulnerabilityTopicCollectorFactory,
        ObjectFactory<BlackDuckPolicyViolationCollector> policyViolationTopicCollectorFactory, ObjectFactory<BlackDuckPolicyOverrideCollector> policyOverrideTopicCollectorFactory,
        ObjectFactory<BlackDuckLicenseLimitCollector> licenseLimitTopicCollectorFactory, ObjectFactory<BlackDuckBomEditCollector> bomEditCollectorFactory,
        ObjectFactory<BlackDuckProjectVersionCollector> blackDuckProjectVersionCollectorFactory) {
        this.vulnerabilityTopicCollectorFactory = vulnerabilityTopicCollectorFactory;
        this.policyViolationTopicCollectorFactory = policyViolationTopicCollectorFactory;
        this.policyOverrideTopicCollectorFactory = policyOverrideTopicCollectorFactory;
        this.licenseLimitTopicCollectorFactory = licenseLimitTopicCollectorFactory;
        this.bomEditCollectorFactory = bomEditCollectorFactory;
        this.blackDuckProjectVersionCollectorFactory = blackDuckProjectVersionCollectorFactory;
    }

    public Set<MessageContentCollector> createTopicCollectors() {
        Set<MessageContentCollector> collectorSet = new HashSet<>();
        collectorSet.add(vulnerabilityTopicCollectorFactory.getObject());
        collectorSet.add(policyViolationTopicCollectorFactory.getObject());
        collectorSet.add(policyOverrideTopicCollectorFactory.getObject());
        collectorSet.add(licenseLimitTopicCollectorFactory.getObject());
        collectorSet.add(bomEditCollectorFactory.getObject());
        collectorSet.add(blackDuckProjectVersionCollectorFactory.getObject());
        return collectorSet;
    }

}
