import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'global/CommonGlobalConfiguration';
import { JIRA_SERVER_GLOBAL_FIELD_KEYS, JIRA_SERVER_INFO } from 'global/channels/jira/server/JiraServerModel';
import CommonGlobalConfigurationForm from 'global/CommonGlobalConfigurationForm';
import TextInput from 'field/input/TextInput';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import PasswordInput from 'field/input/PasswordInput';
import CheckboxInput from 'field/input/CheckboxInput';
import EndpointButtonField from 'field/EndpointButtonField';
import { CONTEXT_TYPE } from 'util/descriptorUtilities';

const JiraServerGlobalConfiguration = ({ csrfToken, readonly }) => {
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, JIRA_SERVER_INFO.key));
    const [errors, setErrors] = useState({});

    return (
        <CommonGlobalConfiguration
            label={JIRA_SERVER_INFO.label}
            description="Configure the Jira Server instance that Alert will send issue updates to."
            lastUpdated={formData.lastUpdated}
        >
            <CommonGlobalConfigurationForm
                setErrors={(data) => setErrors(data)}
                formData={formData}
                setFormData={(data) => setFormData(data)}
                csrfToken={csrfToken}
                buttonIdPrefix={JIRA_SERVER_INFO.key}
            >
                <TextInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.url}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.url}
                    label="URL"
                    description="The URL of the Jira Server server."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, JIRA_SERVER_GLOBAL_FIELD_KEYS.url)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_GLOBAL_FIELD_KEYS.url)}
                    errorValue={errors[JIRA_SERVER_GLOBAL_FIELD_KEYS.url]}
                />
                <TextInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.username}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.username}
                    label="User Name"
                    description="The username of the Jira Server user. Note: Unless 'Disable Plugin Check' is checked, this user must be a Jira admin."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, JIRA_SERVER_GLOBAL_FIELD_KEYS.username)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_GLOBAL_FIELD_KEYS.username)}
                    errorValue={errors[JIRA_SERVER_GLOBAL_FIELD_KEYS.username]}
                />
                <PasswordInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.password}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.password}
                    label="Password"
                    description="The password of the specified Jira Server user."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, JIRA_SERVER_GLOBAL_FIELD_KEYS.password)}
                    isSet={FieldModelUtilities.isFieldModelValueSet(formData, JIRA_SERVER_GLOBAL_FIELD_KEYS.password)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_GLOBAL_FIELD_KEYS.password)}
                    errorValue={errors[JIRA_SERVER_GLOBAL_FIELD_KEYS.password]}
                />
                <CheckboxInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck}
                    label="Disable Plugin Check"
                    description="This will disable checking whether the 'Alert Issue Property Indexer' plugin is installed on the specified Jira instance. Please ensure that the plugin is manually installed before using Alert with Jira. If not, issues created by Alert will not be updated properly."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    isChecked={FieldModelUtilities.getFieldModelBooleanValue(formData, JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck)}
                    errorValue={errors[JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck]}
                />
                <EndpointButtonField
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin}
                    buttonLabel="Install Plugin Remotely"
                    label="Configure Jira server plugin"
                    description="Installs a required plugin on the Jira server."
                    endpoint="/api/function"
                    fieldKey={JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin}
                    requiredRelatedFields={[
                        JIRA_SERVER_GLOBAL_FIELD_KEYS.url,
                        JIRA_SERVER_GLOBAL_FIELD_KEYS.username,
                        JIRA_SERVER_GLOBAL_FIELD_KEYS.password
                    ]}
                    csrfToken={csrfToken}
                    currentConfig={formData}
                    successBox={false}
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin)}
                    errorValue={errors[JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin]}
                />
            </CommonGlobalConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

JiraServerGlobalConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool
};

JiraServerGlobalConfiguration.defaultProps = {
    readonly: false
};

export default JiraServerGlobalConfiguration;
