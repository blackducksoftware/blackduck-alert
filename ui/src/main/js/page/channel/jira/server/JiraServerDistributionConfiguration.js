import PropTypes from 'prop-types';
import React from 'react';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { JIRA_SERVER_DISTRIBUTION_FIELD_KEYS } from 'page/channel/jira/server/JiraServerModel';
import CheckboxInput from 'common/input/CheckboxInput';
import TextInput from 'common/input/TextInput';
import CollapsiblePane from 'common/CollapsiblePane';
import FieldMappingField from 'common/input/FieldMappingField';

const JiraServerDistributionConfiguration = ({
    data, setData, errors, readonly
}) => {
    if (!FieldModelUtilities.hasValue(data, JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueType)) {
        setData(FieldModelUtilities.updateFieldModelSingleValue(data, JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueType, 'Task'));
    }

    return (
        <>
            <CheckboxInput
                id={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.comment}
                name={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.comment}
                label="Add Comments"
                description="If true, this will add comments to the Jira ticket with data describing the latest change."
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                isChecked={FieldModelUtilities.getFieldModelBooleanValue(data, JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.comment)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.comment)}
                errorValue={errors.fieldErrors[JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.comment]}
            />
            <TextInput
                id={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueCreator}
                label="Issue Creator"
                description="The username of the Jira Server user to assign as the issue creator field of the Jira issue."
                name={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueCreator}
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValue(data, JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueCreator)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueCreator)}
                errorValue={errors.fieldErrors[JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueCreator]}
            />
            <TextInput
                id={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.project}
                label="Jira Project"
                description="The name or key of the Jira Project for which this job creates and/or updates Jira tickets."
                name={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.project}
                readOnly={readonly}
                required
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValue(data, JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.project)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.project)}
                errorValue={errors.fieldErrors[JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.project]}
            />
            <TextInput
                id={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueType}
                label="Issue Type"
                description="The issue type to open when creating an issue in Jira Server."
                name={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueType}
                readOnly={readonly}
                required
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValue(data, JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueType)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueType)}
                errorValue={errors.fieldErrors[JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueType]}
            />
            <TextInput
                id={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.resolveWorkflow}
                label="Resolve Transition"
                description="If a transition is listed (case sensitive), it will be used when resolving an issue. This will happen when Alert receives a DELETE operation from a provider. Note: This must be in the 'Done' status category."
                name={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.resolveWorkflow}
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValue(data, JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.resolveWorkflow)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.resolveWorkflow)}
                errorValue={errors.fieldErrors[JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.resolveWorkflow]}
            />
            <TextInput
                id={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.reopenWorkflow}
                label="Re-open Transition"
                description="If a transition is listed (case sensitive), it will be used when re-opening an issue. This will happen when Alert receives an ADD/UPDATE operation from a provider. Note: This must be in the 'To Do' status category."
                name={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.reopenWorkflow}
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValue(data, JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.reopenWorkflow)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.reopenWorkflow)}
                errorValue={errors.fieldErrors[JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.reopenWorkflow]}
            />
            <TextInput
                id={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueSummary}
                label="Issue Summary"
                description="The summary to use for each issue created. Placeholder values can be used to populate data from the message content. See Alert documentation 'Configuring Distribution Job in Alert' for a list of supported placeholder values"
                name={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueSummary}
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValue(data, JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueSummary)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueSummary)}
                errorValue={errors.fieldErrors[JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueSummary]}
            />
            <CollapsiblePane
                id="distribution-jira-server-advanced-configuration"
                title="Advanced Jira Configuration"
                expanded={false}
            >
                <FieldMappingField
                    id={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.fieldMapping}
                    label="Field Mapping"
                    description="Use this field to provide static values to Jira fields or map them to information from the notifications."
                    fieldMappingKey={JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.fieldMapping}
                    mappingTitle="Create Jira Field Mapping"
                    leftSideMapping="Jira Field"
                    rightSideMapping="Value"
                    readonly={readonly}
                    onChange={FieldModelUtilities.handleChange(data, setData)}
                    storedMappings={FieldModelUtilities.getFieldModelValues(data, JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.fieldMapping)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.fieldMapping)}
                    errorValue={errors.fieldErrors[JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.fieldMapping]}
                />
            </CollapsiblePane>
        </>
    );
};

JiraServerDistributionConfiguration.propTypes = {
    data: PropTypes.object.isRequired,
    setData: PropTypes.func.isRequired,
    errors: PropTypes.object.isRequired,
    readonly: PropTypes.bool.isRequired
};

export default JiraServerDistributionConfiguration;
