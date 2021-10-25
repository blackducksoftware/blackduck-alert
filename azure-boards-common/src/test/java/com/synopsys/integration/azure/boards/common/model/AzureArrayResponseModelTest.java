package com.synopsys.integration.azure.boards.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class AzureArrayResponseModelTest {
    private final List<String> arrayResponses = List.of("one", "two", "three");

    @Test
    public void getCountAndValueTest() {
        AzureArrayResponseModel<String> azureArrayResponseModel = new AzureArrayResponseModel<>(arrayResponses.size(), arrayResponses);
        assertEquals(arrayResponses.size(), azureArrayResponseModel.getCount());
        assertEquals(arrayResponses, azureArrayResponseModel.getValue());
    }

    @Test
    public void getCountAndValueEmptyTest() {
        AzureArrayResponseModel<String> azureArrayResponseModel = new AzureArrayResponseModel<>();
        assertEquals(0, azureArrayResponseModel.getCount());
        assertTrue(azureArrayResponseModel.getValue().isEmpty());
    }
}
