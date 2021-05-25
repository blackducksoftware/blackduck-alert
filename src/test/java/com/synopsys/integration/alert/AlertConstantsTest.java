package com.synopsys.integration.alert;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.common.model.AlertConstants;

import javassist.Modifier;

public class AlertConstantsTest {

    @Test
    public void testInstantiationException() throws NoSuchMethodException, SecurityException {
        Constructor<AlertConstants> constuctor = AlertConstants.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(constuctor.getModifiers()));

        constuctor.setAccessible(true);
        try {
            constuctor.newInstance();
            fail();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
            fail();
        } catch (IllegalArgumentException e) {
            fail();
        } catch (InvocationTargetException e) {
        }
    }

}
