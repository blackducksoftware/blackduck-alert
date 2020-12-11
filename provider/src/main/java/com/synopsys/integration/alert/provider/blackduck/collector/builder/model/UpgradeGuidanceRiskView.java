/**
 * provider
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
package com.synopsys.integration.alert.provider.blackduck.collector.builder.model;

import java.math.BigDecimal;
import java.util.Optional;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceLongTermVulnerabilityRiskView;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceShortTermVulnerabilityRiskView;

public class UpgradeGuidanceRiskView extends AlertSerializableModel {
    private Integer critical;
    private Integer high;
    private Integer medium;
    private Integer low;

    public static UpgradeGuidanceRiskView fromShortTermVulnerabilityRiskView(ComponentVersionUpgradeGuidanceShortTermVulnerabilityRiskView shortTermVulnerabilityRiskView) {
        if (null == shortTermVulnerabilityRiskView) {
            return new UpgradeGuidanceRiskView();
        }
        return new UpgradeGuidanceRiskView(getIntegerFromBigDecimal(shortTermVulnerabilityRiskView.getCritical()), getIntegerFromBigDecimal(shortTermVulnerabilityRiskView.getHigh()),
            getIntegerFromBigDecimal(shortTermVulnerabilityRiskView.getMedium()), getIntegerFromBigDecimal(shortTermVulnerabilityRiskView.getLow()));
    }

    public static UpgradeGuidanceRiskView fromLongTermVulnerabilityRiskView(ComponentVersionUpgradeGuidanceLongTermVulnerabilityRiskView longTermVulnerabilityRiskView) {
        if (null == longTermVulnerabilityRiskView) {
            return new UpgradeGuidanceRiskView();
        }
        return new UpgradeGuidanceRiskView(getIntegerFromBigDecimal(longTermVulnerabilityRiskView.getCritical()), getIntegerFromBigDecimal(longTermVulnerabilityRiskView.getHigh()),
            getIntegerFromBigDecimal(longTermVulnerabilityRiskView.getMedium()), getIntegerFromBigDecimal(longTermVulnerabilityRiskView.getLow()));
    }

    private static Integer getIntegerFromBigDecimal(BigDecimal bigDecimal) {
        return null == bigDecimal ? null : bigDecimal.intValue();
    }

    public UpgradeGuidanceRiskView() {
        this.critical = null;
        this.high = null;
        this.medium = null;
        this.low = null;
    }

    public UpgradeGuidanceRiskView(Integer critical, Integer high, Integer medium, Integer low) {
        this.critical = critical;
        this.high = high;
        this.medium = medium;
        this.low = low;
    }

    public Optional<Integer> getCritical() {
        return Optional.ofNullable(critical);
    }

    public Optional<Integer> getHigh() {
        return Optional.ofNullable(high);
    }

    public Optional<Integer> getMedium() {
        return Optional.ofNullable(medium);
    }

    public Optional<Integer> getLow() {
        return Optional.ofNullable(low);
    }

    public boolean hasVulnerabilities() {
        return (null != critical && critical > 0) ||
                   (null != high && high > 0) ||
                   (null != medium && medium > 0) ||
                   (null != low && low > 0);
    }

}
