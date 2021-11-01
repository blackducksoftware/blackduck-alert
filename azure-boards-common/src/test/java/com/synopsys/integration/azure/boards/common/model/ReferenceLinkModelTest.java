package com.synopsys.integration.azure.boards.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class ReferenceLinkModelTest {
    @Test
    public void getNameTest() {
        String href = "http://example";
        ReferenceLinkModel referenceLinkModel = new ReferenceLinkModel(href);
        assertEquals(href, referenceLinkModel.getHref());
    }

    @Test
    public void emptyFieldReferenceModelTest() {
        ReferenceLinkModel referenceLinkModel = new ReferenceLinkModel();
        assertNull(referenceLinkModel.getHref());
    }
}
