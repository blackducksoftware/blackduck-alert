import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { createNewConfigurationRequest } from 'common/util/configurationRequestBuilder';
import DynamicSelectInput from 'common/input/DynamicSelectInput';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import { LabelFieldPropertyDefaults } from './field/LabeledField';

// TODO Remove currentConfig and requiredRelatedFields
const EndpointSelectField = ({
    id,
    clearable,
    createRequestBody,
    csrfToken,
    currentConfig,
    description,
    endpoint,
    errorName,
    errorValue,
    fieldKey,
    inputClass,
    label,
    labelClass,
    multiSelect,
    onChange,
    placeholder,
    readOnly,
    removeSelected,
    required,
    requiredRelatedFields,
    searchable,
    selectSpacingClass,
    showDescriptionPlaceHolder,
    value
}) => {
    const [options, setOptions] = useState([]);
    const [requestErrorValue, setRequestErrorValue] = useState(null);

    const onSendClick = async () => {
        const newFieldModel = createRequestBody ? createRequestBody() : FieldModelUtilities.createFieldModelFromRequestedFields(currentConfig, requiredRelatedFields);
        const request = createNewConfigurationRequest(`/alert${endpoint}/${fieldKey}`, csrfToken, newFieldModel);
        request.then((response) => {
            if (response.ok) {
                response.json().then((data) => {
                    const selectOptions = data.options.map((item) => {
                        const dataValue = item.value;
                        return {
                            key: dataValue,
                            label: item.label,
                            value: dataValue
                        };
                    });
                    setOptions(selectOptions);
                    setRequestErrorValue(null);
                });
            } else {
                response.json()
                    .then((data) => {
                        setOptions([]);
                        setRequestErrorValue(HTTPErrorUtils.createFieldError(data.message));
                    });
            }
        });
    };

    useEffect(() => {
        onSendClick();
    }, []);

    return (
        <div>
            <DynamicSelectInput
                onChange={onChange}
                onFocus={onSendClick}
                options={options}
                id={id || fieldKey}
                name={id || fieldKey}
                inputClass={inputClass}
                searchable={searchable}
                placeholder={placeholder}
                value={value}
                removeSelected={removeSelected}
                multiSelect={multiSelect}
                selectSpacingClass={selectSpacingClass}
                readOnly={readOnly}
                clearable={clearable}
                labelClass={labelClass}
                description={description}
                showDescriptionPlaceHolder={showDescriptionPlaceHolder}
                label={label}
                errorName={errorName}
                errorValue={errorValue || requestErrorValue}
                required={required}
            />
        </div>
    );
};

EndpointSelectField.propTypes = {
    id: PropTypes.string,
    clearable: PropTypes.bool,
    csrfToken: PropTypes.string.isRequired,
    currentConfig: PropTypes.object,
    endpoint: PropTypes.string.isRequired,
    fieldKey: PropTypes.string.isRequired,
    inputClass: PropTypes.string,
    multiSelect: PropTypes.bool,
    placeholder: PropTypes.string,
    onChange: PropTypes.func.isRequired,
    readOnly: PropTypes.bool,
    removeSelected: PropTypes.bool,
    requiredRelatedFields: PropTypes.array,
    searchable: PropTypes.bool,
    selectSpacingClass: PropTypes.string,
    value: PropTypes.array,
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    required: PropTypes.bool,
    showDescriptionPlaceHolder: PropTypes.bool,
    createRequestBody: PropTypes.func

};

EndpointSelectField.defaultProps = {
    id: 'endpointSelectFieldId',
    clearable: true,
    currentConfig: {},
    inputClass: 'typeAheadField',
    labelClass: 'col-sm-3',
    multiSelect: false,
    placeholder: 'Choose a value',
    readOnly: false,
    removeSelected: false,
    requiredRelatedFields: [],
    searchable: false,
    selectSpacingClass: 'col-sm-8',
    value: [],
    createRequestBody: null,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT
};

export default EndpointSelectField;
