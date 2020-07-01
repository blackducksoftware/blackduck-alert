import React from 'react';
import PropTypes from 'prop-types';
import Select, { components } from 'react-select';
import LabeledField from 'field/LabeledField';
import DescriptorOption from 'component/common/DescriptorOption';

const { Option, SingleValue, MultiValue } = components;

function DynamicSelectInput(props) {
    const {
        onChange, id, inputClass, options, searchable, placeholder, value, removeSelected, multiSelect, selectSpacingClass, readOnly, clearable, onFocus
    } = props;

    const selectClasses = `${selectSpacingClass} d-inline-flex p-2`;

    const handleChange = (option) => {
        const optionValue = option ? option.value : null;
        const parsedArray = (Array.isArray(option) && option.length > 0) ? option.map(mappedOption => mappedOption.value) : [optionValue];

        onChange({
            target: {
                name: id,
                value: parsedArray
            }
        });
    };

    const typeOptionLabel = props => (
        <Option {...props}>
            <DescriptorOption label={props.data.label} value={props.data.value} />
        </Option>
    );

    const typeLabel = props => (
        <SingleValue {...props}>
            <DescriptorOption label={props.data.label} value={props.data.value} />
        </SingleValue>
    );

    const multiTypeLabel = props => (
        <MultiValue {...props}>
            <DescriptorOption label={props.data.label} value={props.data.value} />
        </MultiValue>
    );

    const components = {
        Option: typeOptionLabel,
        SingleValue: typeLabel,
        MultiValue: multiTypeLabel
    }

    const selectValue = options.filter(option => value.includes(option.value));

    const field = (<div className={selectClasses}>
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
            components={components}
            isDisabled={readOnly}
            noOptionsMessage={() => 'No options available'}
            onFocus={onFocus}
        />
    </div>);
    return (
        <LabeledField field={field} {...props} />
    );
}

DynamicSelectInput.propTypes = {
    id: PropTypes.string,
    inputClass: PropTypes.string,
    labelClass: PropTypes.string,
    selectSpacingClass: PropTypes.string,
    options: PropTypes.array,
    components: PropTypes.object,
    value: PropTypes.array,
    placeholder: PropTypes.string,
    searchable: PropTypes.bool,
    removeSelected: PropTypes.bool,
    readOnly: PropTypes.bool,
    multiSelect: PropTypes.bool,
    clearable: PropTypes.bool,
    onChange: PropTypes.func.isRequired,
    onFocus: PropTypes.func
};

DynamicSelectInput.defaultProps = {
    id: 'dynamicSelectInputId',
    value: [],
    placeholder: 'Choose a value',
    options: [],
    components: {},
    inputClass: 'typeAheadField',
    labelClass: 'col-sm-3',
    selectSpacingClass: 'col-sm-8',
    searchable: false,
    removeSelected: false,
    readOnly: false,
    multiSelect: false,
    clearable: true,
    onFocus: () => null
};


export default DynamicSelectInput;
