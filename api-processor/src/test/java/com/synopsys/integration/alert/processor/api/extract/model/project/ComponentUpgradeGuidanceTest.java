package com.synopsys.integration.alert.processor.api.extract.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class ComponentUpgradeGuidanceTest {
    private final LinkableItem shortTermGuidance = new LinkableItem("Short Term Guidance", "1.1.0");
    private final LinkableItem longTermGuidance = new LinkableItem("Long Term Guidance", "2.0.0");

    @Test
    public void getShortTermUpgradeGuidanceTest() {
        ComponentUpgradeGuidance componentUpgradeGuidance = new ComponentUpgradeGuidance(shortTermGuidance, longTermGuidance);
        assertTrue(componentUpgradeGuidance.getShortTermUpgradeGuidance().isPresent());
        assertEquals(shortTermGuidance, componentUpgradeGuidance.getShortTermUpgradeGuidance().get());
    }

    @Test
    public void getLongTermUpgradeGuidanceTest() {
        ComponentUpgradeGuidance componentUpgradeGuidance = new ComponentUpgradeGuidance(shortTermGuidance, longTermGuidance);
        assertTrue(componentUpgradeGuidance.getLongTermUpgradeGuidance().isPresent());
        assertEquals(longTermGuidance, componentUpgradeGuidance.getLongTermUpgradeGuidance().get());
    }

    @Test
    public void getNullUpgradeGuidance() {
        ComponentUpgradeGuidance componentUpgradeGuidance = ComponentUpgradeGuidance.none();
        assertTrue(componentUpgradeGuidance.getShortTermUpgradeGuidance().isEmpty());
        assertTrue(componentUpgradeGuidance.getLongTermUpgradeGuidance().isEmpty());
    }
}
