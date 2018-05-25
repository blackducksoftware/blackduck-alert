package com.blackducksoftware.integration.hub.alert.startup;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AlertStartupPropertyTest {

    @Test
    public void testConstruction() {
        final Class<?> propertyClass = Integer.class;
        final String propertyKey = "propertyKey";
        final String fieldName = "fieldName";

        final AlertStartupProperty property = new AlertStartupProperty(propertyClass, propertyKey, fieldName);
        assertEquals(propertyClass, property.getPropertyClass());
        assertEquals(propertyKey, property.getPropertyKey());
        assertEquals(fieldName, property.getFieldName());
    }
}
