package com.synopsys.integration.alert.api.common.model;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

public class AlertConstantsTest {
    @Test
    public void privateConstructorTest() {
        for (Constructor<?> constructor : AlertConstants.class.getConstructors()) {
            try {
                constructor.newInstance();
                fail("Expected exception to be thrown");
            } catch (InstantiationException e) {
                // Pass
            } catch (Exception e) {
                fail("Unexpected exception type", e);
            }
        }
    }

}
