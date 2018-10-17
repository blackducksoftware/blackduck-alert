package com.synopsys.integration.alert.workflow.startup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AlertStartupPropertyTest {

    @Test
    public void testConstruction() {
        final String propertyKey = "propertyKey";
        final String fieldName = "fieldName";

        final AlertStartupProperty property = new AlertStartupProperty(propertyKey, fieldName);
        assertEquals(propertyKey, property.getPropertyKey());
        assertEquals(fieldName, property.getFieldName());
        assertFalse(property.isAlwaysOverride());
    }

    @Test
    public void testConstructionForOverride() {
        final String propertyKey = "propertyKey";
        final String fieldName = "fieldName";

        final AlertStartupProperty property = new AlertStartupProperty(propertyKey, fieldName, true);
        assertEquals(propertyKey, property.getPropertyKey());
        assertEquals(fieldName, property.getFieldName());
        assertTrue(property.isAlwaysOverride());
    }
}
