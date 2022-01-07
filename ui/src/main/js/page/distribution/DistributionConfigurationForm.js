import React, { useEffect, useState } from 'react';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'common/global/CommonGlobalConfiguration';
import CheckboxInput from 'common/input/CheckboxInput';
import SelectInput from 'common/input/DynamicSelectInput';
import {
    DISTRIBUTION_CHANNEL_OPTIONS,
    DISTRIBUTION_COMMON_FIELD_KEYS,
    DISTRIBUTION_FREQUENCY_OPTIONS,
    DISTRIBUTION_NOTIFICATION_TYPE_OPTIONS,
    DISTRIBUTION_POLICY_SELECT_COLUMNS,
    DISTRIBUTION_PROCESSING_DESCRIPTIONS,
    DISTRIBUTION_PROCESSING_TYPES,
    DISTRIBUTION_PROJECT_SELECT_COLUMNS,
    DISTRIBUTION_TEST_FIELD_KEYS,
    DISTRIBUTION_URLS,
    DISTRIBUTION_VULNERABILITY_SEVERITY_OPTIONS
} from 'page/distribution/DistributionModel';
import EndpointSelectField from 'common/input/EndpointSelectField';
import TextInput from 'common/input/TextInput';
import CollapsiblePane from 'common/CollapsiblePane';
import TableSelectInput from 'common/input/TableSelectInput';
import { useHistory, useLocation, useParams } from 'react-router-dom';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { CONTEXT_TYPE, isOneOperationAssigned, isOperationAssigned, OPERATIONS } from 'common/util/descriptorUtilities';
import CommonDistributionConfigurationForm from 'page/distribution/CommonDistributionConfigurationForm';
import * as DistributionRequestUtility from 'page/distribution/DistributionRequestUtility';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import { AZURE_INFO } from 'page/channel/azure/AzureModel';
import { BLACKDUCK_INFO } from 'page/provider/blackduck/BlackDuckModel';
import { EMAIL_INFO } from 'page/channel/email/EmailModels';
import { JIRA_CLOUD_INFO } from 'page/channel/jira/cloud/JiraCloudModel';
import { JIRA_SERVER_INFO } from 'page/channel/jira/server/JiraServerModel';
import { MSTEAMS_INFO } from 'page/channel/msteams/MSTeamsModel';
import { SLACK_INFO } from 'page/channel/slack/SlackModels';
import AzureDistributionConfiguration from 'page/channel/azure/AzureDistributionConfiguration';
import EmailDistributionConfiguration from 'page/channel/email/EmailDistributionConfiguration';
import JiraCloudDistributionConfiguration from 'page/channel/jira/cloud/JiraCloudDistributionConfiguration';
import JiraServerDistributionConfiguration from 'page/channel/jira/server/JiraServerDistributionConfiguration';
import MsTeamsDistributionConfiguration from 'page/channel/msteams/MsTeamsDistributionConfiguration';
import SlackDistributionConfiguration from 'page/channel/slack/SlackDistributionConfiguration';

const DistributionConfigurationForm = ({
                                           csrfToken, errorHandler, descriptors, lastUpdated
                                       }) => {
    const { id } = useParams();
    const history = useHistory();
    const location = useLocation();
    const [formData, setFormData] = useState({});
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());
    const [channelModel, setChannelModel] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.DISTRIBUTION, AZURE_INFO.key));
    const [specificChannelModel, setSpecificChannelModel] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.DISTRIBUTION, AZURE_INFO.key));
    const [providerModel, setProviderModel] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.DISTRIBUTION, BLACKDUCK_INFO.key));
    const [selectedChannel, setSelectedChannel] = useState(AZURE_INFO.key);
    const [testFieldModel, setTestFieldModel] = useState({});
    const [readonly, setReadonly] = useState(false);
    const [processingTypes, setProcessingTypes] = useState(DISTRIBUTION_PROCESSING_TYPES);

    const retrieveData = async () => DistributionRequestUtility.getDataById(id, csrfToken, errorHandler, setErrors);

    const createDistributionData = (channelModelData, providerModelData) => {
        const providerConfig = JSON.parse(JSON.stringify(providerModelData));
        let configuredProviderProjects = [];

        const fieldConfiguredProjects = FieldModelUtilities.getFieldModelValues(providerConfig, DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects);
        if (fieldConfiguredProjects && fieldConfiguredProjects.length > 0) {
            configuredProviderProjects = fieldConfiguredProjects.map((selectedValue) => ({
                name: selectedValue.name,
                href: selectedValue.href,
                missing: false
            }));
        }

        const providerConfigToSave = FieldModelUtilities.updateFieldModelValues(providerConfig, DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects, []);
        const allChannelData = FieldModelUtilities.combineFieldModels(channelModelData, specificChannelModel);
        return {
            jobId: formData.jobId,
            fieldModels: [
                allChannelData,
                providerConfigToSave
            ],
            configuredProviderProjects
        };
    };

    const updateJobData = () => createDistributionData(channelModel, providerModel);

    const createTestData = () => {
        const channelFieldModel = FieldModelUtilities.combineFieldModels(channelModel, testFieldModel);
        return createDistributionData(channelFieldModel, providerModel);
    };

    const createAdditionalEmailRequestBody = () => {
        const providerConfigId = FieldModelUtilities.getFieldModelSingleValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId);
        return FieldModelUtilities.updateFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId, providerConfigId);
    };

    const createProviderRequestBody = () => {
        const providerName = FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName);
        const copiedProviderModel = JSON.parse(JSON.stringify(providerModel));
        delete copiedProviderModel.keyToValues[DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects];
        return FieldModelUtilities.updateFieldModelSingleValue(copiedProviderModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName, providerName);
    };

    useEffect(() => {
        const channelFieldModel = (formData && formData.fieldModels)
            ? formData.fieldModels.find((model) => FieldModelUtilities.hasKey(model, DISTRIBUTION_COMMON_FIELD_KEYS.channelName))
            : FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.DISTRIBUTION, AZURE_INFO.key);
        const channelKey = FieldModelUtilities.getFieldModelSingleValue(channelFieldModel, DISTRIBUTION_COMMON_FIELD_KEYS.channelName);
        if (channelKey) {
            setSelectedChannel(channelKey);
        }

        let newSpecificFieldModel = FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.DISTRIBUTION, channelKey);
        Object.keys(channelFieldModel.keyToValues).forEach((key) => {
            if (!Object.values(DISTRIBUTION_COMMON_FIELD_KEYS).includes(key)) {
                const channelFieldValues = FieldModelUtilities.getFieldModelValues(channelFieldModel, key);
                newSpecificFieldModel = FieldModelUtilities.updateFieldModelValues(newSpecificFieldModel, key, channelFieldValues);
                delete channelFieldModel.keyToValues[key];
            }
        });
        setSpecificChannelModel(newSpecificFieldModel);
        setChannelModel(channelFieldModel);

        const providerName = FieldModelUtilities.getFieldModelSingleValue(channelFieldModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName);
        let providerFieldModel = (formData && formData.fieldModels)
            ? formData.fieldModels.find((model) => providerName === model.descriptorName)
            : FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.DISTRIBUTION, BLACKDUCK_INFO.key);

        const { configuredProviderProjects } = formData;
        if (configuredProviderProjects) {
            providerFieldModel = FieldModelUtilities.updateFieldModelValues(providerFieldModel, DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects, configuredProviderProjects);
        }
        setProviderModel(providerFieldModel);

        if (descriptors[channelKey]) {
            setReadonly(descriptors[channelKey].readOnly);
        }
    }, [formData]);

    const onChannelSelectChange = (event) => {
        const { target } = event;
        const { name, value } = target;
        DistributionRequestUtility.checkDescriptorForGlobalConfig({
            csrfToken, descriptorName: value, errorHandler, fieldName: name, errors, setErrors
        });
        const firstValue = value[0];
        channelModel.descriptorName = firstValue;
        FieldModelUtilities.handleChange(channelModel, setChannelModel)(event);
        setSelectedChannel(firstValue);
        setSpecificChannelModel(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.DISTRIBUTION, firstValue));
        if (descriptors[firstValue]) {
            setReadonly(descriptors[firstValue].readOnly);
        }
    };

    const getProcessingDescription = (processingType) => DISTRIBUTION_PROCESSING_DESCRIPTIONS[processingType] || '';

    useEffect(() => {
        switch (selectedChannel.toString()) {
            case AZURE_INFO.key:
            case JIRA_SERVER_INFO.key:
            case JIRA_CLOUD_INFO.key: {
                const filtered = DISTRIBUTION_PROCESSING_TYPES.filter((type) => type.value !== 'SUMMARY');
                setProcessingTypes(filtered);
                break;
            }
            default:
                setProcessingTypes(DISTRIBUTION_PROCESSING_TYPES);
        }
    }, [selectedChannel]);

    const renderChannelFields = () => {
        switch (selectedChannel.toString()) {
            case AZURE_INFO.key:
                return (
                    <AzureDistributionConfiguration
                        data={specificChannelModel}
                        setData={setSpecificChannelModel}
                        errors={errors}
                        readonly={readonly}
                    />
                );
            case EMAIL_INFO.key:
                return (
                    <EmailDistributionConfiguration
                        csrfToken={csrfToken}
                        createAdditionalEmailRequestBody={createAdditionalEmailRequestBody}
                        data={specificChannelModel}
                        setData={setSpecificChannelModel}
                        errors={errors}
                        readonly={readonly}
                    />
                );
            case JIRA_CLOUD_INFO.key:
                return (
                    <JiraCloudDistributionConfiguration
                        csrfToken={csrfToken}
                        data={specificChannelModel}
                        setData={setSpecificChannelModel}
                        errors={errors}
                        readonly={readonly}
                    />
                );
            case JIRA_SERVER_INFO.key:
                return (
                    <JiraServerDistributionConfiguration
                        csrfToken={csrfToken}
                        data={specificChannelModel}
                        setData={setSpecificChannelModel}
                        errors={errors}
                        readonly={readonly}
                    />
                );
            case MSTEAMS_INFO.key:
                return (
                    <MsTeamsDistributionConfiguration
                        data={specificChannelModel}
                        setData={setSpecificChannelModel}
                        errors={errors}
                        readonly={readonly}
                    />
                );
            case SLACK_INFO.key:
                return (
                    <SlackDistributionConfiguration
                        data={specificChannelModel}
                        setData={setSpecificChannelModel}
                        errors={errors}
                        readonly={readonly}
                    />
                );
            default:
                return null;
        }
    };

    if (location.pathname.includes('/copy') && formData.jobId) {
        delete formData.jobId;
    }

    if (!FieldModelUtilities.hasKey(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.enabled)) {
        const defaultValueModel = FieldModelUtilities.updateFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.enabled, true);
        setChannelModel(defaultValueModel);
    }

    if (!FieldModelUtilities.hasKey(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.channelName)) {
        onChannelSelectChange({ target: { name: DISTRIBUTION_COMMON_FIELD_KEYS.channelName, value: [AZURE_INFO.key] } });
    }

    if (!FieldModelUtilities.hasKey(testFieldModel, DISTRIBUTION_TEST_FIELD_KEYS.topic) && !FieldModelUtilities.hasKey(testFieldModel, DISTRIBUTION_TEST_FIELD_KEYS.message)) {
        const topicFieldModel = FieldModelUtilities.updateFieldModelSingleValue(testFieldModel, DISTRIBUTION_TEST_FIELD_KEYS.topic, 'Alert Test Message');
        const messageFieldModel = FieldModelUtilities.updateFieldModelSingleValue(topicFieldModel, DISTRIBUTION_TEST_FIELD_KEYS.message, 'Test Message Content');
        setTestFieldModel(messageFieldModel);
    }

    const testFields = (
        <div>
            <TextInput
                id={DISTRIBUTION_TEST_FIELD_KEYS.topic}
                label="Topic"
                name={DISTRIBUTION_TEST_FIELD_KEYS.topic}
                required
                onChange={FieldModelUtilities.handleChange(testFieldModel, setTestFieldModel)}
                value={FieldModelUtilities.getFieldModelSingleValue(testFieldModel, DISTRIBUTION_TEST_FIELD_KEYS.topic)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_TEST_FIELD_KEYS.topic)}
                errorValue={errors.fieldErrors[DISTRIBUTION_TEST_FIELD_KEYS.topic]}
            />
            <TextInput
                id={DISTRIBUTION_TEST_FIELD_KEYS.message}
                label="Message"
                name={DISTRIBUTION_TEST_FIELD_KEYS.message}
                required
                onChange={FieldModelUtilities.handleChange(testFieldModel, setTestFieldModel)}
                value={FieldModelUtilities.getFieldModelSingleValue(testFieldModel, DISTRIBUTION_TEST_FIELD_KEYS.message)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_TEST_FIELD_KEYS.message)}
                errorValue={errors.fieldErrors[DISTRIBUTION_TEST_FIELD_KEYS.message]}
            />
        </div>
    );

    const processingFieldDescription = `Select the way messages will be processed: ${getProcessingDescription(FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.processingType))}`;

    // TODO need to provide finer grain control with permissions.
    return (
        <CommonGlobalConfiguration
            label="Distribution Configuration"
            description="Configure the Distribution Job for Alert to send updates."
            lastUpdated={lastUpdated}
        >
            <CommonDistributionConfigurationForm
                setErrors={setErrors}
                formData={formData}
                setFormData={setFormData}
                testFields={testFields}
                testFormData={testFieldModel}
                setTestFormData={setTestFieldModel}
                csrfToken={csrfToken}
                displaySave={!readonly && isOneOperationAssigned(descriptors[selectedChannel], [OPERATIONS.WRITE, OPERATIONS.CREATE])}
                displayTest={!readonly && isOperationAssigned(descriptors[selectedChannel], OPERATIONS.EXECUTE)}
                displayDelete={false}
                afterSuccessfulSave={() => history.push(DISTRIBUTION_URLS.distributionTableUrl)}
                retrieveData={retrieveData}
                createDataToSend={updateJobData}
                createDataToTest={createTestData}
                errorHandler={errorHandler}
            >
                <CheckboxInput
                    id={DISTRIBUTION_COMMON_FIELD_KEYS.enabled}
                    name={DISTRIBUTION_COMMON_FIELD_KEYS.enabled}
                    label="Enabled"
                    description="If selected, this job will be used for processing provider notifications, otherwise, this job will not be used."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
                    isChecked={FieldModelUtilities.getFieldModelBooleanValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.enabled)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.enabled)}
                    errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.enabled]}
                />
                <SelectInput
                    id={DISTRIBUTION_COMMON_FIELD_KEYS.channelName}
                    name={DISTRIBUTION_COMMON_FIELD_KEYS.channelName}
                    label="Channel Type"
                    description="Select the channel. Notifications generated through Alert will be sent through this channel."
                    options={DISTRIBUTION_CHANNEL_OPTIONS}
                    clearable={false}
                    readOnly={readonly}
                    required
                    onChange={onChannelSelectChange}
                    value={FieldModelUtilities.getFieldModelValues(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.channelName)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.channelName)}
                    errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.channelName]}
                />
                <TextInput
                    id={DISTRIBUTION_COMMON_FIELD_KEYS.name}
                    name={DISTRIBUTION_COMMON_FIELD_KEYS.name}
                    label="Name"
                    description="The name of the distribution job. Must be unique"
                    readOnly={readonly}
                    required
                    onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.name)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.name)}
                    errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.name]}
                />
                <SelectInput
                    id={DISTRIBUTION_COMMON_FIELD_KEYS.frequency}
                    name={DISTRIBUTION_COMMON_FIELD_KEYS.frequency}
                    label="Frequency"
                    description="Select how frequently this job should check for notifications to send."
                    options={DISTRIBUTION_FREQUENCY_OPTIONS}
                    readOnly={readonly}
                    required
                    onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
                    value={FieldModelUtilities.getFieldModelValues(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.frequency)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.frequency)}
                    errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.frequency]}
                />
                <SelectInput
                    id={DISTRIBUTION_COMMON_FIELD_KEYS.providerName}
                    name={DISTRIBUTION_COMMON_FIELD_KEYS.providerName}
                    label="Provider Type"
                    description="Select the provider. Only notifications for that provider will be processed in this distribution job."
                    options={[{ label: BLACKDUCK_INFO.label, value: BLACKDUCK_INFO.key }]}
                    clearable={false}
                    readOnly={readonly}
                    required
                    onChange={FieldModelUtilities.handleChange(channelModel, setChannelModel)}
                    value={FieldModelUtilities.getFieldModelValues(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.providerName)}
                    errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.providerName]}
                />
                <EndpointSelectField
                    id={DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId}
                    csrfToken={csrfToken}
                    endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                    fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId}
                    label="Provider Configuration"
                    description="The provider configuration to use with this distribution job."
                    clearable={false}
                    readOnly={readonly}
                    required
                    createRequestBody={createProviderRequestBody}
                    onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                    value={FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId)}
                    errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId]}
                />
                {renderChannelFields()}
                {FieldModelUtilities.hasValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName)
                    && FieldModelUtilities.hasValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId)
                    && (
                        <div>
                            <SelectInput
                                id={DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes}
                                name={DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes}
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
                                errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes]}
                            />
                            <SelectInput
                                id={DISTRIBUTION_COMMON_FIELD_KEYS.processingType}
                                name={DISTRIBUTION_COMMON_FIELD_KEYS.processingType}
                                label="Processing"
                                description={processingFieldDescription}
                                options={processingTypes}
                                readOnly={readonly}
                                required
                                onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                                value={FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.processingType)}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.processingType)}
                                errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.processingType]}
                            />
                            <CheckboxInput
                                id={DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject}
                                name={DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject}
                                label="Filter By Project"
                                description="If selected, only notifications from the selected Projects table will be processed. Otherwise notifications from all Projects are processed."
                                readOnly={readonly}
                                onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                                isChecked={FieldModelUtilities.getFieldModelBooleanValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject)}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject)}
                                errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject]}
                            />
                        </div>
                    )}
                {FieldModelUtilities.getFieldModelBooleanValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.filterByProject) && (
                    <div>
                        <TextInput
                            id={DISTRIBUTION_COMMON_FIELD_KEYS.projectNamePattern}
                            key={DISTRIBUTION_COMMON_FIELD_KEYS.projectNamePattern}
                            name={DISTRIBUTION_COMMON_FIELD_KEYS.projectNamePattern}
                            label="Project Name Pattern"
                            description="The regular expression to use to determine what Projects to include. These are in addition to the Projects selected in the table."
                            readOnly={readonly}
                            onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                            value={FieldModelUtilities.getFieldModelSingleValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.projectNamePattern)}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.projectNamePattern)}
                            errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.projectNamePattern]}
                        />
                        <TextInput
                            id={DISTRIBUTION_COMMON_FIELD_KEYS.projectVersionNamePattern}
                            key={DISTRIBUTION_COMMON_FIELD_KEYS.projectVersionNamePattern}
                            name={DISTRIBUTION_COMMON_FIELD_KEYS.projectVersionNamePattern}
                            label="Project Version Name Pattern"
                            description="The regular expression to use to determine what Project Versions to include."
                            readOnly={readonly}
                            onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                            value={FieldModelUtilities.getFieldModelSingleValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.projectVersionNamePattern)}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.projectVersionNamePattern)}
                            errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.projectVersionNamePattern]}
                        />
                        <TableSelectInput
                            id={DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects}
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
                            createRequestBody={createProviderRequestBody}
                            onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                            value={FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects)}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects)}
                            errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects]}
                        />
                    </div>
                )}
                {FieldModelUtilities.hasValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes)
                    && (
                        <CollapsiblePane
                            id="distribution-notification-filtering"
                            title="Black Duck Notification Filtering"
                            expanded={false}
                        >
                            <TableSelectInput
                                id={DISTRIBUTION_COMMON_FIELD_KEYS.policyFilter}
                                csrfToken={csrfToken}
                                endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                                fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.policyFilter}
                                columns={DISTRIBUTION_POLICY_SELECT_COLUMNS}
                                label="Policy Notification Type Filter"
                                description="Filter which notifications you want sent via this job (You must have the policy notification type selected for this filter to apply)."
                                readOnly={readonly}
                                paged
                                searchable
                                createRequestBody={createProviderRequestBody}
                                onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                                value={FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.policyFilter)}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.policyFilter)}
                                errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.policyFilter]}
                            />
                            <SelectInput
                                id={DISTRIBUTION_COMMON_FIELD_KEYS.vulnerabilitySeverityFilter}
                                name={DISTRIBUTION_COMMON_FIELD_KEYS.vulnerabilitySeverityFilter}
                                label="Vulnerability Notification Contains Severities"
                                description="Filters out the notifications that do not contain any of the relevant severities (You must have the vulnerability notification type selected for this filter to apply)."
                                options={DISTRIBUTION_VULNERABILITY_SEVERITY_OPTIONS}
                                multiSelect
                                readOnly={readonly}
                                onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                                value={FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.vulnerabilitySeverityFilter)}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.vulnerabilitySeverityFilter)}
                                errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.vulnerabilitySeverityFilter]}
                            />
                        </CollapsiblePane>
                    )}
            </CommonDistributionConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

DistributionConfigurationForm.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    descriptors: PropTypes.object.isRequired,
    lastUpdated: PropTypes.string
};

DistributionConfigurationForm.defaultProps = {
    lastUpdated: null
};

export default DistributionConfigurationForm;
