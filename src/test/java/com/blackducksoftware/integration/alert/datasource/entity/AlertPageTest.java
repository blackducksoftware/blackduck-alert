package com.blackducksoftware.integration.alert.datasource.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.junit.Test;

import com.blackducksoftware.integration.alert.audit.controller.AlertPage;
import com.blackducksoftware.integration.alert.audit.repository.AuditEntryEntity;

public class AlertPageTest {

    @Test
    public void testNullContent() throws JSONException {
        final int totalPages = 0;
        final int currentPage = 0;
        final int pageSize = 0;
        final List<AuditEntryEntity> contentList = null;

        final AlertPage<AuditEntryEntity> restModel = new AlertPage<>(totalPages, currentPage, pageSize, contentList);

        assertEquals(totalPages, restModel.getTotalPages());
        assertEquals(currentPage, restModel.getCurrentPage());
        assertEquals(pageSize, restModel.getPageSize());
        assertNull(restModel.getContentList());
    }

    @Test
    public void testPageContent() throws JSONException {
        final int totalPages = 2;
        final int currentPage = 1;
        final int pageSize = 2;
        final List<AuditEntryEntity> contentList = new ArrayList<>();
        final AuditEntryEntity item = new AuditEntryEntity();
        contentList.add(item);
        contentList.add(item);

        final AlertPage<AuditEntryEntity> restModel = new AlertPage<>(totalPages, currentPage, pageSize, contentList);

        assertEquals(totalPages, restModel.getTotalPages());
        assertEquals(currentPage, restModel.getCurrentPage());
        assertEquals(pageSize, restModel.getPageSize());
        assertEquals(contentList, restModel.getContentList());
    }

}
