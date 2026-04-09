import React from 'react';
import PropTypes from 'prop-types';
import Select, { components } from 'react-select';
import Creatable from 'react-select/creatable';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';
import DescriptorOption from 'common/component/descriptor/DescriptorOption';
import { createUseStyles } from 'react-jss';
import classNames from 'classnames';
import theme from '_theme';

const { Option, SingleValue, MultiValue } = components;

const useStyles = createUseStyles({
    input: {
        width: ({ width }) => width
    },
    errorInput: {
        outline: `1px solid ${theme.colors.red.default}`
    }
});

const DynamicSelectInput = ({
    onChange,
    id,
    name,
    width = '100%',
    options,
    searchable,
    placeholder,
    value,
    removeSelected,
    multiSelect,
    readOnly,
    clearable,
    onFocus,
    fieldDescription,
    tooltipDescription,
    label,
    errorName,
    errorValue,
    required,
    creatable,
    maxMenuHeight,
    customVal,
    customSelect
}) => {
    const classes = useStyles({ width });
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
        let parsedArray = null;
        if (Array.isArray(option) && option.length > 0) {
            parsedArray = option.map((mappedOption) => mappedOption.value);
        } else {
            parsedArray = (singleSelectOptionValue) ? [singleSelectOptionValue] : [];
        }

        onChange({
            target: {
                name,
                value: parsedArray
            }
        });
    };

    const typeOptionLabel = ({ data, ...rest }) => (
        // eslint-disable-next-line react/jsx-props-no-spreading
        <Option {...rest}>
            <DescriptorOption label={data.label} value={data.value} />
        </Option>
    );

    const typeLabel = ({ data, ...rest }) => (
        // eslint-disable-next-line react/jsx-props-no-spreading
        <SingleValue {...rest}>
            <DescriptorOption label={data.label} value={data.value} />
        </SingleValue>
    );

    const multiTypeLabel = ({ data, ...rest }) => (
        // eslint-disable-next-line react/jsx-props-no-spreading
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
        menu: (base) => ({
            ...base,
            zIndex: '101'
        }),
        control: (base, state) => ({
            ...base,
            border: `1px solid ${theme.colors.grey.lightGrey}`,
            backgroundColor: 'white',
            borderRadius: '8px',
            fontSize: '14px',
            outline: state.isFocused ? `2px solid ${theme.colors.defaultBorderColor}` : 'none',
            '&:hover:not(:focus)': {
                border: `1px solid ${theme.colors.defaultBorderColor}`
            }
        }),
        multiValue: (base) => ({
            ...base,
            borderRadius: '4px'
        })
    };
    const inputClass = classNames(classes.input, {
        [classes.errorInput]: errorValue?.severity === 'ERROR'
    });

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
            value={customVal || selectedOptions}
            isMulti={multiSelect}
            closeMenuOnSelect={!multiSelect}
            components={selectInputComponents}
            isDisabled={readOnly}
            noOptionsMessage={() => 'No options available'}
            onFocus={onFocus}
            menuPlacement="auto"
            maxMenuHeight={maxMenuHeight || 250}
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
            id={id}
            tooltipDescription={tooltipDescription}
            fieldDescription={fieldDescription}
            errorName={errorName}
            errorValue={errorValue}
            label={label}
            required={required}
        >
            {customSelect || selectComponent}
        </LabeledField>
    );
};

DynamicSelectInput.propTypes = {
    id: PropTypes.string,
    name: PropTypes.string,
    options: PropTypes.array,
    value: PropTypes.oneOfType([PropTypes.string, PropTypes.array]),
    placeholder: PropTypes.string,
    searchable: PropTypes.bool,
    removeSelected: PropTypes.bool,
    readOnly: PropTypes.bool,
    multiSelect: PropTypes.bool,
    clearable: PropTypes.bool,
    onChange: PropTypes.func.isRequired,
    onFocus: PropTypes.func,
    label: PropTypes.string.isRequired,
    fieldDescription: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    required: PropTypes.bool,
    creatable: PropTypes.bool,
    maxMenuHeight: PropTypes.number,
    tooltipDescription: PropTypes.string,
    customVal: PropTypes.oneOfType([PropTypes.array, PropTypes.object]),
    customSelect: PropTypes.element,
    width: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
};

DynamicSelectInput.defaultProps = {
    id: 'dynamicSelectInputId',
    name: 'dynamicSelectInputId',
    value: [],
    placeholder: 'Choose a value',
    options: [],
    searchable: false,
    removeSelected: false,
    readOnly: false,
    multiSelect: false,
    clearable: true,
    onFocus: () => null,
    tooltipDescription: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    creatable: false,
    customSelect: null
};

export default DynamicSelectInput;
