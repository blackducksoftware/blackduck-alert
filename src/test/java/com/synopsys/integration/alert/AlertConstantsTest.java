package com.synopsys.integration.alert;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

import javassist.Modifier;

public class AlertConstantsTest {

    @Test
    public void testInstantiationException() throws NoSuchMethodException, SecurityException {
        final Constructor<AlertConstants> constuctor = AlertConstants.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(constuctor.getModifiers()));

        constuctor.setAccessible(true);
        try {
            constuctor.newInstance();
            fail();
        } catch (final InstantiationException e) {
        } catch (final IllegalAccessException e) {
            fail();
        } catch (final IllegalArgumentException e) {
            fail();
        } catch (final InvocationTargetException e) {
        }
    }
}
