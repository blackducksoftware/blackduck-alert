package com.synopsys.integration.alert.processor.api.extract.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;

class ComponentUpgradeGuidanceTest {
    private final LinkableItem shortTermGuidance = new LinkableItem("Short Term Guidance", "1.1.0");
    private final LinkableItem longTermGuidance = new LinkableItem("Long Term Guidance", "2.0.0");

    @Test
    void getShortTermUpgradeGuidanceTest() {
        UpgradeGuidanceDetails shortTermUpgradeGuidanceDetails = new UpgradeGuidanceDetails(shortTermGuidance, null, null, null);
        UpgradeGuidanceDetails longTermUpgradeGuidanceDetails = new UpgradeGuidanceDetails(longTermGuidance, null, null, null);
        ComponentUpgradeGuidance componentUpgradeGuidance = new ComponentUpgradeGuidance(shortTermUpgradeGuidanceDetails, longTermUpgradeGuidanceDetails, null, null, null);
        assertTrue(componentUpgradeGuidance.getShortTermUpgradeGuidance().isPresent());
        assertEquals(shortTermGuidance, componentUpgradeGuidance.getShortTermUpgradeGuidance().get());
    }

    @Test
    void getLongTermUpgradeGuidanceTest() {
        UpgradeGuidanceDetails shortTermUpgradeGuidanceDetails = new UpgradeGuidanceDetails(shortTermGuidance, null, null, null);
        UpgradeGuidanceDetails longTermUpgradeGuidanceDetails = new UpgradeGuidanceDetails(longTermGuidance, null, null, null);
        ComponentUpgradeGuidance componentUpgradeGuidance = new ComponentUpgradeGuidance(shortTermUpgradeGuidanceDetails, longTermUpgradeGuidanceDetails, null, null, null);
        assertTrue(componentUpgradeGuidance.getLongTermUpgradeGuidance().isPresent());
        assertEquals(longTermGuidance, componentUpgradeGuidance.getLongTermUpgradeGuidance().get());
    }

    @Test
    void getNullUpgradeGuidance() {
        ComponentUpgradeGuidance componentUpgradeGuidance = ComponentUpgradeGuidance.none();
        assertTrue(componentUpgradeGuidance.getShortTermUpgradeGuidance().isEmpty());
        assertTrue(componentUpgradeGuidance.getLongTermUpgradeGuidance().isEmpty());
    }
}
