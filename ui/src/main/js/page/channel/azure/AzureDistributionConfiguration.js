import PropTypes from 'prop-types';
import React, { useEffect } from 'react';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import TextInput from 'common/component/input/TextInput';
import CheckboxInput from 'common/component/input/CheckboxInput';
import EndpointSelectField from 'common/component/input/EndpointSelectField';
import { createReadRequest } from 'common/util/configurationRequestBuilder';
import { AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS } from 'page/channel/azure/AzureBoardsModel';
import { DISTRIBUTION_COMMON_FIELD_KEYS } from 'page/distribution/DistributionModel';

const AzureDistributionConfiguration = ({
    csrfToken, data, setData, errors, readonly
}) => {
    const readRequest = () => {
        const apiUrl = '/alert/api/configuration/azure-boards?pageNumber=0&pageSize=25';
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

    useEffect(() => {
        if (!FieldModelUtilities.hasValue(data, AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemType)) {
            setData(FieldModelUtilities.updateFieldModelSingleValue(data, AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemType, 'Task'));
        }
    }, []);
    return (
        <>
            <CheckboxInput
                id={AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.comment}
                name={AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.comment}
                label="Comment on Work Items"
                description="If selected, Alert will comment on Work Items it created when updates occur."
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                isChecked={FieldModelUtilities.getFieldModelBooleanValue(data, AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.comment)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.comment)}
                errorValue={errors.fieldErrors[AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.comment]}
            />
            <EndpointSelectField
                id={DISTRIBUTION_COMMON_FIELD_KEYS.channelGlobalConfigId}
                csrfToken={csrfToken}
                endpoint="/api/configuration/azure-boards"
                fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.channelGlobalConfigId}
                label="Azure Board"
                description="Select an Azure Board that will be used to create or update issues. Please note the options are limited to the first 25 Azure Boards."
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
                id={AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.project}
                label="Azure Project"
                description="The project name or id in Azure Boards."
                name={AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.project}
                required
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValue(data, AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.project)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.project)}
                errorValue={errors.fieldErrors[AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.project]}
            />
            <TextInput
                id={AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemType}
                label="Work Item Type"
                description="The work item type in Azure Boards."
                name={AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemType}
                required
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValue(data, AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemType)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemType)}
                errorValue={errors.fieldErrors[AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemType]}
            />
            <TextInput
                id={AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemCompleted}
                label="Work Item Completed State"
                description="The state a work item should result in if Alert receives a DELETE operation for it."
                name={AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemCompleted}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValue(data, AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemCompleted)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemCompleted)}
                errorValue={errors.fieldErrors[AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemCompleted]}
            />
            <TextInput
                id={AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemReopen}
                label="Work Item Reopen State"
                description="The state a work item should result in if Alert receives an ADD operation and the work item is in a completed state."
                name={AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemReopen}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValue(data, AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemReopen)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemReopen)}
                errorValue={errors.fieldErrors[AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS.workItemReopen]}
            />
        </>
    );
};

AzureDistributionConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    data: PropTypes.object.isRequired,
    setData: PropTypes.func.isRequired,
    errors: PropTypes.object.isRequired,
    readonly: PropTypes.bool.isRequired
};

export default AzureDistributionConfiguration;
