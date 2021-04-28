import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'global/CommonGlobalConfiguration';
import CheckboxInput from 'field/input/CheckboxInput';
import SelectInput from 'field/input/DynamicSelectInput';
import {
    DISTRIBUTION_COMMON_FIELD_KEYS,
    DISTRIBUTION_FREQUENCY_OPTIONS,
    DISTRIBUTION_NOTIFICATION_TYPE_OPTIONS,
    DISTRIBUTION_POLICY_SELECT_COLUMNS,
    DISTRIBUTION_POLICY_SELECT_RELATED_FIELDS,
    DISTRIBUTION_PROJECT_SELECT_COLUMNS,
    DISTRIBUTION_PROJECT_SELECT_RELATED_FIELDS,
    DISTRIBUTION_URLS
} from 'distribution/DistributionModel';
import EndpointSelectField from 'field/EndpointSelectField';
import TextInput from 'field/input/TextInput';
import CollapsiblePane from 'component/common/CollapsiblePane';
import TableSelectInput from 'field/input/TableSelectInput';

const DistributionConfigurationForm = ({
    csrfToken, readonly, descriptors, lastUpdated, title
}) => {
    const [inProgress, setInProgress] = useState(false);
    const [loading, setLoading] = useState(false);

    const channelFields = (
        <div>
            Channel specific fields go here...
        </div>
    );
    return (
        <CommonGlobalConfiguration
            label={title}
            description="Configure the Distribution Job for Alert to send updates."
            lastUpdated={lastUpdated}
        >
            <CheckboxInput
                key={DISTRIBUTION_COMMON_FIELD_KEYS.enabled}
                name={DISTRIBUTION_COMMON_FIELD_KEYS.enabled}
                label="Enabled"
                description="If selected, this job will be used for processing provider notifications, otherwise, this job will not be used."
                readOnly={readonly}
            />
            <EndpointSelectField
                csrfToken={csrfToken}
                endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.channelName}
                label="Channel Type"
                description="Select the channel. Notifications generated through Alert will be sent through this channel."
                onChange={() => {
                }}
                readOnly={readonly}
                required
            />
            <TextInput
                key={DISTRIBUTION_COMMON_FIELD_KEYS.name}
                name={DISTRIBUTION_COMMON_FIELD_KEYS.name}
                label="Name"
                description="The name of the distribution job. Must be unique"
                readOnly={readonly}
                required
            />
            <SelectInput
                key={DISTRIBUTION_COMMON_FIELD_KEYS.frequency}
                label="Frequency"
                description="Select how frequently this job should check for notifications to send."
                onChange={() => {
                }}
                options={DISTRIBUTION_FREQUENCY_OPTIONS}
                readOnly={readonly}
                required
            />
            <EndpointSelectField
                csrfToken={csrfToken}
                endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.providerName}
                label="Provider Type"
                description="Select the provider. Only notifications for that provider will be processed in this distribution job."
                onChange={() => {
                }}
                readOnly={readonly}
                required
            />
            <EndpointSelectField
                csrfToken={csrfToken}
                endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId}
                label="Provider Configuration"
                description="The provider configuration to use with this distribution job."
                onChange={() => {
                }}
                clearable={false}
                readOnly={readonly}
                required
            />
            <SelectInput
                key={DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes}
                label="Notification Types"
                description="Select one or more of the notification types. Only these notification types will be included for this distribution job."
                onChange={() => {
                }}
                options={DISTRIBUTION_NOTIFICATION_TYPE_OPTIONS}
                readOnly={readonly}
                required
            />
            <EndpointSelectField
                csrfToken={csrfToken}
                endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.processingType}
                label="Processing"
                description="Select the way messages will be processed: <TODO create the dynamic description>"
                onChange={() => {
                }}
                readOnly={readonly}
                required
            />
            <CheckboxInput
                name={DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject}
                label="Filter By Project"
                description="If selected, only notifications from the selected Projects table will be processed. Otherwise notifications from all Projects are processed."
                isChecked={false}
                readOnly={readonly}
            />

            <TextInput
                key={DISTRIBUTION_COMMON_FIELD_KEYS.projectNamePattern}
                name={DISTRIBUTION_COMMON_FIELD_KEYS.projectNamePattern}
                label="Project Name Pattern"
                description="The regular expression to use to determine what Projects to include. These are in addition to the Projects selected in the table."
                readOnly={readonly}
                required
            />
            <TableSelectInput
                csrfToken={csrfToken}
                endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.selectedProjects}
                columns={DISTRIBUTION_PROJECT_SELECT_COLUMNS}
                currentConfig={null}
                label="Projects"
                description="Select a project or projects that will be used to retrieve notifications from your provider."
                requiredRelatedFields={DISTRIBUTION_PROJECT_SELECT_RELATED_FIELDS}
                readOnly={readonly}
                paged
                searchable
                useRowAsValue
            />
            {channelFields}
            <CollapsiblePane
                id="distribution-notification-filtering"
                title="Black Duck Notification Filtering"
                expanded={false}
            >
                <TableSelectInput
                    csrfToken={csrfToken}
                    endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                    fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.policyFilter}
                    columns={DISTRIBUTION_POLICY_SELECT_COLUMNS}
                    currentConfig={null}
                    label="Policy Notification Type Filter"
                    description="List of Policies you can choose from to further filter which notifications you want sent via this job (You must have a policy notification selected for this filter to apply)."
                    requiredRelatedFields={DISTRIBUTION_POLICY_SELECT_RELATED_FIELDS}
                    readOnly={readonly}
                    paged
                />
                <EndpointSelectField
                    csrfToken={csrfToken}
                    endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                    fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.vulnerabilitySeverityFilter}
                    label="Vulnerability Notification Type Filter"
                    description="List of Vulnerability severities you can choose from to further filter which notifications you want sent via this job (You must have a vulnerability notification selected for this filter to apply)."
                    onChange={() => {
                    }}
                    readOnly={readonly}
                />
            </CollapsiblePane>

        </CommonGlobalConfiguration>
    );
};

DistributionConfigurationForm.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    descriptors: PropTypes.array.isRequired,
    lastUpdated: PropTypes.string,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    title: PropTypes.string.isRequired
};

DistributionConfigurationForm.defaultProps = {
    lastUpdated: null,
    readonly: false
};

export default DistributionConfigurationForm;
