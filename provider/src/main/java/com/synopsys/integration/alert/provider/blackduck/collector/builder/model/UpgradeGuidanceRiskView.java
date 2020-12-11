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
            return new UpgradeGuidanceRiskView(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        return new UpgradeGuidanceRiskView(shortTermVulnerabilityRiskView.getCritical(), shortTermVulnerabilityRiskView.getHigh(), shortTermVulnerabilityRiskView.getMedium(), shortTermVulnerabilityRiskView.getLow());
    }

    public static UpgradeGuidanceRiskView fromLongTermVulnerabilityRiskView(ComponentVersionUpgradeGuidanceLongTermVulnerabilityRiskView longTermVulnerabilityRiskView) {
        if (null == longTermVulnerabilityRiskView) {
            return new UpgradeGuidanceRiskView(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        return new UpgradeGuidanceRiskView(longTermVulnerabilityRiskView.getCritical(), longTermVulnerabilityRiskView.getHigh(), longTermVulnerabilityRiskView.getMedium(), longTermVulnerabilityRiskView.getLow());
    }

    public UpgradeGuidanceRiskView(BigDecimal critical, BigDecimal high, BigDecimal medium, BigDecimal low) {
        this.critical = getIntegerFromBigDecimal(critical);
        this.high = getIntegerFromBigDecimal(high);
        this.medium = getIntegerFromBigDecimal(medium);
        this.low = getIntegerFromBigDecimal(low);
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

    private Integer getIntegerFromBigDecimal(BigDecimal bigDecimal) {
        return null == bigDecimal ? null : bigDecimal.intValue();
    }

}
