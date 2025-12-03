import PropTypes from 'prop-types';
import React from 'react';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { JIRA_SERVER_DISTRIBUTION_FIELD_KEYS } from 'page/channel/jira/server/JiraServerModel';
import CheckboxInput from 'common/component/input/CheckboxInput';
import TextInput from 'common/component/input/TextInput';
import CollapsiblePane from 'common/component/CollapsiblePane';
import { DISTRIBUTION_COMMON_FIELD_KEYS } from 'page/distribution/DistributionModel';
import EndpointSelectField from 'common/component/input/EndpointSelectField';
import { createReadRequest } from 'common/util/configurationRequestBuilder';
import JiraFieldMapDistributionTable from 'page/channel/jira/common/JiraFieldMapDistributionTable';

const JiraServerDistributionConfiguration = ({
    csrfToken, data, setData, errors, readonly
}) => {
    const readRequest = () => {
        const apiUrl = '/alert/api/configuration/jira_server?pageNumber=0&pageSize=25';
        return createReadRequest(apiUrl, csrfToken);
    };

    const convertDataToOptions = (responseData) => {
        const { models } = responseData;
        return models.map((configurationModel) => {
            const { id: configId, name } = configurationModel;
            return {
                key: configId,
                label: name,
                value: configId
            };
        });
    };
    if (!FieldModelUtilities.hasValue(data, JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueType)) {
        setData(FieldModelUtilities.updateFieldModelSingleValue(data, JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.issueType, 'Task'));
    }

    const storedMappings = FieldModelUtilities.getFieldModelValues(data, JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.fieldMapping);
    const tableData = storedMappings.map((mapping) => JSON.parse(mapping));

    function updateModel(model) {
        const updatedFieldModel = model.map((fieldModel) => JSON.stringify(fieldModel));
        setData(FieldModelUtilities.updateFieldModelValues(data, JIRA_SERVER_DISTRIBUTION_FIELD_KEYS.fieldMapping, updatedFieldModel));
    }

    // TODO make configuration select searchable but requires support in the backend
    return (
        <>
            <EndpointSelectField
                id={DISTRIBUTION_COMMON_FIELD_KEYS.channelGlobalConfigId}
                csrfToken={csrfToken}
                endpoint="/api/configuration/jira_server"
                fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.channelGlobalConfigId}
                label="Jira Server"
                description="Select a Jira server that will be used to create or update issues. Please note the options are limited to the first 25 Jira Servers."
                readOnly={readonly}
                required
                readOptionsRequest={readRequest}
                convertDataToOptions={convertDataToOptions}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelValues(data, DISTRIBUTION_COMMON_FIELD_KEYS.channelGlobalConfigId)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.channelGlobalConfigId)}
                errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.channelGlobalConfigId]}
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
                <JiraFieldMapDistributionTable initialData={tableData} onFieldMappingUpdate={updateModel} />
            </CollapsiblePane>
        </>
    );
};

JiraServerDistributionConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    data: PropTypes.object.isRequired,
    setData: PropTypes.func.isRequired,
    errors: PropTypes.object.isRequired,
    readonly: PropTypes.bool.isRequired
};

export default JiraServerDistributionConfiguration;
