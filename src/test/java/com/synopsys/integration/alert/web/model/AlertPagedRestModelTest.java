package com.synopsys.integration.alert.web.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.junit.Test;

import com.synopsys.integration.alert.web.audit.AuditEntryModel;

public class AlertPagedRestModelTest {

    @Test
    public void testRestModelNullContent() throws JSONException {
        final int totalPages = 0;
        final int currentPage = 0;
        final int pageSize = 0;
        final List<AuditEntryModel> contentList = null;

        final AlertPagedModel<AuditEntryModel> restModel = new AlertPagedModel<>(totalPages, currentPage, pageSize, contentList);

        assertEquals(totalPages, restModel.getTotalPages());
        assertEquals(currentPage, restModel.getCurrentPage());
        assertEquals(pageSize, restModel.getPageSize());
        assertNull(restModel.getContent());
    }

    @Test
    public void testRestModel() throws JSONException {
        final int totalPages = 2;
        final int currentPage = 1;
        final int pageSize = 2;
        final List<AuditEntryModel> contentList = new ArrayList<>();
        final AuditEntryModel item = new AuditEntryModel();
        contentList.add(item);
        contentList.add(item);

        final AlertPagedModel<AuditEntryModel> restModel = new AlertPagedModel<>(totalPages, currentPage, pageSize, contentList);

        assertEquals(totalPages, restModel.getTotalPages());
        assertEquals(currentPage, restModel.getCurrentPage());
        assertEquals(pageSize, restModel.getPageSize());
        assertEquals(contentList, restModel.getContent());
    }
}
