package com.synopsys.integration.alert.common.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AlertPagedModelTest {
    int testTotalPages = 1;
    int testCurrentPage = 2;
    int testPageSize = 3;
    List<AlertSerializableModel> testContent;

    @BeforeEach
    public void init() {
        testContent = new ArrayList<>();
        AlertSerializableModel testAlertSerializableModel = Mockito.mock(AlertSerializableModel.class);
        testContent.add(testAlertSerializableModel);
    }

    @Test
    public void getTotalPagesTest() {
        AlertPagedModel<AlertSerializableModel> testAlertPagedModel = new AlertPagedModel<>(testTotalPages, testCurrentPage, testPageSize, testContent);
        assertEquals(testTotalPages, testAlertPagedModel.getTotalPages());
    }

    @Test
    public void getCurrentPageTest() {
        AlertPagedModel<AlertSerializableModel> testAlertPagedModel = new AlertPagedModel<>(testTotalPages, testCurrentPage, testPageSize, testContent);
        assertEquals(testCurrentPage, testAlertPagedModel.getCurrentPage());
    }

    @Test
    public void getPageSizeTest() {
        AlertPagedModel<AlertSerializableModel> testAlertPagedModel = new AlertPagedModel<>(testTotalPages, testCurrentPage, testPageSize, testContent);
        assertEquals(testPageSize, testAlertPagedModel.getPageSize());
    }

    @Test
    public void getContentTest() {
        AlertPagedModel<AlertSerializableModel> testAlertPagedModel = new AlertPagedModel<>(testTotalPages, testCurrentPage, testPageSize, testContent);
        assertEquals(testContent, testAlertPagedModel.getContent());
    }
}