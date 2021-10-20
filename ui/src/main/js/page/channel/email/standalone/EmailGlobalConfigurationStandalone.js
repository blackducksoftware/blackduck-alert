import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'common/global/CommonGlobalConfiguration';
import {
    EMAIL_GLOBAL_ADVANCED_FIELD_KEYS, EMAIL_GLOBAL_FIELD_KEYS, EMAIL_INFO, EMAIL_TEST_FIELD
} from 'page/channel/email/EmailModels';
import CommonGlobalConfigurationForm from 'common/global/CommonGlobalConfigurationForm';
import TextInput from 'common/input/TextInput';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import CheckboxInput from 'common/input/CheckboxInput';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';
import * as GlobalRequestHelper from 'common/global/GlobalRequestHelper';
import PasswordInput from 'common/input/PasswordInput';

const EmailGlobalConfigurationStandalone = ({
    csrfToken, errorHandler, readonly, displayTest, displaySave, displayDelete
}) => {
    const [fieldModel, setFieldModel] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, EMAIL_INFO.key));
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());
    const [testFieldData, setTestFieldData] = useState({});

    const testField = (
        <TextInput
            id={EMAIL_TEST_FIELD.key}
            name={EMAIL_TEST_FIELD.key}
            label={EMAIL_TEST_FIELD.label}
            description={EMAIL_TEST_FIELD.description}
            onChange={FieldModelUtilities.handleTestChange(testFieldData, setTestFieldData)}
            value={testFieldData[EMAIL_TEST_FIELD.key]}
        />
    );

    const hasAdvancedConfig = Object.keys(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS).some((key) => FieldModelUtilities.hasValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS[key]));

    const retrieveData = async () => {
        const data = await GlobalRequestHelper.getDataFindFirst(EMAIL_INFO.key, csrfToken);
        if (data) {
            setFieldModel(data);
        }
    };

    return (
        <CommonGlobalConfiguration
            label={`${EMAIL_INFO.label} Beta`}
            description="Configure the email server that Alert will send emails to."
            lastUpdated={fieldModel.lastUpdated}
        >
            <CommonGlobalConfigurationForm
                csrfToken={csrfToken}
                formData={fieldModel}
                setErrors={(errors) => setErrors(errors)}
                setFormData={(content) => setFieldModel(content)}
                testFields={testField}
                testFormData={testFieldData}
                setTestFormData={(values) => setTestFieldData(values)}
                buttonIdPrefix={EMAIL_INFO.key}
                retrieveData={retrieveData}
                readonly={readonly}
                displayTest={displayTest}
                displaySave={displaySave}
                displayDelete={displayDelete}
                errorHandler={errorHandler}
            >
                <TextInput
                    id={EMAIL_GLOBAL_FIELD_KEYS.host}
                    name={EMAIL_GLOBAL_FIELD_KEYS.host}
                    label="SMTP Host"
                    description="The host name of the SMTP email server."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_FIELD_KEYS.host)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_FIELD_KEYS.host)}
                    errorValue={errors.fieldErrors[EMAIL_GLOBAL_FIELD_KEYS.host]}
                />
                <TextInput
                    id={EMAIL_GLOBAL_FIELD_KEYS.from}
                    name={EMAIL_GLOBAL_FIELD_KEYS.from}
                    label="SMTP From"
                    description="The email address to use as the return address."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_FIELD_KEYS.from)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_FIELD_KEYS.from)}
                    errorValue={errors.fieldErrors[EMAIL_GLOBAL_FIELD_KEYS.from]}
                />
                <CheckboxInput
                    id={EMAIL_GLOBAL_FIELD_KEYS.auth}
                    name={EMAIL_GLOBAL_FIELD_KEYS.auth}
                    label="SMTP Auth"
                    description="Select this if your SMTP server requires authentication, then fill in the SMTP User and SMTP Password."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                    isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_FIELD_KEYS.auth)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_FIELD_KEYS.auth)}
                    errorValue={errors.fieldErrors[EMAIL_GLOBAL_FIELD_KEYS.auth]}
                />
                <TextInput
                    id={EMAIL_GLOBAL_FIELD_KEYS.user}
                    name={EMAIL_GLOBAL_FIELD_KEYS.user}
                    label="SMTP User"
                    description="The username to authenticate with the SMTP server."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_FIELD_KEYS.user)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_FIELD_KEYS.user)}
                    errorValue={errors.fieldErrors[EMAIL_GLOBAL_FIELD_KEYS.user]}
                />
                <PasswordInput
                    id={EMAIL_GLOBAL_FIELD_KEYS.password}
                    name={EMAIL_GLOBAL_FIELD_KEYS.password}
                    label="SMTP Password"
                    description="The password to authenticate with the SMTP server."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_FIELD_KEYS.password)}
                    isSet={FieldModelUtilities.isFieldModelValueSet(fieldModel, EMAIL_GLOBAL_FIELD_KEYS.password)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_FIELD_KEYS.password)}
                    errorValue={errors.fieldErrors[EMAIL_GLOBAL_FIELD_KEYS.password]}
                />
            </CommonGlobalConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

EmailGlobalConfigurationStandalone.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    displayTest: PropTypes.bool,
    displaySave: PropTypes.bool,
    displayDelete: PropTypes.bool
};

EmailGlobalConfigurationStandalone.defaultProps = {
    readonly: false,
    displayTest: true,
    displaySave: true,
    displayDelete: true
};

export default EmailGlobalConfigurationStandalone;
