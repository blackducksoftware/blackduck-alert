package com.blackduck.integration.alert.component.audit.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.api.BaseController;

@RestController
@RequestMapping(AuditEntryController.AUDIT_FAILED_PATH)
public class AuditEntryController extends BaseController {
    public static final String AUDIT_FAILED_PATH = AlertRestConstants.BASE_PATH + "/audit/failed";

    private final AuditEntryActions actions;

    public AuditEntryController(final AuditEntryActions actions) {
        this.actions = actions;
    }

    @GetMapping
    public AuditEntryPageModel getPage(
        @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
        @RequestParam(value = "pageSize", required = false) Integer pageSize,
        @RequestParam(value = "searchTerm", required = false) String searchTerm,
        @RequestParam(value = "sortField", required = false) String sortField,
        @RequestParam(value = "sortOrder", required = false) String sortOrder
    ) {
        ActionResponse<AuditEntryPageModel> response = actions.get(
            pageNumber,
            pageSize,
            searchTerm,
            sortField,
            sortOrder
        );
        return ResponseFactory.createContentResponseFromAction(response);
    }

    @PutMapping(value = "/resend/{id}/")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendById(@PathVariable(value = "id") Long notificationId) {
        actions.resendNotification(notificationId);
    }

    @PutMapping(value = "/resend/{id}/job/{jobId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendByIdAndJobId(@PathVariable(value = "id") Long notificationId, @PathVariable(value = "jobId") UUID jobId) {
        actions.resendNotification(notificationId, jobId);
    }
}
