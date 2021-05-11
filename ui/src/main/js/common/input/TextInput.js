import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/input/field/LabeledField';

const TextInput = ({
    id, autoFocus, description, errorName, errorValue, inputClass, label, labelClass, name, onChange, optionList, readOnly, required, showDescriptionPlaceHolder, value
}) => {
    const listId = optionList ? 'listOptions' : null;
    const dataListOptionObjects = !optionList ? null : optionList.map((currentOption) => (<option key={`${currentOption}Key`} value={currentOption} />));

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
                {optionList
                && (
                    <datalist id={listId}>
                        {dataListOptionObjects}
                    </datalist>
                )}
            </div>
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
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    required: PropTypes.bool,
    showDescriptionPlaceHolder: PropTypes.bool

};

TextInput.defaultProps = {
    id: 'textInputId',
    autoFocus: false,
    inputClass: 'form-control',
    name: 'name',
    onChange: () => true,
    optionList: null,
    readOnly: false,
    value: '',
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT
};

export default TextInput;
