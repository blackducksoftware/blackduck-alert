package com.synopsys.integration.alert.channel.jira.cloud.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.common.rest.api.AbstractFunctionController;

@RestController
@RequestMapping(JiraCloudFieldMappingFunctionController.JIRA_CLOUD_FUNCTION_URL)
public class JiraCloudFieldMappingFunctionController extends AbstractFunctionController<FieldMappingResponse> {
    public static final String JIRA_CLOUD_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + JiraCloudDescriptor.KEY_FIELD_MAPPING;

    public JiraCloudFieldMappingFunctionController(JiraCloudFieldMappingFunctionAction jiraCloudFieldMappingFunctionAction) {
        super(jiraCloudFieldMappingFunctionAction);
    }
}
