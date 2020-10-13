import React, { Component } from 'react';
import PropTypes from 'prop-types';
import LabeledField from 'field/LabeledField';

class TextInput extends Component {
    render() {
        const {
            isSet, inputClass, id, readOnly, autoFocus, name, value, onChange
        } = this.props;
        const placeholderText = (isSet) ? '********' : null;

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
                    placeholder={placeholderText}
                />
            </div>
        );

        return (
            <LabeledField field={field} {...this.props} />
        );
    }
}

TextInput.propTypes = {
    id: PropTypes.string,
    isSet: PropTypes.bool,
    readOnly: PropTypes.bool,
    autoFocus: PropTypes.bool,
    inputClass: PropTypes.string,
    name: PropTypes.string,
    value: PropTypes.string,
    onChange: PropTypes.func
};

TextInput.defaultProps = {
    id: 'textInputId',
    isSet: false,
    value: '',
    readOnly: false,
    autoFocus: false,
    inputClass: 'form-control',
    name: 'name',
    onChange: () => true
};

export default TextInput;
