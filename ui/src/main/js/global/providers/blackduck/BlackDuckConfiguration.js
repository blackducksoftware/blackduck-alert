import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import TextInput from 'field/input/TextInput';
import { BLACKDUCK_GLOBAL_FIELD_KEYS, BLACKDUCK_INFO } from 'global/providers/blackduck/BlackDuckModel';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import PasswordInput from 'field/input/PasswordInput';
import NumberInput from 'field/input/NumberInput';
import CommonGlobalConfigurationForm from 'global/CommonGlobalConfigurationForm';
import { CONTEXT_TYPE } from 'util/descriptorUtilities';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'global/CommonGlobalConfiguration';
import { AUTHENTICATION_LDAP_FIELD_KEYS } from '../../components/auth/AuthenticationModel';
import CheckboxInput from '../../../field/input/CheckboxInput';

const BlackDuckConfiguration = ({ csrfToken, readonly }) => {
    const { id } = useParams();
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, BLACKDUCK_INFO.key));
    const [errors, setErrors] = useState({});

    if (!FieldModelUtilities.hasValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.timeout)) {
        const defaultValue = FieldModelUtilities.updateFieldModelSingleValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.timeout, 300);
        setFormData(defaultValue);
    }

    if (!FieldModelUtilities.hasValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.enabled)) {
        const defaultValue = FieldModelUtilities.updateFieldModelSingleValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.enabled, true);
        setFormData(defaultValue);
    }

    return (
        <CommonGlobalConfiguration
            label={BLACKDUCK_INFO.label}
            description={BLACKDUCK_INFO.description}
        >
            <CommonGlobalConfigurationForm
                setErrors={(error) => setErrors(error)}
                formData={formData}
                setFormData={(data) => setFormData(data)}
                csrfToken={csrfToken}
                displayDelete={false}
            >
                <CheckboxInput
                    key={BLACKDUCK_GLOBAL_FIELD_KEYS.enabled}
                    name={BLACKDUCK_GLOBAL_FIELD_KEYS.enabled}
                    label="Enabled"
                    description="If selected, this provider configuration will be able to pull data into Alert and available to configure with distribution jobs, otherwise, it will not be available for those usages."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    isChecked={FieldModelUtilities.getFieldModelBooleanValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.enabled)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.enabled)}
                    errorValue={errors[BLACKDUCK_GLOBAL_FIELD_KEYS.enabled]}
                />
                <TextInput
                    key={BLACKDUCK_GLOBAL_FIELD_KEYS.name}
                    name={BLACKDUCK_GLOBAL_FIELD_KEYS.name}
                    label="Provider Configuration"
                    description="The name of this provider configuration. Must be unique."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.name)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.name)}
                    errorValue={errors[BLACKDUCK_GLOBAL_FIELD_KEYS.name]}
                />
                <TextInput
                    key={BLACKDUCK_GLOBAL_FIELD_KEYS.url}
                    name={BLACKDUCK_GLOBAL_FIELD_KEYS.url}
                    label="URL"
                    description="The URL of the Black Duck server."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.url)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.url)}
                    errorValue={errors[BLACKDUCK_GLOBAL_FIELD_KEYS.url]}
                />
                <PasswordInput
                    key={BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey}
                    name={BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey}
                    label="API Token"
                    description="The API token used to retrieve data from the Black Duck server. The API token should be for a super user."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey)}
                    isSet={FieldModelUtilities.isFieldModelValueSet(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey)}
                    errorValue={errors[BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey]}
                />
                <NumberInput
                    key={BLACKDUCK_GLOBAL_FIELD_KEYS.timeout}
                    name={BLACKDUCK_GLOBAL_FIELD_KEYS.timeout}
                    label="Timeout"
                    description="The timeout in seconds for all connections to the Black Duck server."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.timeout)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.timeout)}
                    errorValue={errors[BLACKDUCK_GLOBAL_FIELD_KEYS.timeout]}
                />
            </CommonGlobalConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

BlackDuckConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool
};

BlackDuckConfiguration.defaultProps = {
    readonly: false
};

export default BlackDuckConfiguration;
