package com.synopsys.integration.alert.channel.jira.cloud.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.CustomFunctionAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.FieldValidationUtility;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class JiraCloudFieldMappingFunctionAction extends CustomFunctionAction<FieldMappingResponse> {

    @Autowired
    public JiraCloudFieldMappingFunctionAction(AuthorizationManager authorizationManager, DescriptorMap descriptorMap, FieldValidationUtility fieldValidationUtility) {
        super(JiraCloudDescriptor.KEY_FIELD_MAPPING, authorizationManager, descriptorMap, fieldValidationUtility);
    }

    @Override
    public ActionResponse<FieldMappingResponse> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) throws IntegrationException {
        // FIXME populate this with actual values once the UI component has been properly implemented
        List<LabelValueSelectOption> leftSideOptions = List.of(
            new LabelValueSelectOption("First Option Label", "FirstOptionKey"),
            new LabelValueSelectOption("Second Option Label", "SecondOptionKey"),
            new LabelValueSelectOption("This is the left", "LeftSideKey")
        );
        List<LabelValueSelectOption> rightSideOptions = List.of(
            new LabelValueSelectOption("First Option Label right side", "RightSideKey"),
            new LabelValueSelectOption("Another option", "AnotherKey")
        );
        FieldMappingResponse fieldMappingResponse = new FieldMappingResponse(leftSideOptions, rightSideOptions);
        return new ActionResponse<>(HttpStatus.OK, fieldMappingResponse);
    }
}
