package com.synopsys.integration.alert.provider.blackduck.collector.builder.model;

import java.math.BigDecimal;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceLongTermVulnerabilityRiskView;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceShortTermVulnerabilityRiskView;

public class UpgradeGuidanceRiskView extends AlertSerializableModel {
    private BigDecimal critical;
    private BigDecimal high;
    private BigDecimal medium;
    private BigDecimal low;
    private boolean hasCriticalVulnerabilities;
    private boolean hasHighVulnerabilities;
    private boolean hasMediumVulnerabilities;
    private boolean hasLowVulnerabilities;
    private boolean hasVulnerabilities;

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
        this.critical = critical;
        this.high = high;
        this.medium = medium;
        this.low = low;
        hasCriticalVulnerabilities = isBigDecimalGreaterThanZero(critical);
        hasHighVulnerabilities = isBigDecimalGreaterThanZero(high);
        hasMediumVulnerabilities = isBigDecimalGreaterThanZero(medium);
        hasLowVulnerabilities = isBigDecimalGreaterThanZero(low);
        hasVulnerabilities = hasCriticalVulnerabilities || hasHighVulnerabilities || hasMediumVulnerabilities || hasLowVulnerabilities;

    }

    public BigDecimal getCritical() {
        return critical;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getMedium() {
        return medium;
    }

    public BigDecimal getLow() {
        return low;
    }

    public boolean hasCriticalVulnerabilities() {
        return hasCriticalVulnerabilities;
    }

    public boolean hasHighVulnerabilities() {
        return hasHighVulnerabilities;
    }

    public boolean hasMediumVulnerabilities() {
        return hasMediumVulnerabilities;
    }

    public boolean hasLowVulnerabilities() {
        return hasLowVulnerabilities;
    }

    public boolean hasVulnerabilities() {
        return hasVulnerabilities;
    }

    private boolean isBigDecimalGreaterThanZero(BigDecimal bigDecimal) {
        return bigDecimal != null && BigDecimal.ZERO.compareTo(bigDecimal) < 0;
    }

}
