import React from 'react';
import PropTypes from 'prop-types';
import Select, { components } from 'react-select';
import LabeledField, { LabelFieldPropertyDefaults } from 'field/LabeledField';
import DescriptorOption from 'component/common/DescriptorOption';

const { Option, SingleValue, MultiValue } = components;

const DynamicSelectInput = ({
    onChange,
    id,
    inputClass,
    options,
    searchable,
    placeholder,
    value,
    removeSelected,
    multiSelect,
    selectSpacingClass,
    readOnly,
    clearable,
    onFocus,
    labelClass,
    description,
    showDescriptionPlaceHolder,
    label,
    errorName,
    errorValue,
    required
}) => {
    const selectClasses = `${selectSpacingClass} d-inline-flex p-2`;

    const handleChange = (option) => {
        const optionValue = option ? option.value : null;
        const parsedArray = (Array.isArray(option) && option.length > 0) ? option.map((mappedOption) => mappedOption.value) : [optionValue];

        onChange({
            target: {
                name: id,
                value: parsedArray
            }
        });
    };

    const typeOptionLabel = ({ data, ...rest }) => (
        <Option {...rest}>
            <DescriptorOption label={data.label} value={data.value} />
        </Option>
    );

    const typeLabel = ({ data, ...rest }) => (
        <SingleValue {...rest}>
            <DescriptorOption label={data.label} value={data.value} />
        </SingleValue>
    );

    const multiTypeLabel = ({ data, ...rest }) => (
        <MultiValue {...rest}>
            <DescriptorOption label={data.label} value={data.value} />
        </MultiValue>
    );

    const selectInputComponents = {
        Option: typeOptionLabel,
        SingleValue: typeLabel,
        MultiValue: multiTypeLabel
    };

    const selectValue = options.filter((option) => value.includes(option.value));
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
                    onChange={handleChange}
                    isSearchable={searchable}
                    isClearable={clearable}
                    removeSelected={removeSelected}
                    options={options}
                    placeholder={placeholder}
                    value={selectValue}
                    isMulti={multiSelect}
                    closeMenuOnSelect={!multiSelect}
                    components={selectInputComponents}
                    isDisabled={readOnly}
                    noOptionsMessage={() => 'No options available'}
                    onFocus={onFocus}
                />
            </div>
        </LabeledField>
    );
};

DynamicSelectInput.propTypes = {
    id: PropTypes.string,
    inputClass: PropTypes.string,
    selectSpacingClass: PropTypes.string,
    options: PropTypes.array,
    value: PropTypes.array,
    placeholder: PropTypes.string,
    searchable: PropTypes.bool,
    removeSelected: PropTypes.bool,
    readOnly: PropTypes.bool,
    multiSelect: PropTypes.bool,
    clearable: PropTypes.bool,
    onChange: PropTypes.func.isRequired,
    onFocus: PropTypes.func,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    description: PropTypes.string,
    showDescriptionPlaceHolder: PropTypes.bool,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    required: PropTypes.bool
};

DynamicSelectInput.defaultProps = {
    id: 'dynamicSelectInputId',
    value: [],
    placeholder: 'Choose a value',
    options: [],
    inputClass: 'typeAheadField',
    labelClass: 'col-sm-3',
    selectSpacingClass: 'col-sm-8',
    searchable: false,
    removeSelected: false,
    readOnly: false,
    multiSelect: false,
    clearable: true,
    onFocus: () => null,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT
};

export default DynamicSelectInput;
