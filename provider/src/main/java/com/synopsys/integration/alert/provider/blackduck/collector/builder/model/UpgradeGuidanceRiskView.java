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
            return new UpgradeGuidanceRiskView(null, null, null, null);
        }
        return new UpgradeGuidanceRiskView(getIntegerFromBigDecimal(shortTermVulnerabilityRiskView.getCritical()), getIntegerFromBigDecimal(shortTermVulnerabilityRiskView.getHigh()),
            getIntegerFromBigDecimal(shortTermVulnerabilityRiskView.getMedium()), getIntegerFromBigDecimal(shortTermVulnerabilityRiskView.getLow()));
    }

    public static UpgradeGuidanceRiskView fromLongTermVulnerabilityRiskView(ComponentVersionUpgradeGuidanceLongTermVulnerabilityRiskView longTermVulnerabilityRiskView) {
        if (null == longTermVulnerabilityRiskView) {
            return new UpgradeGuidanceRiskView(null, null, null, null);
        }
        return new UpgradeGuidanceRiskView(getIntegerFromBigDecimal(longTermVulnerabilityRiskView.getCritical()), getIntegerFromBigDecimal(longTermVulnerabilityRiskView.getHigh()),
            getIntegerFromBigDecimal(longTermVulnerabilityRiskView.getMedium()), getIntegerFromBigDecimal(longTermVulnerabilityRiskView.getLow()));
    }

    private static Integer getIntegerFromBigDecimal(BigDecimal bigDecimal) {
        return null == bigDecimal ? null : bigDecimal.intValue();
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
