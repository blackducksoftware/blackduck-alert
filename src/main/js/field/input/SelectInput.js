import React from 'react';
import Select from 'react-select';
import LabeledField from 'field/LabeledField';

export default class SelectInput extends LabeledField {
    render() {
        const {
            onChange, id, className, options, isSearchable, placeholder, value, removeSelected, hasMultipleValues, components, selectSpacingClass
        } = this.props;

        const selectSpacing = selectSpacingClass || 'col-sm-4';

        const selectClasses = `${selectSpacing} d-inline-flex p-2`;

        const selectId = id || 'id';
        const selectOptions = options || [];
        const searchable = isSearchable || false;
        const clearSelected = removeSelected || false;
        const mulipleValues = hasMultipleValues || false;
        const placeHolderText = placeholder || 'Choose a value';
        const selectComponents = components || {};

        return (super.render(<div className={selectClasses}>
            <Select
                id={selectId}
                className={className}
                onChange={onChange}
                isSearchable={searchable}
                removeSelected={clearSelected}
                options={selectOptions}
                placeholder={placeHolderText}
                value={value}
                isMulti={mulipleValues}
                components={selectComponents}
            />
        </div>));
    }
}
