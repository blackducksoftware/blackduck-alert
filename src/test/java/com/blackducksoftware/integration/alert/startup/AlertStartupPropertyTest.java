package com.blackducksoftware.integration.alert.startup;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AlertStartupPropertyTest {

    @Test
    public void testConstruction() {
        final String propertyKey = "propertyKey";
        final String fieldName = "fieldName";

        final AlertStartupProperty property = new AlertStartupProperty(propertyKey, fieldName);
        assertEquals(propertyKey, property.getPropertyKey());
        assertEquals(fieldName, property.getFieldName());
    }
}
