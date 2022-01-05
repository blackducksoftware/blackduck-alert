/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.provider.processing;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.CustomFunctionAction;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOptions;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.IssueTrackerChannelKey;

@Component
public class ProcessingSelectCustomFunctionAction extends CustomFunctionAction<LabelValueSelectOptions> {
    private final List<String> issueTrackerChannelKeys;

    @Autowired
    public ProcessingSelectCustomFunctionAction(AuthorizationManager authorizationManager, List<IssueTrackerChannelKey> issueTrackerChannelKeys) {
        super(authorizationManager);
        this.issueTrackerChannelKeys = issueTrackerChannelKeys.stream()
            .map(IssueTrackerChannelKey::getUniversalKey)
            .collect(Collectors.toList());
    }

    @Override
    public ActionResponse<LabelValueSelectOptions> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        String channelName = fieldModel.getFieldValue(ChannelDescriptor.KEY_CHANNEL_NAME).orElse("");
        List<LabelValueSelectOption> options = Arrays.stream(ProcessingType.values())
            .filter(processingType -> this.shouldInclude(processingType, channelName))
            .map(processingType -> new LabelValueSelectOption(processingType.getLabel(), processingType.name()))
            .collect(Collectors.toList());

        LabelValueSelectOptions optionList = new LabelValueSelectOptions(options);
        return new ActionResponse<>(HttpStatus.OK, optionList);
    }

    @Override
    protected Collection<AlertFieldStatus> validateRelatedFields(FieldModel fieldModel) {
        // No validation needed
        return Set.of();
    }

    private boolean shouldInclude(ProcessingType processingType, String channelName) {
        // We do not want to expose the summary processing type as an option for issue tracker channels
        return !(issueTrackerChannelKeys.contains(channelName) && processingType == ProcessingType.SUMMARY);
    }

}
