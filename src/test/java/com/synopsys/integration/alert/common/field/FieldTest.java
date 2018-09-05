package com.synopsys.integration.alert.common.field;

import org.junit.Assert;
import org.junit.Test;

public class FieldTest {
    @Test
    public void getAndSetTest() {
        String fieldName = null;
        final Field field = new Field(fieldName) {};

        Assert.assertEquals(null, field.getFieldKey());

        fieldName = "cool filed bro";
        field.setFieldKey(fieldName);
        Assert.assertEquals(fieldName, field.getFieldKey());
    }
}
