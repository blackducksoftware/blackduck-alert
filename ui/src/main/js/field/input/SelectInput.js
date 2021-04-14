import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';
import LabeledField, { LabelFieldPropertyDefaults } from 'field/LabeledField';

function noOptionsMessage() {
    return null;
}

const SelectInput = ({
    id,
    clearable,
    components,
    description,
    errorName,
    errorValue,
    hasMultipleValues,
    inputClass,
    isSearchable,
    label,
    labelClass,
    onChange,
    options,
    placeholder,
    readOnly,
    removeSelected,
    required,
    selectSpacingClass,
    showDescriptionPlaceHolder,
    value

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
    clearable: PropTypes.bool,
    components: PropTypes.object,
    hasMultipleValues: PropTypes.bool,
    inputClass: PropTypes.string,
    isSearchable: PropTypes.bool,
    onChange: PropTypes.func.isRequired,
    options: PropTypes.array,
    placeholder: PropTypes.string,
    readOnly: PropTypes.bool,
    removeSelected: PropTypes.bool,
    selectSpacingClass: PropTypes.string,
    value: PropTypes.object,
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    required: PropTypes.bool,
    showDescriptionPlaceHolder: PropTypes.bool
};

SelectInput.defaultProps = {
    id: 'selectInputId',
    clearable: false,
    components: {},
    hasMultipleValues: false,
    inputClass: 'form-control',
    isSearchable: false,
    placeholder: 'Choose a value',
    options: [],
    removeSelected: false,
    readOnly: false,
    selectSpacingClass: 'col-sm-4',
    value: {},
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT
};

export default SelectInput;
