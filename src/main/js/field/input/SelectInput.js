import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';
import LabeledField from 'field/LabeledField';

function noOptionsMessage() {
    return null;
}

class SelectInput extends Component {
    render() {
        const {
            onChange, id, inputClass, options, isSearchable, placeholder, value, removeSelected, hasMultipleValues, components, selectSpacingClass
        } = this.props;
        const selectClasses = `${selectSpacingClass} d-inline-flex p-2`;
        let closeOnSelect = true;
        if (hasMultipleValues) {
            closeOnSelect = false;
        }

        const field = (<div className={selectClasses}>
            <Select
                id={id}
                className={inputClass}
                onChange={onChange}
                isSearchable={isSearchable}
                removeSelected={removeSelected}
                options={options}
                placeholder={placeholder}
                value={value}
                isMulti={hasMultipleValues}
                closeMenuOnSelect={closeOnSelect}
                components={components}
                noOptionsMessage={noOptionsMessage}
            />
        </div>);
        return (
            <LabeledField field={field} {...this.props} />
        );
    }
}

SelectInput.propTypes = {
    id: PropTypes.string,
    inputClass: PropTypes.string,
    selectSpacingClass: PropTypes.string,
    options: PropTypes.array,
    components: PropTypes.object,
    value: PropTypes.string,
    placeholder: PropTypes.string,
    isSearchable: PropTypes.bool,
    removeSelected: PropTypes.bool,
    hasMultipleValues: PropTypes.bool,
    onChange: PropTypes.func.isRequired
};

SelectInput.defaultProps = {
    id: 'id',
    value: undefined,
    placeholder: 'Choose a value',
    options: [],
    components: {},
    inputClass: 'form-control',
    selectSpacingClass: 'col-sm-4',
    isSearchable: false,
    removeSelected: false,
    hasMultipleValues: false
};


export default SelectInput;
