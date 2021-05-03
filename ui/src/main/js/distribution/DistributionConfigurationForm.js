import React, { useEffect, useState } from 'react';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'global/CommonGlobalConfiguration';
import CheckboxInput from 'field/input/CheckboxInput';
import SelectInput from 'field/input/DynamicSelectInput';
import {
    DISTRIBUTION_COMMON_FIELD_KEYS,
    DISTRIBUTION_FREQUENCY_OPTIONS,
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
import { AZURE_INFO } from 'global/channels/azure/AzureModel';
import { BLACKDUCK_INFO } from 'global/providers/blackduck/BlackDuckModel';
import { EMAIL_INFO } from 'global/channels/email/EmailModels';
import { SLACK_INFO } from 'global/channels/slack/SlackModels';
import EmailDistributionConfiguration from 'distribution/channels/email/EmailDistributionConfiguration';
import SlackDistributionConfiguration from 'distribution/channels/slack/SlackDistributionConfiguration';

const DistributionConfigurationForm = ({
    csrfToken, readonly, descriptors, lastUpdated
}) => {
    const { id } = useParams();
    const history = useHistory();
    const location = useLocation();
    const [formData, setFormData] = useState({});
    const [errors, setErrors] = useState({});
    const channelFieldKeys = {};
    channelFieldKeys[DISTRIBUTION_COMMON_FIELD_KEYS.enabled] = {};
    channelFieldKeys[DISTRIBUTION_COMMON_FIELD_KEYS.name] = {};
    channelFieldKeys[DISTRIBUTION_COMMON_FIELD_KEYS.channelName] = {};
    channelFieldKeys[DISTRIBUTION_COMMON_FIELD_KEYS.processingType] = {};
    channelFieldKeys[DISTRIBUTION_COMMON_FIELD_KEYS.frequency] = {};
    channelFieldKeys[DISTRIBUTION_COMMON_FIELD_KEYS.providerName] = {};
    const providerFieldKeys = {};
    providerFieldKeys[DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId] = {};
    providerFieldKeys[DISTRIBUTION_COMMON_FIELD_KEYS.providerName] = {};
    providerFieldKeys[DISTRIBUTION_COMMON_FIELD_KEYS.processingType] = {};
    providerFieldKeys[DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes] = {};
    providerFieldKeys[DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject] = {};
    providerFieldKeys[DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects] = {};
    const [channelModel, setChannelModel] = useState(FieldModelUtilities.createEmptyFieldModel(channelFieldKeys, CONTEXT_TYPE.DISTRIBUTION, AZURE_INFO.key));
    const [providerModel, setProviderModel] = useState(FieldModelUtilities.createEmptyFieldModel(providerFieldKeys, CONTEXT_TYPE.DISTRIBUTION, BLACKDUCK_INFO.key));
    const [channelFields, setChannelFields] = useState(null);
    const [providerHasChannelName, setProviderHasChannelName] = useState(false);
    const [hasProvider, setHasProvider] = useState(false);
    const [hasNotificationTypes, setHasNotificationTypes] = useState(false);
    const [filterByProject, setFilterByProject] = useState(false);
    const retrieveData = async () => {
        const data = await DistributionRequestUtility.getDataById(id, csrfToken);
        return data;
    };

    const updateJobData = () => {
        const providerConfigToSave = JSON.parse(JSON.stringify(providerModel));
        let configuredProviderProjects = [];

        const fieldConfiguredProjects = providerModel.keyToValues[DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects];
        if (fieldConfiguredProjects && fieldConfiguredProjects.values && fieldConfiguredProjects.values.length > 0) {
            configuredProviderProjects = fieldConfiguredProjects.values.map((selectedValue) => {
                let valueObject = selectedValue;
                if (typeof selectedValue === 'string') {
                    valueObject = JSON.parse(selectedValue);
                }

                return {
                    name: valueObject.name,
                    href: valueObject.href
                };
            });

            // Related fields need this to have a value in order to validate successfully
            providerConfigToSave.keyToValues[DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects].values = ['undefined'];
        }

        return {
            jobId: formData.jobId,
            fieldModels: [
                channelModel,
                providerConfigToSave
            ],
            configuredProviderProjects
        };
    };

    const createAdditionalEmailRequestBody = () => {
        const providerName = FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName);
        const providerConfigId = FieldModelUtilities.getFieldModelSingleValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId);
        const updatedModel = FieldModelUtilities.updateFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName, providerName);
        return FieldModelUtilities.updateFieldModelSingleValue(updatedModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId, providerConfigId);
    };

    const createProjectRequestBody = () => {
        const providerName = FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName);
        const providerConfigId = FieldModelUtilities.getFieldModelSingleValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId);
        const updatedModel = FieldModelUtilities.updateFieldModelSingleValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName, providerName);
        return FieldModelUtilities.updateFieldModelSingleValue(updatedModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId, providerConfigId);
    };
    const createPolicyFilterRequestBody = () => {
        const providerName = FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName);
        const providerConfigId = FieldModelUtilities.getFieldModelSingleValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId);
        const notificationTypes = FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes);
        const providerNameModel = FieldModelUtilities.updateFieldModelSingleValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName, providerName);
        const providerInfoModel = FieldModelUtilities.updateFieldModelSingleValue(providerNameModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId, providerConfigId);
        return FieldModelUtilities.updateFieldModelValues(providerInfoModel, DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes, notificationTypes);
    };

    useEffect(() => {
        const channelFieldModel = (formData && formData.fieldModels) ? formData.fieldModels.find((model) => FieldModelUtilities.hasKey(model, DISTRIBUTION_COMMON_FIELD_KEYS.channelName)) : {};
        const channelNameDefined = FieldModelUtilities.hasValue(channelFieldModel, DISTRIBUTION_COMMON_FIELD_KEYS.channelName);
        const providerName = FieldModelUtilities.getFieldModelSingleValue(channelFieldModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName);
        let providerFieldModel = (formData && formData.fieldModels) ? formData.fieldModels.find((model) => providerName === model.descriptorName) : {};
        // add the required related fields to the provider field model for the endpoint select fields.
        providerFieldModel = FieldModelUtilities.updateFieldModelSingleValue(providerFieldModel, DISTRIBUTION_COMMON_FIELD_KEYS.channelName, FieldModelUtilities.getFieldModelSingleValue(channelFieldModel, DISTRIBUTION_COMMON_FIELD_KEYS.channelName));
        providerFieldModel = FieldModelUtilities.updateFieldModelSingleValue(providerFieldModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName, FieldModelUtilities.getFieldModelSingleValue(channelFieldModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName));
        providerFieldModel = FieldModelUtilities.updateFieldModelSingleValue(providerFieldModel, DISTRIBUTION_COMMON_FIELD_KEYS.processingType, FieldModelUtilities.getFieldModelSingleValue(channelFieldModel, DISTRIBUTION_COMMON_FIELD_KEYS.processingType));
        setChannelModel(channelFieldModel);
        setProviderModel(providerFieldModel);
        setProviderHasChannelName(channelNameDefined);
    }, [formData]);

    useEffect(() => {
        const channelKey = FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.channelName);
        if (channelKey === EMAIL_INFO.key) {
            setChannelFields(<EmailDistributionConfiguration csrfToken={csrfToken} createAdditionalEmailRequestBody={createAdditionalEmailRequestBody} data={channelModel} setData={setChannelModel} errors={errors} readonly={readonly} />);
        } else if (channelKey === SLACK_INFO.key) {
            setChannelFields(<SlackDistributionConfiguration data={channelModel} setData={setChannelModel} errors={errors} readonly={readonly} />);
        } else {
            setChannelFields(null);
        }
    }, [channelModel]);

    useEffect(() => {
        setHasProvider(FieldModelUtilities.hasValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName));
        setFilterByProject(FieldModelUtilities.getFieldModelBooleanValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject));
        setHasNotificationTypes(FieldModelUtilities.hasValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes));
    }, [providerModel]);

    if (location.pathname.includes('/copy') && FieldModelUtilities.getFieldModelId(formData)) {
        delete formData.id;
        setFormData(formData);
    }

    return (
        <CommonGlobalConfiguration
            label="Distribution Configuration"
            description="Configure the Distribution Job for Alert to send updates."
            lastUpdated={lastUpdated}
        >
            <CommonDistributionConfigurationForm
                setErrors={(error) => setErrors(error)}
                formData={formData}
                setFormData={setFormData}
                csrfToken={csrfToken}
                displayDelete={false}
                afterSuccessfulSave={() => history.push(DISTRIBUTION_URLS.distributionTableUrl)}
                retrieveData={retrieveData}
                createDataToSend={updateJobData}
            >
                <CheckboxInput
                    name={DISTRIBUTION_COMMON_FIELD_KEYS.enabled}
                    label="Enabled"
                    description="If selected, this job will be used for processing provider notifications, otherwise, this job will not be used."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
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
                    clearable={false}
                    readOnly={readonly}
                    required
                    currentConfig={channelModel}
                    onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
                    value={FieldModelUtilities.getFieldModelValues(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.channelName)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.channelName)}
                    errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.channelName]}
                />
                <TextInput
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
                    id={DISTRIBUTION_COMMON_FIELD_KEYS.frequency}
                    label="Frequency"
                    description="Select how frequently this job should check for notifications to send."
                    options={DISTRIBUTION_FREQUENCY_OPTIONS}
                    readOnly={readonly}
                    required
                    onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
                    value={FieldModelUtilities.getFieldModelValues(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.frequency)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.frequency)}
                    errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.frequency]}
                />
                <EndpointSelectField
                    csrfToken={csrfToken}
                    endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                    fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.providerName}
                    label="Provider Type"
                    description="Select the provider. Only notifications for that provider will be processed in this distribution job."
                    clearable={false}
                    readOnly={readonly}
                    required
                    currentConfig={channelModel}
                    onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
                    value={FieldModelUtilities.getFieldModelValues(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName)}
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
                    currentConfig={providerModel}
                    onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                    value={FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId)}
                    errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId]}
                />
                {channelFields}
                {hasProvider && providerHasChannelName && (
                    <div>
                        <SelectInput
                            id={DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes}
                            label="Notification Types"
                            description="Select one or more of the notification types. Only these notification types will be included for this distribution job."
                            options={DISTRIBUTION_NOTIFICATION_TYPE_OPTIONS}
                            multiSelect
                            readOnly={readonly}
                            removeSelected
                            required
                            onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                            value={FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes)}
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
                            currentConfig={providerModel}
                            onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                            value={FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.processingType)}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.processingType)}
                            errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.processingType]}
                        />
                        <CheckboxInput
                            name={DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject}
                            label="Filter By Project"
                            description="If selected, only notifications from the selected Projects table will be processed. Otherwise notifications from all Projects are processed."
                            readOnly={readonly}
                            onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                            isChecked={filterByProject}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject)}
                            errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject]}
                        />
                    </div>
                )}
                {filterByProject && (
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
                            fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects}
                            columns={DISTRIBUTION_PROJECT_SELECT_COLUMNS}
                            label="Projects"
                            description="Select a project or projects that will be used to retrieve notifications from your provider."
                            readOnly={readonly}
                            paged
                            searchable
                            useRowAsValue
                            createRequestBody={createProjectRequestBody}
                            onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                            value={FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects)}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects)}
                            errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects]}
                        />
                    </div>
                )}
                {hasNotificationTypes && (
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
                            label="Policy Notification Type Filter"
                            description="List of Policies you can choose from to further filter which notifications you want sent via this job (You must have a policy notification selected for this filter to apply)."
                            readOnly={readonly}
                            paged
                            searchable
                            createRequestBody={createPolicyFilterRequestBody}
                            onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                            value={FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.policyFilter)}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.policyFilter)}
                            errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.policyFilter]}
                        />
                        <EndpointSelectField
                            csrfToken={csrfToken}
                            endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                            fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.vulnerabilitySeverityFilter}
                            label="Vulnerability Notification Type Filter"
                            description="List of Vulnerability severities you can choose from to further filter which notifications you want sent via this job (You must have a vulnerability notification selected for this filter to apply)."
                            multiSelect
                            readOnly={readonly}
                            requiredRelatedFields={[DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes]}
                            currentConfig={providerModel}
                            onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                            value={FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.vulnerabilitySeverityFilter)}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.vulnerabilitySeverityFilter)}
                            errorValue={errors[DISTRIBUTION_COMMON_FIELD_KEYS.vulnerabilitySeverityFilter]}
                        />
                    </CollapsiblePane>
                )}
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
