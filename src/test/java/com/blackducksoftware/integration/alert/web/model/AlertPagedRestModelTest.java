package com.blackducksoftware.integration.alert.web.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.junit.Test;

import com.blackducksoftware.integration.alert.audit.controller.AuditEntryRestModel;
import com.blackducksoftware.integration.alert.web.model.AlertPagedRestModel;

public class AlertPagedRestModelTest {

    @Test
    public void testRestModelNullContent() throws JSONException {
        final int totalPages = 0;
        final int currentPage = 0;
        final int pageSize = 0;
        final List<AuditEntryRestModel> contentList = null;

        final AlertPagedRestModel<AuditEntryRestModel> restModel = new AlertPagedRestModel<>(totalPages, currentPage, pageSize, contentList);

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
        final List<AuditEntryRestModel> contentList = new ArrayList<>();
        final AuditEntryRestModel item = new AuditEntryRestModel();
        contentList.add(item);
        contentList.add(item);

        final AlertPagedRestModel<AuditEntryRestModel> restModel = new AlertPagedRestModel<>(totalPages, currentPage, pageSize, contentList);

        assertEquals(totalPages, restModel.getTotalPages());
        assertEquals(currentPage, restModel.getCurrentPage());
        assertEquals(pageSize, restModel.getPageSize());
        assertEquals(contentList, restModel.getContent());
    }
}
