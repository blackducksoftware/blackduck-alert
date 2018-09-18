package com.synopsys.integration.alert.common.field;

import org.junit.Assert;
import org.junit.Test;

public class FieldTest {
    @Test
    public void getAndSetTest() {
        String fieldName = null;
        String fieldLabel = null;
        final Field field = new Field(fieldName, fieldLabel) {};

        Assert.assertEquals(null, field.getFieldKey());
        Assert.assertEquals(null, field.getLabel());

        fieldName = "cool field bro";
        fieldLabel = "cool label bro";
        field.setFieldKey(fieldName);
        field.setLabel(fieldLabel);
        Assert.assertEquals(fieldName, field.getFieldKey());
        Assert.assertEquals(fieldLabel, field.getLabel());
    }
}
