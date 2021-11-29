import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'common/global/CommonGlobalConfiguration';
import { JIRA_CLOUD_GLOBAL_FIELD_KEYS, JIRA_CLOUD_INFO } from 'page/channel/jira/cloud/JiraCloudModel';
import CommonGlobalConfigurationForm from 'common/global/CommonGlobalConfigurationForm';
import TextInput from 'common/input/TextInput';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import PasswordInput from 'common/input/PasswordInput';
import CheckboxInput from 'common/input/CheckboxInput';
import EndpointButtonField from 'common/input/field/EndpointButtonField';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';
import * as GlobalRequestHelper from 'common/global/GlobalRequestHelper';

const JiraCloudGlobalConfiguration = ({
    csrfToken, errorHandler, readonly, displayTest, displaySave, displayDelete
}) => {
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, JIRA_CLOUD_INFO.key));
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());

    const retrieveData = async () => {
        const data = await GlobalRequestHelper.getDataFindFirst(JIRA_CLOUD_INFO.key, csrfToken);
        if (data) {
            setFormData(data);
        }
    };

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
                buttonIdPrefix={JIRA_CLOUD_INFO.key}
                retrieveData={retrieveData}
                readonly={readonly}
                displayTest={displayTest}
                displaySave={displaySave}
                displayDelete={displayDelete}
                errorHandler={errorHandler}
            >
                <TextInput
                    id={JIRA_CLOUD_GLOBAL_FIELD_KEYS.url}
                    name={JIRA_CLOUD_GLOBAL_FIELD_KEYS.url}
                    label="URL"
                    description="The URL of the Jira Cloud server."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, JIRA_CLOUD_GLOBAL_FIELD_KEYS.url)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_CLOUD_GLOBAL_FIELD_KEYS.url)}
                    errorValue={errors.fieldErrors[JIRA_CLOUD_GLOBAL_FIELD_KEYS.url]}
                />
                <TextInput
                    id={JIRA_CLOUD_GLOBAL_FIELD_KEYS.emailAddress}
                    name={JIRA_CLOUD_GLOBAL_FIELD_KEYS.emailAddress}
                    label="Email Address"
                    description="The email address of the Jira Cloud user. Note: Unless 'Disable Plugin Check' is checked, this user must be a Jira admin."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, JIRA_CLOUD_GLOBAL_FIELD_KEYS.emailAddress)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_CLOUD_GLOBAL_FIELD_KEYS.emailAddress)}
                    errorValue={errors.fieldErrors[JIRA_CLOUD_GLOBAL_FIELD_KEYS.emailAddress]}
                />
                <PasswordInput
                    id={JIRA_CLOUD_GLOBAL_FIELD_KEYS.accessToken}
                    name={JIRA_CLOUD_GLOBAL_FIELD_KEYS.accessToken}
                    label="API Token"
                    description="The API token of the specified Jira user."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, JIRA_CLOUD_GLOBAL_FIELD_KEYS.accessToken)}
                    isSet={FieldModelUtilities.isFieldModelValueSet(formData, JIRA_CLOUD_GLOBAL_FIELD_KEYS.accessToken)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_CLOUD_GLOBAL_FIELD_KEYS.accessToken)}
                    errorValue={errors.fieldErrors[JIRA_CLOUD_GLOBAL_FIELD_KEYS.accessToken]}
                />
                <CheckboxInput
                    id={JIRA_CLOUD_GLOBAL_FIELD_KEYS.disablePluginCheck}
                    name={JIRA_CLOUD_GLOBAL_FIELD_KEYS.disablePluginCheck}
                    label="Disable Plugin Check"
                    description="This will disable checking whether the 'Alert Issue Property Indexer' plugin is installed on the specified Jira instance. Please ensure that the plugin is manually installed before using Alert with Jira. If not, issues created by Alert will not be updated properly, and duplicate issues may be created."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    isChecked={FieldModelUtilities.getFieldModelBooleanValue(formData, JIRA_CLOUD_GLOBAL_FIELD_KEYS.disablePluginCheck)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_CLOUD_GLOBAL_FIELD_KEYS.disablePluginCheck)}
                    errorValue={errors.fieldErrors[JIRA_CLOUD_GLOBAL_FIELD_KEYS.disablePluginCheck]}
                />
                <EndpointButtonField
                    id={JIRA_CLOUD_GLOBAL_FIELD_KEYS.configurePlugin}
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
                    successBox={false}
                    readOnly={readonly || !displayTest}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_CLOUD_GLOBAL_FIELD_KEYS.configurePlugin)}
                    errorValue={errors.fieldErrors[JIRA_CLOUD_GLOBAL_FIELD_KEYS.configurePlugin]}
                />
            </CommonGlobalConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

JiraCloudGlobalConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    displayTest: PropTypes.bool,
    displaySave: PropTypes.bool,
    displayDelete: PropTypes.bool
};

JiraCloudGlobalConfiguration.defaultProps = {
    readonly: false,
    displayTest: true,
    displaySave: true,
    displayDelete: true
};

export default JiraCloudGlobalConfiguration;
