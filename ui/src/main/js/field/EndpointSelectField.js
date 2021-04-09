import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { createNewConfigurationRequest } from 'util/configurationRequestBuilder';
import DynamicSelectInput from 'field/input/DynamicSelectInput';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import { LabelFieldPropertyDefaults } from './LabeledField';

const EndpointSelectField = (props) => {
    const {
        currentConfig, fieldKey, csrfToken, endpoint, requiredRelatedFields, value, errorValue, onChange,
        id,
        inputClass,
        searchable,
        placeholder,
        removeSelected,
        multiSelect,
        selectSpacingClass,
        readOnly,
        clearable,
        labelClass,
        description,
        showDescriptionPlaceHolder,
        label,
        errorName,
        required
    } = props;
    const [options, setOptions] = useState([]);

    const emptyFieldValue = () => {
        const eventObject = {
            target: {
                name: fieldKey,
                value: []
            }
        };

        onChange(eventObject);
    };

    const onSendClick = () => {
        const newFieldModel = FieldModelUtilities.createFieldModelFromRequestedFields(currentConfig, requiredRelatedFields);
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
                    const selectedValues = selectOptions.filter((option) => value.includes(option.value));
                    if (selectOptions.length === 0 || selectedValues.length === 0) {
                        emptyFieldValue();
                    }

                    setOptions(selectOptions);
                });
            } else {
                response.json()
                    .then(() => {
                        setOptions([]);
                        // setFieldError({
                        //     severity: 'ERROR',
                        //     fieldMessage: data.message
                        // });
                    })
                    .then(() => emptyFieldValue());
            }
        });
    };

    useEffect(() => {
        onSendClick();
    }, []);

    useEffect(() => {
        onSendClick();
    }, [currentConfig]);

    return (
        <div>
            <DynamicSelectInput
                onChange={onChange}
                onFocus={onSendClick}
                options={options}
                id={id}
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
                errorValue={errorValue}
                required={required}
            />
        </div>
    );
};

EndpointSelectField.propTypes = {
    id: PropTypes.string,
    currentConfig: PropTypes.object,
    csrfToken: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    endpoint: PropTypes.string.isRequired,
    fieldKey: PropTypes.string.isRequired,
    requiredRelatedFields: PropTypes.array,
    inputClass: PropTypes.string,
    selectSpacingClass: PropTypes.string,
    value: PropTypes.array,
    placeholder: PropTypes.string,
    searchable: PropTypes.bool,
    removeSelected: PropTypes.bool,
    readOnly: PropTypes.bool,
    multiSelect: PropTypes.bool,
    clearable: PropTypes.bool,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    description: PropTypes.string,
    showDescriptionPlaceHolder: PropTypes.bool,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    required: PropTypes.bool
};

EndpointSelectField.defaultProps = {
    id: 'endpointSelectFieldId',
    currentConfig: {},
    requiredRelatedFields: [],
    value: [],
    placeholder: 'Choose a value',
    inputClass: 'typeAheadField',
    labelClass: 'col-sm-3',
    selectSpacingClass: 'col-sm-8',
    searchable: false,
    removeSelected: false,
    readOnly: false,
    multiSelect: false,
    clearable: true,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT
};

export default EndpointSelectField;
