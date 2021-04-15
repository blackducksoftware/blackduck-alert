import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'global/CommonGlobalConfiguration';
import { JIRA_CLOUD_GLOBAL_FIELD_KEYS, JIRA_CLOUD_INFO } from 'global/channels/jira/cloud/JiraCloudModel';
import CommonGlobalConfigurationForm from 'global/CommonGlobalConfigurationForm';
import TextInput from 'field/input/TextInput';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import PasswordInput from 'field/input/PasswordInput';
import CheckboxInput from 'field/input/CheckboxInput';
import EndpointButtonField from 'field/EndpointButtonField';
import { CONTEXT_TYPE } from 'util/descriptorUtilities';

const JiraCloudGlobalConfiguration = ({ csrfToken, readonly }) => {
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, JIRA_CLOUD_INFO.key));
    const [errors, setErrors] = useState({});

    return (
        <CommonGlobalConfiguration
            label={JIRA_CLOUD_INFO.label}
            description="Configure the Jira Cloud instance that Alert will send issue updates to."
            lastUpdated={formData.lastUpdated}
        >
            <CommonGlobalConfigurationForm
                setErrors={(data) => setErrors(data)}
                formData={formData}
                setFormData={(data) => setFormData(data)}
                csrfToken={csrfToken}
            >
                <TextInput
                    key={JIRA_CLOUD_GLOBAL_FIELD_KEYS.url}
                    name={JIRA_CLOUD_GLOBAL_FIELD_KEYS.url}
                    label="URL"
                    description="The URL of the Jira Cloud server."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, JIRA_CLOUD_GLOBAL_FIELD_KEYS.url)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_CLOUD_GLOBAL_FIELD_KEYS.url)}
                    errorValue={errors[JIRA_CLOUD_GLOBAL_FIELD_KEYS.url]}
                />
                <TextInput
                    key={JIRA_CLOUD_GLOBAL_FIELD_KEYS.emailAddress}
                    name={JIRA_CLOUD_GLOBAL_FIELD_KEYS.emailAddress}
                    label="Email Address"
                    description="The email address of the Jira Cloud user. Note: Unless 'Disable Plugin Check' is checked, this user must be a Jira admin."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, JIRA_CLOUD_GLOBAL_FIELD_KEYS.emailAddress)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_CLOUD_GLOBAL_FIELD_KEYS.emailAddress)}
                    errorValue={errors[JIRA_CLOUD_GLOBAL_FIELD_KEYS.emailAddress]}
                />
                <PasswordInput
                    key={JIRA_CLOUD_GLOBAL_FIELD_KEYS.accessToken}
                    name={JIRA_CLOUD_GLOBAL_FIELD_KEYS.accessToken}
                    label="API Token"
                    description="The API token of the specified Jira user."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, JIRA_CLOUD_GLOBAL_FIELD_KEYS.accessToken)}
                    isSet={FieldModelUtilities.isFieldModelValueSet(formData, JIRA_CLOUD_GLOBAL_FIELD_KEYS.accessToken)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_CLOUD_GLOBAL_FIELD_KEYS.accessToken)}
                    errorValue={errors[JIRA_CLOUD_GLOBAL_FIELD_KEYS.accessToken]}
                />
                <CheckboxInput
                    key={JIRA_CLOUD_GLOBAL_FIELD_KEYS.disablePluginCheck}
                    name={JIRA_CLOUD_GLOBAL_FIELD_KEYS.disablePluginCheck}
                    label="Disable Plugin Check"
                    description="This will disable checking whether the 'Alert Issue Property Indexer' plugin is installed on the specified Jira instance. Please ensure that the plugin is manually installed before using Alert with Jira. If not, issues created by Alert will not be updated properly."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    isChecked={FieldModelUtilities.getFieldModelBooleanValue(formData, JIRA_CLOUD_GLOBAL_FIELD_KEYS.disablePluginCheck)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_CLOUD_GLOBAL_FIELD_KEYS.disablePluginCheck)}
                    errorValue={errors[JIRA_CLOUD_GLOBAL_FIELD_KEYS.disablePluginCheck]}
                />
                <EndpointButtonField
                    key={JIRA_CLOUD_GLOBAL_FIELD_KEYS.configurePlugin}
                    name={JIRA_CLOUD_GLOBAL_FIELD_KEYS.configurePlugin}
                    buttonLabel="Install Plugin Remotely"
                    label="Configure Jira Cloud plugin"
                    description="Installs a required plugin on the Jira Cloud server."
                    endpoint="/api/function"
                    fieldKey={JIRA_CLOUD_GLOBAL_FIELD_KEYS.configurePlugin}
                    requiredRelatedFields={[
                        JIRA_CLOUD_GLOBAL_FIELD_KEYS.url,
                        JIRA_CLOUD_GLOBAL_FIELD_KEYS.emailAddress,
                        JIRA_CLOUD_GLOBAL_FIELD_KEYS.accessToken
                    ]}
                    csrfToken={csrfToken}
                    currentConfig={formData}
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_CLOUD_GLOBAL_FIELD_KEYS.configurePlugin)}
                    errorValue={errors[JIRA_CLOUD_GLOBAL_FIELD_KEYS.configurePlugin]}
                    successBox
                />
            </CommonGlobalConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

JiraCloudGlobalConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool
};

JiraCloudGlobalConfiguration.defaultProps = {
    readonly: false
};

export default JiraCloudGlobalConfiguration;
