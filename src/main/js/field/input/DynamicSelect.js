import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';
import LabeledField from 'field/LabeledField';

function noOptionsMessage() {
    return 'No options available...';
}

function DynamicSelectInput(props) {
    const {
        onChange, id, inputClass, options, searchable, placeholder, value, removeSelected, multiSelect, components, selectSpacingClass, readOnly, clearable
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
            value={value}
            isMulti={multiSelect}
            closeMenuOnSelect={!multiSelect}
            components={components}
            isDisabled={readOnly}
            noOptionsMessage={noOptionsMessage}
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
    onChange: PropTypes.func.isRequired
};

DynamicSelectInput.defaultProps = {
    id: 'id',
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
    clearable: true
};


export default DynamicSelectInput;
