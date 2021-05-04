import PropTypes from 'prop-types';
import React, { useEffect } from 'react';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import TextInput from 'field/input/TextInput';
import CheckboxInput from 'field/input/CheckboxInput';
import { AZURE_DISTRIBUTION_FIELD_KEYS } from 'distribution/channels/azure/AzureModel';

const AzureDistributionConfiguration = ({
    data, setData, errors, readonly
}) => {
    useEffect(() => {
        setData(FieldModelUtilities.updateFieldModelSingleValue(data, AZURE_DISTRIBUTION_FIELD_KEYS.workItemType, 'Task'));
    }, []);
    return (
        <>
            <CheckboxInput
                name={AZURE_DISTRIBUTION_FIELD_KEYS.comment}
                label="Comment on Work Items"
                description="If selected, Alert will comment on Work Items it created when updates occur."
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                isChecked={FieldModelUtilities.getFieldModelBooleanValue(data, AZURE_DISTRIBUTION_FIELD_KEYS.comment)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_DISTRIBUTION_FIELD_KEYS.comment)}
                errorValue={errors[AZURE_DISTRIBUTION_FIELD_KEYS.comment]}
            />
            <TextInput
                id={AZURE_DISTRIBUTION_FIELD_KEYS.project}
                label="Azure Project"
                description="The project name or id in Azure Boards."
                name={AZURE_DISTRIBUTION_FIELD_KEYS.project}
                required
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValueOrDefault(data, AZURE_DISTRIBUTION_FIELD_KEYS.project)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_DISTRIBUTION_FIELD_KEYS.project)}
                errorValue={errors[AZURE_DISTRIBUTION_FIELD_KEYS.project]}
            />
            <TextInput
                id={AZURE_DISTRIBUTION_FIELD_KEYS.workItemType}
                label="Work Item Type"
                description="The work item type in Azure Boards."
                name={AZURE_DISTRIBUTION_FIELD_KEYS.workItemType}
                required
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValueOrDefault(data, AZURE_DISTRIBUTION_FIELD_KEYS.workItemType)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_DISTRIBUTION_FIELD_KEYS.workItemType)}
                errorValue={errors[AZURE_DISTRIBUTION_FIELD_KEYS.workItemType]}
            />
            <TextInput
                id={AZURE_DISTRIBUTION_FIELD_KEYS.workItemCompleted}
                label="Work Item Completed State"
                description="The state a work item should result in if Alert receives a DELETE operation for it."
                name={AZURE_DISTRIBUTION_FIELD_KEYS.workItemCompleted}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValueOrDefault(data, AZURE_DISTRIBUTION_FIELD_KEYS.workItemCompleted)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_DISTRIBUTION_FIELD_KEYS.workItemCompleted)}
                errorValue={errors[AZURE_DISTRIBUTION_FIELD_KEYS.workItemCompleted]}
            />
            <TextInput
                id={AZURE_DISTRIBUTION_FIELD_KEYS.workItemReopen}
                label="Work Item Reopen State"
                description="The state a work item should result in if Alert receives an ADD operation and the work item is in a completed state."
                name={AZURE_DISTRIBUTION_FIELD_KEYS.workItemReopen}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValueOrDefault(data, AZURE_DISTRIBUTION_FIELD_KEYS.workItemReopen)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_DISTRIBUTION_FIELD_KEYS.workItemReopen)}
                errorValue={errors[AZURE_DISTRIBUTION_FIELD_KEYS.workItemReopen]}
            />
        </>
    );
};

AzureDistributionConfiguration.propTypes = {
    data: PropTypes.object.isRequired,
    setData: PropTypes.func.isRequired,
    errors: PropTypes.object.isRequired,
    readonly: PropTypes.bool.isRequired
};

export default AzureDistributionConfiguration;
