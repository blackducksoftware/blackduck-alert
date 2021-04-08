import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';
import LabeledField, { LabelFieldPropertyDefaults } from 'field/LabeledField';

function noOptionsMessage() {
    return null;
}

const SelectInput = ({
    onChange,
    id,
    inputClass,
    options,
    isSearchable,
    placeholder,
    value,
    removeSelected,
    hasMultipleValues,
    components,
    selectSpacingClass,
    readOnly,
    clearable,
    labelClass,
    description,
    showDescriptionPlaceHolder,
    label,
    errorName,
    errorValue,
    required
}) => {
    const selectClasses = `${selectSpacingClass} d-inline-flex p-2`;
    let closeOnSelect = true;
    if (hasMultipleValues) {
        closeOnSelect = false;
    }
    return (
        <LabeledField
            labelClass={labelClass}
            description={description}
            showDescriptionPlaceHolder={showDescriptionPlaceHolder}
            label={label}
            errorName={errorName}
            errorValue={errorValue}
            required={required}
        >
            <div className={selectClasses}>
                <Select
                    id={id}
                    className={inputClass}
                    onChange={onChange}
                    isSearchable={isSearchable}
                    isClearable={clearable}
                    removeSelected={removeSelected}
                    options={options}
                    placeholder={placeholder}
                    value={value}
                    isMulti={hasMultipleValues}
                    closeMenuOnSelect={closeOnSelect}
                    components={components}
                    noOptionsMessage={noOptionsMessage}
                    isDisabled={readOnly}
                />
            </div>
        </LabeledField>
    );
};

SelectInput.propTypes = {
    id: PropTypes.string,
    inputClass: PropTypes.string,
    selectSpacingClass: PropTypes.string,
    options: PropTypes.array,
    components: PropTypes.object,
    placeholder: PropTypes.string,
    isSearchable: PropTypes.bool,
    removeSelected: PropTypes.bool,
    readOnly: PropTypes.bool,
    clearable: PropTypes.bool,
    hasMultipleValues: PropTypes.bool,
    value: PropTypes.object,
    onChange: PropTypes.func.isRequired,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    description: PropTypes.string,
    showDescriptionPlaceHolder: PropTypes.bool,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    required: PropTypes.bool
};

SelectInput.defaultProps = {
    id: 'selectInputId',
    placeholder: 'Choose a value',
    options: [],
    components: {},
    inputClass: 'form-control',
    selectSpacingClass: 'col-sm-4',
    isSearchable: false,
    removeSelected: false,
    readOnly: false,
    clearable: false,
    hasMultipleValues: false,
    value: {},
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT
};

export default SelectInput;
