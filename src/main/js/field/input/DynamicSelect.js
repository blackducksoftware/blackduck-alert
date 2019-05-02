import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';
import LabeledField from 'field/LabeledField';

function DynamicSelectInput(props) {
    const {
        onChange, id, inputClass, options, searchable, placeholder, value, removeSelected, multiSelect, components, selectSpacingClass
    } = props;

    const selectClasses = `${selectSpacingClass} d-inline-flex p-2`;

    const handleChange = (option) => {
        console.log(option);
        const optionValue = option ? option.value : null;
        const parsedArray = (Array.isArray(option) && option.length > 0) ? option.map(mappedOption => mappedOption.value) : optionValue;
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
            removeSelected={removeSelected}
            options={options}
            placeholder={placeholder}
            value={value}
            isMulti={multiSelect}
            closeMenuOnSelect={!multiSelect}
            components={components}
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
    multiSelect: PropTypes.bool,
    onChange: PropTypes.func.isRequired
};

DynamicSelectInput.defaultProps = {
    id: 'id',
    value: undefined,
    placeholder: 'Choose a value',
    options: [],
    components: {},
    inputClass: 'typeAheadField',
    labelClass: 'col-sm-3',
    selectSpacingClass: 'col-sm-8',
    searchable: false,
    removeSelected: false,
    multiSelect: false
};


export default DynamicSelectInput;