import React, { useState } from 'react';
import { useHistory, useLocation, useParams } from 'react-router-dom';
import TextInput from 'common/input/TextInput';
import { BLACKDUCK_GLOBAL_FIELD_KEYS, BLACKDUCK_INFO, BLACKDUCK_URLS } from 'page/provider/blackduck/BlackDuckModel';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import PasswordInput from 'common/input/PasswordInput';
import NumberInput from 'common/input/NumberInput';
import CommonGlobalConfigurationForm from 'common/global/CommonGlobalConfigurationForm';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'common/global/CommonGlobalConfiguration';
import CheckboxInput from 'common/input/CheckboxInput';
import * as GlobalRequestHelper from 'common/global/GlobalRequestHelper';

const BlackDuckConfiguration = ({
    csrfToken, errorHandler, readonly, displayTest, displaySave
}) => {
    const { id } = useParams();
    const history = useHistory();
    const location = useLocation();
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, BLACKDUCK_INFO.key));
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());

    if (!FieldModelUtilities.hasValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.timeout)) {
        const defaultValue = FieldModelUtilities.updateFieldModelSingleValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.timeout, 300);
        setFormData(defaultValue);
    }

    if (!FieldModelUtilities.hasKey(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.enabled)) {
        const defaultValue = FieldModelUtilities.updateFieldModelSingleValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.enabled, true);
        setFormData(defaultValue);
    }

    const retrieveData = async () => {
        const data = await GlobalRequestHelper.getDataById(id, csrfToken);
        if (data) {
            setFormData(data);
        }
    };

    if (location.pathname.includes('/copy') && FieldModelUtilities.getFieldModelId(formData)) {
        const copy = FieldModelUtilities.updateFieldModelSingleValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey, '');
        delete copy.id;
        setFormData(copy);
    }

    return (
        <CommonGlobalConfiguration
            label={BLACKDUCK_INFO.label}
            description={BLACKDUCK_INFO.description}
            lastUpdated={formData.lastUpdated}
        >
            <CommonGlobalConfigurationForm
                setErrors={(error) => setErrors(error)}
                formData={formData}
                setFormData={(data) => setFormData(data)}
                csrfToken={csrfToken}
                displayDelete={false}
                afterSuccessfulSave={() => history.push(BLACKDUCK_URLS.blackDuckTableUrl)}
                retrieveData={retrieveData}
                readonly={readonly}
                displayTest={displayTest}
                displaySave={displaySave}
                displayCancel
                errorHandler={errorHandler}
            >
                <CheckboxInput
                    id={BLACKDUCK_GLOBAL_FIELD_KEYS.enabled}
                    name={BLACKDUCK_GLOBAL_FIELD_KEYS.enabled}
                    label="Enabled"
                    description="If selected, this provider configuration will be able to pull data into Alert and available to configure with distribution jobs, otherwise, it will not be available for those usages."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    isChecked={FieldModelUtilities.getFieldModelBooleanValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.enabled)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.enabled)}
                    errorValue={errors.fieldErrors[BLACKDUCK_GLOBAL_FIELD_KEYS.enabled]}
                />
                <TextInput
                    id={BLACKDUCK_GLOBAL_FIELD_KEYS.name}
                    name={BLACKDUCK_GLOBAL_FIELD_KEYS.name}
                    label="Provider Configuration"
                    description="The name of this provider configuration. Must be unique."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.name)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.name)}
                    errorValue={errors.fieldErrors[BLACKDUCK_GLOBAL_FIELD_KEYS.name]}
                />
                <TextInput
                    id={BLACKDUCK_GLOBAL_FIELD_KEYS.url}
                    name={BLACKDUCK_GLOBAL_FIELD_KEYS.url}
                    label="URL"
                    description="The URL of the Black Duck server."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.url)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.url)}
                    errorValue={errors.fieldErrors[BLACKDUCK_GLOBAL_FIELD_KEYS.url]}
                />
                <PasswordInput
                    id={BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey}
                    name={BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey}
                    label="API Token"
                    description="The API token used to retrieve data from the Black Duck server. The API token should be for a super user."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey)}
                    isSet={FieldModelUtilities.isFieldModelValueSet(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey)}
                    errorValue={errors.fieldErrors[BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey]}
                />
                <NumberInput
                    id={BLACKDUCK_GLOBAL_FIELD_KEYS.timeout}
                    name={BLACKDUCK_GLOBAL_FIELD_KEYS.timeout}
                    label="Timeout"
                    description="The timeout in seconds for all connections to the Black Duck server."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelNumberValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.timeout)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.timeout)}
                    errorValue={errors.fieldErrors[BLACKDUCK_GLOBAL_FIELD_KEYS.timeout]}
                />
            </CommonGlobalConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

BlackDuckConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    displayTest: PropTypes.bool,
    displaySave: PropTypes.bool
};

BlackDuckConfiguration.defaultProps = {
    readonly: false,
    displayTest: true,
    displaySave: true
};

export default BlackDuckConfiguration;
