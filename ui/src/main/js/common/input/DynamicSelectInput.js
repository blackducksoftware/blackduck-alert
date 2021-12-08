import React from 'react';
import PropTypes from 'prop-types';
import Select, { components } from 'react-select';
import Creatable from 'react-select/creatable';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/input/field/LabeledField';
import DescriptorOption from 'common/DescriptorOption';

const { Option, SingleValue, MultiValue } = components;

const DynamicSelectInput = ({
    onChange,
    id,
    name,
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
    required,
    creatable
}) => {
    const selectClasses = `${selectSpacingClass} d-inline-flex p-2`;
    const selectedOptions = options.filter((option) => value.includes(option.value));
    if (creatable) {
        const selectedOptionValues = selectedOptions.map((selection) => selection.value);
        const customOptions = value
            .filter((customValue) => customValue)
            .filter((customValue) => !selectedOptionValues.includes(customValue))
            .map((customValue) => ({ label: customValue, value: customValue }));
        selectedOptions.push(...customOptions);
    }

    const handleChange = (option) => {
        const singleSelectOptionValue = option ? option.value : null;
        const parsedArray = (Array.isArray(option) && option.length > 0) ? option.map((mappedOption) => mappedOption.value) : [singleSelectOptionValue];
        onChange({
            target: {
                name,
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

    // moves the dropdown in front of our fixed buttons
    const selectStyles = {
        menu: (base, state) => ({
            ...base,
            zIndex: '101'
        })
    };

    const createStandardSelect = () => (
        <Select
            id={id}
            className={inputClass}
            onChange={handleChange}
            isSearchable={searchable}
            isClearable={clearable}
            removeSelected={removeSelected}
            options={options}
            placeholder={placeholder}
            value={selectedOptions}
            isMulti={multiSelect}
            closeMenuOnSelect={!multiSelect}
            components={selectInputComponents}
            isDisabled={readOnly}
            noOptionsMessage={() => 'No options available'}
            onFocus={onFocus}
            menuPlacement="auto"
            maxMenuHeight={250}
            styles={selectStyles}
        />
    );

    const createCreatableSelect = () => (
        <Creatable
            id={id}
            className={inputClass}
            onChange={handleChange}
            isSearchable={searchable}
            isClearable={clearable}
            removeSelected={removeSelected}
            options={options}
            placeholder={placeholder}
            value={selectedOptions}
            isMulti={multiSelect}
            closeMenuOnSelect={!multiSelect}
            components={selectInputComponents}
            isDisabled={readOnly}
            noOptionsMessage={() => 'Create your own options'}
            onFocus={onFocus}
            menuPlacement="auto"
            maxMenuHeight={250}
            styles={selectStyles}
        />
    );

    const selectComponent = creatable ? createCreatableSelect() : createStandardSelect();

    return (
        <LabeledField
            description={description}
            errorName={errorName}
            errorValue={errorValue}
            label={label}
            labelClass={labelClass}
            required={required}
            showDescriptionPlaceHolder={showDescriptionPlaceHolder}
        >
            <div className={selectClasses}>
                {selectComponent}
            </div>
        </LabeledField>
    );
};

DynamicSelectInput.propTypes = {
    id: PropTypes.string,
    name: PropTypes.string,
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
    required: PropTypes.bool,
    creatable: PropTypes.bool
};

DynamicSelectInput.defaultProps = {
    id: 'dynamicSelectInputId',
    name: 'dynamicSelectInputId',
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
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT,
    creatable: false
};

export default DynamicSelectInput;
