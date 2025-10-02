import React, { useEffect, useState } from 'react';
import Select, { components } from 'react-select';
import * as PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import CheckboxInput from 'common/component/input/CheckboxInput';
import SelectInput from 'common/component/input/DynamicSelectInput';
import {
    DISTRIBUTION_CHANNEL_OPTIONS,
    DISTRIBUTION_COMMON_FIELD_KEYS,
    DISTRIBUTION_CONFIGURATION_INFO,
    DISTRIBUTION_FREQUENCY_OPTIONS,
    DISTRIBUTION_NOTIFICATION_TYPE_OPTIONS,
    DISTRIBUTION_PROCESSING_DESCRIPTIONS,
    DISTRIBUTION_PROCESSING_TYPES,
    DISTRIBUTION_TEST_FIELD_KEYS,
    DISTRIBUTION_URLS,
    DISTRIBUTION_VULNERABILITY_SEVERITY_OPTIONS
} from 'page/distribution/DistributionModel';
import EndpointSelectField from 'common/component/input/EndpointSelectField';
import TextInput from 'common/component/input/TextInput';
import CollapsiblePane from 'common/component/CollapsiblePane';
import { useHistory, useLocation, useParams } from 'react-router-dom';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { CONTEXT_TYPE, isOneOperationAssigned, isOperationAssigned, OPERATIONS } from 'common/util/descriptorUtilities';
import CommonDistributionConfigurationForm from 'page/distribution/CommonDistributionConfigurationForm';
import * as DistributionRequestUtility from 'page/distribution/DistributionRequestUtility';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import { AZURE_BOARDS_INFO } from 'page/channel/azure/AzureBoardsModel';
import { BLACKDUCK_INFO } from 'page/provider/blackduck/BlackDuckModel';
import { EMAIL_INFO } from 'page/channel/email/EmailModels';
import { JIRA_CLOUD_INFO } from 'page/channel/jira/cloud/JiraCloudModel';
import { JIRA_SERVER_INFO } from 'page/channel/jira/server/JiraServerModel';
import { MSTEAMS_INFO } from 'page/channel/msteams/MSTeamsModel';
import { SLACK_INFO } from 'page/channel/slack/SlackModels';
import AzureBoardsDistributionConfiguration from 'page/channel/azure/AzureBoardsDistributionConfiguration';
import EmailDistributionConfiguration from 'page/channel/email/EmailDistributionConfiguration';
import JiraCloudDistributionConfiguration from 'page/channel/jira/cloud/JiraCloudDistributionConfiguration';
import JiraServerDistributionConfiguration from 'page/channel/jira/server/JiraServerDistributionConfiguration';
import MsTeamsDistributionConfiguration from 'page/channel/msteams/MsTeamsDistributionConfiguration';
import SlackDistributionConfiguration from 'page/channel/slack/SlackDistributionConfiguration';
import PageHeader from 'common/component/navigation/PageHeader';
import { createNewConfigurationRequest } from 'common/util/configurationRequestBuilder';
import DynamicSelectInput from 'common/component/input/DynamicSelectInput';
import ProjectSelectModal from 'page/distribution/ProjectSelectModal';

const DistributionConfigurationForm = ({
    csrfToken, errorHandler, descriptors
}) => {
    const { id } = useParams();
    const history = useHistory();
    const location = useLocation();
    const [formData, setFormData] = useState({});
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());
    const [channelModel, setChannelModel] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.DISTRIBUTION, AZURE_BOARDS_INFO.key));
    const [specificChannelModel, setSpecificChannelModel] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.DISTRIBUTION, AZURE_BOARDS_INFO.key));
    const [providerModel, setProviderModel] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.DISTRIBUTION, BLACKDUCK_INFO.key));
    const [selectedChannel, setSelectedChannel] = useState('');
    const [testFieldModel, setTestFieldModel] = useState({});
    const [readonly, setReadonly] = useState(false);
    const [processingTypes, setProcessingTypes] = useState(DISTRIBUTION_PROCESSING_TYPES);
    const [showProjectSelectModal, setShowProjectSelectModal] = useState(false);

    const retrieveData = async () => DistributionRequestUtility.getDataById(id, csrfToken, errorHandler, setErrors);

    const createDistributionData = (channelModelData, providerModelData) => {
        const providerConfig = JSON.parse(JSON.stringify(providerModelData));
        let configuredProviderProjects = [];

        const fieldConfiguredProjects = FieldModelUtilities.getFieldModelValues(providerConfig, DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects);
        
        // Determine if fieldConfiguredProjects has shape { label: projectName, href: projectHREF }
        //      If true change shape to { name: projectName, href: projectHREF, missing: false }
        //      If false, data shape is OK
        if (fieldConfiguredProjects.some(project => project.hasOwnProperty('label'))) {
            configuredProviderProjects = fieldConfiguredProjects.map((selectedValue) => ({
                name: selectedValue.label,
                href: selectedValue.value,
                missing: false
            }));
        } else {
            configuredProviderProjects = fieldConfiguredProjects;
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

    const createCommonRequestBody = () => {
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
            : FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.DISTRIBUTION, AZURE_BOARDS_INFO.key);
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
            case AZURE_BOARDS_INFO.key:
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

    // For channels that are using GlobalConfigModels
    const globalConfigSetSpecificChannelModel = () => {
        if (!FieldModelUtilities.hasKey(specificChannelModel, DISTRIBUTION_COMMON_FIELD_KEYS.channelGlobalConfigId)) {
            const commonGlobalConfigId = FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.channelGlobalConfigId);
            setSpecificChannelModel(FieldModelUtilities.updateFieldModelSingleValue(specificChannelModel, DISTRIBUTION_COMMON_FIELD_KEYS.channelGlobalConfigId, commonGlobalConfigId));
        }
    };

    const renderChannelFields = () => {
        switch (selectedChannel.toString()) {
            case AZURE_BOARDS_INFO.key: {
                globalConfigSetSpecificChannelModel();
                return (
                    <AzureBoardsDistributionConfiguration
                        csrfToken={csrfToken}
                        data={specificChannelModel}
                        setData={setSpecificChannelModel}
                        errors={errors}
                        readonly={readonly}
                    />
                );
            }
            case EMAIL_INFO.key:
                return (
                    <EmailDistributionConfiguration
                        csrfToken={csrfToken}
                        createAdditionalEmailRequestBody={createCommonRequestBody}
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
            case JIRA_SERVER_INFO.key: {
                globalConfigSetSpecificChannelModel();
                return (
                    <JiraServerDistributionConfiguration
                        csrfToken={csrfToken}
                        data={specificChannelModel}
                        setData={setSpecificChannelModel}
                        errors={errors}
                        readonly={readonly}
                    />
                );
            }
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

    const getPolicyFiltersRequest = () => {
        const apiUrl = '/alert/api/function/blackduck.policy.notification.filter?pageNumber=0&pageSize=1000&searchTerm=';
        return createNewConfigurationRequest(apiUrl, csrfToken, createProviderRequestBody());
    };

    const convertPolicyDataToOptions = (responseData) => {
        const { models } = responseData;
        return models.map((model) => {
            const { name } = model;
            return {
                label: name,
                value: name
            };
        });
    };

    const removeSelectedProject = (option) => {
        const options = FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects);
        const parsedArray = options.filter((project) => project.value !== option.value);
        return FieldModelUtilities.handleChange(providerModel, setProviderModel)(
            {
                target: {
                    name: DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects,
                    value: parsedArray
                }
            }
        );
    };

    function getProjectValues(data) {
        if (data.some(project => project.hasOwnProperty('label'))) {
            return data;
        }

        return data.map((project) => (
            {label: project.name, value: project.href}
        ))
    }
    console.log(!isOperationAssigned(descriptors[selectedChannel], OPERATIONS.EXECUTE));
    // TODO need to provide finer grain control with permissions.
    return (
        <>
            <PageHeader
                title={DISTRIBUTION_CONFIGURATION_INFO.label}
                description={DISTRIBUTION_CONFIGURATION_INFO.description}
                icon={['fas', 'tasks']}
            />
            <CommonDistributionConfigurationForm
                setErrors={setErrors}
                formData={formData}
                setFormData={setFormData}
                testFields={testFields}
                testFormData={testFieldModel}
                setTestFormData={setTestFieldModel}
                csrfToken={csrfToken}
                displaySave={!readonly}
                isSaveDisabled={!isOneOperationAssigned(descriptors[selectedChannel], [OPERATIONS.WRITE, OPERATIONS.CREATE])}
                displayTest={!readonly}
                isTestDisabled={!isOperationAssigned(descriptors[selectedChannel], OPERATIONS.EXECUTE)}
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
                    label="Provider"
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
                        <DynamicSelectInput
                            id={DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects}
                            label="Projects"
                            description="Select a project or projects that will be used to retrieve notifications from your provider."
                            csrfToken={csrfToken}
                            readOnly={readonly}
                            onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects)}
                            errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects]}
                            customSelect={(
                                <>
                                    <div className="typeAheadField">
                                        <Select
                                            noOptionsMessage={() => null}
                                            openMenuOnClick={false}
                                            isSearchable={false}
                                            placeholder="Select Projects..."
                                            value={getProjectValues(FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects))}
                                            isMulti
                                            isClearable
                                            styles={{
                                                dropdownIndicator: (base) => ({
                                                    ...base,
                                                    padding: '12px'
                                                }),
                                                multiValueRemove: (base) => ({
                                                    ...base,
                                                    padding: '7px'
                                                }),
                                            }}
                                            components={{
                                                DropdownIndicator: ({ ...props }) => (
                                                    <components.DropdownIndicator
                                                        {...props}
                                                        onClick={() => setShowProjectSelectModal(true)}
                                                    >
                                                        <FontAwesomeIcon icon="plus" onClick={() => setShowProjectSelectModal(true)}/>
                                                    </components.DropdownIndicator>
                                                ),
                                                MultiValueRemove: ({ ...props }) => (
                                                    <components.MultiValueRemove
                                                        {...props}
                                                    >
                                                        <FontAwesomeIcon
                                                            icon="times"
                                                            onClick={() => removeSelectedProject(props.data)}
                                                            size="xs"
                                                        />
                                                    </components.MultiValueRemove>
                                                )
                                            }}
                                        />
                                    </div>
                                </>
                            )}
                        />
                        {showProjectSelectModal && (
                            <ProjectSelectModal
                                isOpen={showProjectSelectModal}
                                handleClose={() => setShowProjectSelectModal(false)}
                                csrfToken={csrfToken}
                                projectRequestBody={createCommonRequestBody}
                                handleSubmit={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                                formData={getProjectValues(FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects))}
                            />
                        )}
                    </div>
                )}
                {FieldModelUtilities.hasValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.notificationTypes)
                    && (
                        <CollapsiblePane
                            id="distribution-notification-filtering"
                            title="Black Duck Notification Filtering"
                            expanded={false}
                        >
                            <EndpointSelectField
                                id={DISTRIBUTION_COMMON_FIELD_KEYS.policyFilter}
                                csrfToken={csrfToken}
                                endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                                fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.policyFilter}
                                label="Policy Notification Type Filter"
                                description="Filter which notifications you want sent via this job (You must have the policy notification type selected for this filter to apply)."
                                searchable
                                multiSelect
                                readOnly={readonly}
                                readOptionsRequest={getPolicyFiltersRequest}
                                convertDataToOptions={convertPolicyDataToOptions}
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
                <SelectInput
                    id={DISTRIBUTION_COMMON_FIELD_KEYS.channelName}
                    name={DISTRIBUTION_COMMON_FIELD_KEYS.channelName}
                    label="Channel"
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
                {renderChannelFields()}
            </CommonDistributionConfigurationForm>
        </>
    );
};

DistributionConfigurationForm.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    descriptors: PropTypes.object.isRequired
};

export default DistributionConfigurationForm;
