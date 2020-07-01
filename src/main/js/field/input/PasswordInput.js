import React, { Component } from 'react';
import PropTypes from 'prop-types';
import LabeledField from 'field/LabeledField';

class PasswordInput extends Component {
    render() {
        const { readOnly, isSet, inputClass, id, name, value, onChange } = this.props;

        const placeholderText = (isSet) ? '***********' : null;

        let field = null;
        if (readOnly) {
            field = (<div className="d-inline-flex flex-column p-2 col-sm-8">
                <input id={id} type="password" readOnly className={inputClass} name={name} value={value} placeholder={placeholderText} />
            </div>);
        } else {
            field = (<div className="d-inline-flex flex-column p-2 col-sm-8">
                <input id={id} type="password" className={inputClass} name={name} value={value} onChange={onChange} placeholder={placeholderText} />
            </div>);
        }
        return (
            <LabeledField field={field} {...this.props} />
        );
    }
}

PasswordInput.propTypes = {
    id: PropTypes.string,
    isSet: PropTypes.bool,
    readOnly: PropTypes.bool,
    inputClass: PropTypes.string,
    name: PropTypes.string,
    value: PropTypes.string,
    onChange: PropTypes.func
};

PasswordInput.defaultProps = {
    id: 'passwordInputId',
    isSet: false,
    value: '',
    readOnly: false,
    inputClass: 'form-control',
    name: 'name',
    onChange: () => true
};


export default PasswordInput;
