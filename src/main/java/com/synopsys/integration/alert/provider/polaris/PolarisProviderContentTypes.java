package com.synopsys.integration.alert.provider.polaris;

import java.util.List;

import com.synopsys.integration.alert.common.enumeration.FieldContentIdentifier;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.alert.provider.polaris.model.AlertPolarisNotificationTypeEnum;

public class PolarisProviderContentTypes {
    public static final String LABEL_PROJECT_NAME = "Project";
    public static final String LABEL_ISSUE_TYPE = "Issue Type";
    public static final String LABEL_ISSUE_COUNT = "Updated Issue Count";

    private static final JsonField<String> PROJECT_NAME_FIELD = JsonField.createStringField(JsonField.createJsonPath(JsonField.FORMAT_SINGLE_REPLACEMENT), "projectName", FieldContentIdentifier.TOPIC, LABEL_PROJECT_NAME);
    private static final JsonField<String> PROJECT_LINK_FIELD = JsonField.createStringField(JsonField.createJsonPath(JsonField.FORMAT_SINGLE_REPLACEMENT), "projectLink", FieldContentIdentifier.TOPIC_URL,
        LABEL_PROJECT_NAME + JsonField.LABEL_URL_SUFFIX);
    private static final JsonField<String> ISSUE_TYPE_FIELD = JsonField.createStringField(JsonField.createJsonPath(JsonField.FORMAT_SINGLE_REPLACEMENT), "issueType", FieldContentIdentifier.SUB_TOPIC, LABEL_ISSUE_TYPE);
    private static final JsonField<Integer> ISSUE_COUNT_FIELD = JsonField.createIntegerField(JsonField.createJsonPath(JsonField.FORMAT_SINGLE_REPLACEMENT), "count", FieldContentIdentifier.CATEGORY_ITEM, LABEL_ISSUE_COUNT);

    private static final List<JsonField<?>> POLARIS_FIELDS = List.of(PROJECT_NAME_FIELD, PROJECT_LINK_FIELD, ISSUE_TYPE_FIELD, ISSUE_COUNT_FIELD);

    public static final ProviderContentType ISSUE_COUNT_INCREASED = new ProviderContentType(AlertPolarisNotificationTypeEnum.ISSUE_COUNT_INCREASED.name(), POLARIS_FIELDS);
    public static final ProviderContentType ISSUE_COUNT_DECREASED = new ProviderContentType(AlertPolarisNotificationTypeEnum.ISSUE_COUNT_DECREASED.name(), POLARIS_FIELDS);

}
