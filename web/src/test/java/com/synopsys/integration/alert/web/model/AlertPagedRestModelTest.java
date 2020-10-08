package com.synopsys.integration.alert.web.model;

import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public class AlertPagedRestModelTest {

    @Test
    public void testRestModelNullContent() throws JSONException {
        final int totalPages = 0;
        final int currentPage = 0;
        final int pageSize = 0;
        List<AuditEntryModel> contentList = null;

        AlertPagedModel<AuditEntryModel> restModel = new AlertPagedModel<>(totalPages, currentPage, pageSize, contentList);

        Assert.assertEquals(totalPages, restModel.getTotalPages());
        Assert.assertEquals(currentPage, restModel.getCurrentPage());
        Assert.assertEquals(pageSize, restModel.getPageSize());
        assertNull(restModel.getContent());
    }

    @Test
    public void testRestModel() {
        final int totalPages = 2;
        final int currentPage = 1;
        final int pageSize = 2;
        List<AuditEntryModel> contentList = new ArrayList<>();
        AuditEntryModel item = new AuditEntryModel();
        contentList.add(item);
        contentList.add(item);

        AlertPagedModel<AuditEntryModel> restModel = new AlertPagedModel<>(totalPages, currentPage, pageSize, contentList);

        Assert.assertEquals(totalPages, restModel.getTotalPages());
        Assert.assertEquals(currentPage, restModel.getCurrentPage());
        Assert.assertEquals(pageSize, restModel.getPageSize());
        Assert.assertEquals(contentList, restModel.getContent());
    }
}
