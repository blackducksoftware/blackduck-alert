package com.synopsys.integration.alert.component.audit.web;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingFailedAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.audit.AuditDescriptorKey;

@Component
public class AuditEntryActions {
    private final AuditDescriptorKey descriptorKey;
    private final AuthorizationManager authorizationManager;
    private final ProcessingFailedAccessor auditFailureAccessor;

    @Autowired
    public AuditEntryActions(AuditDescriptorKey descriptorKey, AuthorizationManager authorizationManager, ProcessingFailedAccessor auditFailureAccessor) {
        this.descriptorKey = descriptorKey;
        this.authorizationManager = authorizationManager;
        this.auditFailureAccessor = auditFailureAccessor;
    }

    public ActionResponse<AuditEntryPageModel> get(Integer pageNumber, Integer pageSize, String searchTerm, String sortField, String sortOrder) {
        if (!authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL, descriptorKey)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }
        Integer page = ObjectUtils.defaultIfNull(pageNumber, AlertPagedModel.DEFAULT_PAGE_NUMBER);
        Integer size = ObjectUtils.defaultIfNull(pageSize, AlertPagedModel.DEFAULT_PAGE_SIZE);
        AuditEntryPageModel pagedRestModel = auditFailureAccessor.getPageOfAuditEntries(
            page,
            size,
            searchTerm,
            sortField,
            sortOrder
        );
        return new ActionResponse<>(HttpStatus.OK, pagedRestModel);
    }
}
