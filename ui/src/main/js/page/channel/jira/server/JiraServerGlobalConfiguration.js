import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'common/global/CommonGlobalConfiguration';
import { JIRA_SERVER_GLOBAL_FIELD_KEYS, JIRA_SERVER_INFO } from 'page/channel/jira/server/JiraServerModel';
import CommonGlobalConfigurationForm from 'common/global/CommonGlobalConfigurationForm';
import TextInput from 'common/input/TextInput';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import PasswordInput from 'common/input/PasswordInput';
import CheckboxInput from 'common/input/CheckboxInput';
import EndpointButtonField from 'common/input/field/EndpointButtonField';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';
import * as GlobalRequestHelper from 'common/global/GlobalRequestHelper';

const JiraServerGlobalConfiguration = ({
    csrfToken, errorHandler, readonly, displayTest, displaySave, displayDelete
}) => {
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, JIRA_SERVER_INFO.key));
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());

    const retrieveData = async () => {
        const data = await GlobalRequestHelper.getDataFindFirst(JIRA_SERVER_INFO.key, csrfToken);
        if (data) {
            setFormData(data);
        }
    };

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
                retrieveData={retrieveData}
                readonly={readonly}
                displayTest={displayTest}
                displaySave={displaySave}
                displayDelete={displayDelete}
                errorHandler={errorHandler}
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
                    errorValue={errors.fieldErrors[JIRA_SERVER_GLOBAL_FIELD_KEYS.url]}
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
                    errorValue={errors.fieldErrors[JIRA_SERVER_GLOBAL_FIELD_KEYS.username]}
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
                    errorValue={errors.fieldErrors[JIRA_SERVER_GLOBAL_FIELD_KEYS.password]}
                />
                <CheckboxInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck}
                    label="Disable Plugin Check"
                    description="This will disable checking whether the 'Alert Issue Property Indexer' plugin is installed on the specified Jira instance. Please ensure that the plugin is manually installed before using Alert with Jira. If not, issues created by Alert will not be updated properly, and duplicate issues may be created."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    isChecked={FieldModelUtilities.getFieldModelBooleanValue(formData, JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck)}
                    errorValue={errors.fieldErrors[JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck]}
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
                    readOnly={readonly || !displayTest}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin)}
                    errorValue={errors.fieldErrors[JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin]}
                />
            </CommonGlobalConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

JiraServerGlobalConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    displayTest: PropTypes.bool,
    displaySave: PropTypes.bool,
    displayDelete: PropTypes.bool
};

JiraServerGlobalConfiguration.defaultProps = {
    readonly: false,
    displayTest: true,
    displaySave: true,
    displayDelete: true
};

export default JiraServerGlobalConfiguration;
