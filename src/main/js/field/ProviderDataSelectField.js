import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Select from "react-select";
import LabeledField from "./LabeledField";

function noOptionsMessage() {
    return 'Please select a provider';
}

function retrieveOptions(endpoint) {
    // TODO get the provider name dynamically
    const resolvedEndpoint = endpoint.replace('{provider}', 'provider_blackduck');
}

class ProviderDataSelectField extends Component {
    render() {
        const {
            onChange, id, inputClass, providerDataEndpoint, isSearchable, placeholder, value, removeSelected, hasMultipleValues, components, selectSpacingClass, readOnly
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
                options={retrieveOptions(providerDataEndpoint)}
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

ProviderDataSelectField.propTypes = {
    id: PropTypes.string,
    inputClass: PropTypes.string,
    selectSpacingClass: PropTypes.string,
    components: PropTypes.object,
    placeholder: PropTypes.string,
    providerDataEndpoint: PropTypes.string,
    isSearchable: PropTypes.bool,
    removeSelected: PropTypes.bool,
    hasMultipleValues: PropTypes.bool,
    onChange: PropTypes.func.isRequired
};

ProviderDataSelectField.defaultProps = {
    id: 'id',
    placeholder: 'Choose a value',
    options: [],
    components: {},
    inputClass: 'form-control',
    selectSpacingClass: 'col-sm-4',
    isSearchable: false,
    removeSelected: false,
    hasMultipleValues: false
};

export default ProviderDataSelectField;
