package com.synopsys.integration.azure.boards.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class FieldReferenceModelTest {
    private final String referenceName = "referenceName";
    private final String url = "http://url";

    private final FieldReferenceModel fieldReferenceModel = new FieldReferenceModel(referenceName, url);

    @Test
    public void getReferenceNameTest() {
        assertEquals(referenceName, fieldReferenceModel.getReferenceName());
    }

    @Test
    public void getUrlTest() {
        assertEquals(url, fieldReferenceModel.getUrl());
    }

    @Test
    public void emptyFieldReferenceModelTest() {
        FieldReferenceModel empty = new FieldReferenceModel();
        assertNull(empty.getReferenceName());
        assertNull(empty.getUrl());
    }
}
