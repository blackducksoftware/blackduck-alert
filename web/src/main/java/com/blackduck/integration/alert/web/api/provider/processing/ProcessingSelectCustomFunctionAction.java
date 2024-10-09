package com.blackduck.integration.alert.web.api.provider.processing;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.api.descriptor.model.IssueTrackerChannelKey;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.action.CustomFunctionAction;
import com.blackduck.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackduck.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.blackduck.integration.alert.common.descriptor.config.field.LabelValueSelectOptions;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.rest.HttpServletContentWrapper;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;

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
