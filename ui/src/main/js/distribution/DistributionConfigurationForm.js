import React, { useEffect, useState } from 'react';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'global/CommonGlobalConfiguration';
import CheckboxInput from 'field/input/CheckboxInput';
import SelectInput from 'field/input/DynamicSelectInput';
import {
    DISTRIBUTION_COMMON_FIELD_KEYS,
    DISTRIBUTION_FREQUENCY_OPTIONS,
    DISTRIBUTION_INFO,
    DISTRIBUTION_NOTIFICATION_TYPE_OPTIONS,
    DISTRIBUTION_POLICY_SELECT_COLUMNS,
    DISTRIBUTION_PROJECT_SELECT_COLUMNS,
    DISTRIBUTION_URLS
} from 'distribution/DistributionModel';
import EndpointSelectField from 'field/EndpointSelectField';
import TextInput from 'field/input/TextInput';
import CollapsiblePane from 'component/common/CollapsiblePane';
import TableSelectInput from 'field/input/TableSelectInput';
import { useHistory, useLocation, useParams } from 'react-router-dom';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import { CONTEXT_TYPE } from 'util/descriptorUtilities';
import CommonDistributionConfigurationForm from 'distribution/CommonDistributionConfigurationForm';
import * as DistributionRequestUtility from 'distribution/DistributionRequestUtility';

const DistributionConfigurationForm = ({
    csrfToken, readonly, descriptors, lastUpdated
}) => {
    const { id } = useParams();
    const history = useHistory();
    const location = useLocation();
    // TODO create the correct empty model
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.DISTRIBUTION, DISTRIBUTION_INFO));
    const [errors, setErrors] = useState({});
    const [title, setTitle] = useState('New Distribution Configuration');
    const [channelModel, setChannelModel] = useState({});
    const [providerModel, setProviderModel] = useState({});

    const retrieveData = async () => {
        const data = await DistributionRequestUtility.getDataById(id, csrfToken);
        if (data) {
            setFormData(data);
        }
    };

    useEffect(() => {
        const channelFieldModel = (formData && formData.fieldModels) ? formData.fieldModels.find((model) => FieldModelUtilities.hasKey(model, DISTRIBUTION_COMMON_FIELD_KEYS.channelName)) : {};
        const providerName = FieldModelUtilities.getFieldModelSingleValue(channelFieldModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName);
        const providerFieldModel = (formData && formData.fieldModels) ? formData.fieldModels.find((model) => providerName === model.descriptorName) : {};
        setChannelModel(channelFieldModel);
        setProviderModel(providerFieldModel);
    }, [formData]);

    if (location.pathname.includes('/copy') && FieldModelUtilities.getFieldModelId(formData)) {
        delete formData.id;
        setFormData(formData);
    }

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
            <CommonDistributionConfigurationForm
                setErrors={(error) => setErrors(error)}
                formData={formData}
                setFormData={(data) => setFormData(data)}
                csrfToken={csrfToken}
                displayDelete={false}
                afterSuccessfulSave={() => history.push(DISTRIBUTION_URLS.distributionTableUrl)}
                retrieveData={retrieveData}
            >
                <CheckboxInput
                    key={DISTRIBUTION_COMMON_FIELD_KEYS.enabled}
                    name={DISTRIBUTION_COMMON_FIELD_KEYS.enabled}
                    label="Enabled"
                    description="If selected, this job will be used for processing provider notifications, otherwise, this job will not be used."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(channelModel, setProviderModel)}
                    isChecked={FieldModelUtilities.getFieldModelBooleanValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.enabled)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.enabled)}
                    errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.enabled]}
                />
                <EndpointSelectField
                    csrfToken={csrfToken}
                    endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                    fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.channelName}
                    label="Channel Type"
                    description="Select the channel. Notifications generated through Alert will be sent through this channel."
                    readOnly={readonly}
                    required
                    requiredRelatedFields={[]}
                    onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.channelName)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.channelName)}
                    errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.channelName]}
                />
                <TextInput
                    key={DISTRIBUTION_COMMON_FIELD_KEYS.name}
                    name={DISTRIBUTION_COMMON_FIELD_KEYS.name}
                    label="Name"
                    description="The name of the distribution job. Must be unique"
                    readOnly={readonly}
                    required
                    onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.name)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.name)}
                    errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.name]}
                />
                <SelectInput
                    key={DISTRIBUTION_COMMON_FIELD_KEYS.frequency}
                    label="Frequency"
                    description="Select how frequently this job should check for notifications to send."
                    options={DISTRIBUTION_FREQUENCY_OPTIONS}
                    readOnly={readonly}
                    required
                    onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.frequency)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.frequency)}
                    errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.frequency]}
                />
                <EndpointSelectField
                    csrfToken={csrfToken}
                    endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                    fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.providerName}
                    label="Provider Type"
                    description="Select the provider. Only notifications for that provider will be processed in this distribution job."
                    readOnly={readonly}
                    required
                    onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.providerName)}
                    errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.providerName]}
                />
                <EndpointSelectField
                    csrfToken={csrfToken}
                    endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                    fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId}
                    label="Provider Configuration"
                    description="The provider configuration to use with this distribution job."
                    clearable={false}
                    readOnly={readonly}
                    required
                    onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId)}
                    errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId]}
                />
                <SelectInput
                    key={DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes}
                    label="Notification Types"
                    description="Select one or more of the notification types. Only these notification types will be included for this distribution job."
                    options={DISTRIBUTION_NOTIFICATION_TYPE_OPTIONS}
                    readOnly={readonly}
                    required
                    onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes)}
                    errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes]}
                />
                <EndpointSelectField
                    csrfToken={csrfToken}
                    endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                    fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.processingType}
                    label="Processing"
                    description="Select the way messages will be processed: <TODO create the dynamic description>"
                    readOnly={readonly}
                    required
                    requiredRelatedFields={[DISTRIBUTION_COMMON_FIELD_KEYS.channelName]}
                    onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.processingType)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.processingType)}
                    errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.processingType]}
                />
                <CheckboxInput
                    name={DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject}
                    label="Filter By Project"
                    description="If selected, only notifications from the selected Projects table will be processed. Otherwise notifications from all Projects are processed."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
                    isChecked={FieldModelUtilities.getFieldModelBooleanValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject)}
                    errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject]}
                />

                {FieldModelUtilities.getFieldModelBooleanValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject) && (
                    <div>
                        <TextInput
                            key={DISTRIBUTION_COMMON_FIELD_KEYS.projectNamePattern}
                            name={DISTRIBUTION_COMMON_FIELD_KEYS.projectNamePattern}
                            label="Project Name Pattern"
                            description="The regular expression to use to determine what Projects to include. These are in addition to the Projects selected in the table."
                            readOnly={readonly}
                            required
                            onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
                            value={FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.projectNamePattern)}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.projectNamePattern)}
                            errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.projectNamePattern]}
                        />
                        <TableSelectInput
                            csrfToken={csrfToken}
                            endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                            fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.selectedProjects}
                            columns={DISTRIBUTION_PROJECT_SELECT_COLUMNS}
                            currentConfig={null}
                            label="Projects"
                            description="Select a project or projects that will be used to retrieve notifications from your provider."
                            requiredRelatedFields={[
                                DISTRIBUTION_COMMON_FIELD_KEYS.providerName,
                                DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId
                            ]}
                            readOnly={readonly}
                            paged
                            searchable
                            useRowAsValue
                            onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
                            value={FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.selectedProjects)}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.selectedProjects)}
                            errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.selectedProjects]}
                        />
                    </div>
                )}
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
                        requiredRelatedFields={[]}
                        readOnly={readonly}
                        paged
                        onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.policyFilter)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.policyFilter)}
                        errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.policyFilter]}
                    />
                    <EndpointSelectField
                        csrfToken={csrfToken}
                        endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                        fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.vulnerabilitySeverityFilter}
                        label="Vulnerability Notification Type Filter"
                        description="List of Vulnerability severities you can choose from to further filter which notifications you want sent via this job (You must have a vulnerability notification selected for this filter to apply)."
                        readOnly={readonly}
                        requiredRelatedFields={[DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes]}
                        onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.vulnerabilitySeverityFilter)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.vulnerabilitySeverityFilter)}
                        errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.vulnerabilitySeverityFilter]}
                    />
                </CollapsiblePane>
            </CommonDistributionConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

DistributionConfigurationForm.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    descriptors: PropTypes.array.isRequired,
    lastUpdated: PropTypes.string,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool
};

DistributionConfigurationForm.defaultProps = {
    lastUpdated: null,
    readonly: false
};

export default DistributionConfigurationForm;
