package com.blackduck.integration.alert.common.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;

public class AlertPagedModelTest {
    private final int testTotalPages = 1;
    private final int testCurrentPage = 2;
    private final int testPageSize = 3;
    private List<AlertSerializableModel> testContent;
    private AlertPagedModel<AlertSerializableModel> testAlertPagedModel;

    @BeforeEach
    public void init() {
        testContent = new ArrayList<>();
        AlertSerializableModel testAlertSerializableModel = Mockito.mock(AlertSerializableModel.class);
        testContent.add(testAlertSerializableModel);
        testAlertPagedModel = new AlertPagedModel(testTotalPages, testCurrentPage, testPageSize, testContent);
    }

    @Test
    public void getTotalPagesTest() {
        assertEquals(testTotalPages, testAlertPagedModel.getTotalPages());
    }

    @Test
    public void getCurrentPageTest() {
        assertEquals(testCurrentPage, testAlertPagedModel.getCurrentPage());
    }

    @Test
    public void getPageSizeTest() {
        assertEquals(testPageSize, testAlertPagedModel.getPageSize());
    }

    @Test
    public void getContentTest() {
        assertEquals(testContent, testAlertPagedModel.getModels());
    }

}
