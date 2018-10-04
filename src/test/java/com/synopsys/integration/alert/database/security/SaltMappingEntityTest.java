package com.synopsys.integration.alert.database.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SaltMappingEntityTest {

    @Test
    public void testDefaultConstructor() {
        final SaltMappingEntity entity = new SaltMappingEntity();
        assertNull(entity.getPropertyKey());
        assertNull(entity.getSalt());
    }

    @Test
    public void testValues() {
        final String propertyKey = "propertyKey";
        final String salt = "propertySalt";
        final SaltMappingEntity entity = new SaltMappingEntity(propertyKey, salt);
        assertEquals(propertyKey, entity.getPropertyKey());
        assertEquals(salt, entity.getSalt());
    }
}
