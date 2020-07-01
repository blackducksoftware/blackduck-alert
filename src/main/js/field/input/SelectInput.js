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
            onChange, id, inputClass, options, isSearchable, placeholder, value, removeSelected, hasMultipleValues, components, selectSpacingClass, readOnly, clearable
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
    placeholder: PropTypes.string,
    isSearchable: PropTypes.bool,
    removeSelected: PropTypes.bool,
    readOnly: PropTypes.bool,
    multiSelect: PropTypes.bool,
    clearable: PropTypes.bool,
    hasMultipleValues: PropTypes.bool,
    value: PropTypes.object,
    onChange: PropTypes.func.isRequired
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
    multiSelect: false,
    clearable: false,
    hasMultipleValues: false,
    value: {}
};


export default SelectInput;
