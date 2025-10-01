import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'common/configuration/global/CommonGlobalConfiguration';
import { JIRA_CLOUD_GLOBAL_FIELD_KEYS, JIRA_CLOUD_INFO } from 'page/channel/jira/cloud/JiraCloudModel';
import CommonGlobalConfigurationForm from 'common/configuration/global/CommonGlobalConfigurationForm';
import TextInput from 'common/component/input/TextInput';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import PasswordInput from 'common/component/input/PasswordInput';
import CheckboxInput from 'common/component/input/CheckboxInput';
import EndpointButtonField from 'common/component/input/field/EndpointButtonField';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';
import * as GlobalRequestHelper from 'common/configuration/global/GlobalRequestHelper';
import NumberInput from 'common/component/input/NumberInput';

const JiraCloudGlobalConfiguration = ({
    csrfToken, errorHandler, readonly, displayTest, displaySave, displayDelete
}) => {
    const initModelFunction = () => {
        let initModel = FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, JIRA_CLOUD_INFO.key);
        initModel = FieldModelUtilities.updateFieldModelSingleValue(initModel, JIRA_CLOUD_GLOBAL_FIELD_KEYS.timeout, '300');
        return initModel;
    };
    const [formData, setFormData] = useState(initModelFunction());
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());

    const retrieveData = async () => {
        const data = await GlobalRequestHelper.getDataFindFirst(JIRA_CLOUD_INFO.key, csrfToken);
        if (data) {
            let updatedFormData = data;
            if (!FieldModelUtilities.hasValue(data, JIRA_CLOUD_GLOBAL_FIELD_KEYS.timeout)) {
                updatedFormData = FieldModelUtilities.updateFieldModelSingleValue(data, JIRA_CLOUD_GLOBAL_FIELD_KEYS.timeout, '300');
            }
            setFormData(updatedFormData);
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
                <NumberInput
                    id={JIRA_CLOUD_GLOBAL_FIELD_KEYS.timeout}
                    name={JIRA_CLOUD_GLOBAL_FIELD_KEYS.timeout}
                    label="Timeout"
                    description="The timeout in seconds for all connections to Jira Cloud."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelNumberValue(formData, JIRA_CLOUD_GLOBAL_FIELD_KEYS.timeout)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_CLOUD_GLOBAL_FIELD_KEYS.timeout)}
                    errorValue={errors.fieldErrors[JIRA_CLOUD_GLOBAL_FIELD_KEYS.timeout]}
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
                        JIRA_CLOUD_GLOBAL_FIELD_KEYS.accessToken,
                        JIRA_CLOUD_GLOBAL_FIELD_KEYS.timeout
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
