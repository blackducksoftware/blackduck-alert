import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'field/LabeledField';

const TextInput = ({
    inputClass, id, readOnly, autoFocus, name, value, onChange, optionList, labelClass, description, showDescriptionPlaceHolder, label, errorName, errorValue, required
}) => {
    let listId = null;
    let dataListOptions = null;
    if (optionList) {
        listId = 'listOptions';
        const dataListOptionObjects = optionList.map((currentOption) => (<option key={`${currentOption}Key`} value={currentOption} />));
        dataListOptions = (
            <datalist id={listId}>
                {dataListOptionObjects}
            </datalist>
        );
    }

    const field = (
        <div className="d-inline-flex flex-column p-2 col-sm-8">
            <input
                id={id}
                type="text"
                readOnly={readOnly}
                autoFocus={autoFocus}
                className={inputClass}
                name={name}
                value={value}
                onChange={onChange}
                list={listId}
            />
            {dataListOptions}
        </div>
    );

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
            {field}
        </LabeledField>
    );
};

TextInput.propTypes = {
    id: PropTypes.string,
    readOnly: PropTypes.bool,
    autoFocus: PropTypes.bool,
    inputClass: PropTypes.string,
    name: PropTypes.string,
    value: PropTypes.string,
    onChange: PropTypes.func,
    optionList: PropTypes.array,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    description: PropTypes.string,
    showDescriptionPlaceHolder: PropTypes.bool,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    required: PropTypes.bool
};

TextInput.defaultProps = {
    id: 'textInputId',
    value: '',
    readOnly: false,
    autoFocus: false,
    inputClass: 'form-control',
    name: 'name',
    onChange: () => true,
    optionList: null,
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT
};

export default TextInput;
