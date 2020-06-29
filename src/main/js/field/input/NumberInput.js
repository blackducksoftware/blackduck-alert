import React, { Component } from 'react';
import PropTypes from 'prop-types';
import LabeledField from 'field/LabeledField';

class NumberInput extends Component {
    render() {
        const { readOnly, inputClass, id, name, value, onChange } = this.props;

        let field = null;
        if (readOnly) {
            field = (<div className="d-inline-flex flex-column p-2 col-sm-3"><input id={id} type="number" readOnly className={inputClass} name={name} value={value} /></div>);
        } else {
            field = (<div className="d-inline-flex flex-column p-2 col-sm-3"><input id={id} type="number" className={inputClass} name={name} value={value} onChange={onChange} /></div>);
        }
        return (
            <LabeledField field={field} {...this.props} />
        );
    }
}


NumberInput.propTypes = {
    id: PropTypes.string,
    readOnly: PropTypes.bool,
    inputClass: PropTypes.string,
    name: PropTypes.string,
    value: PropTypes.string,
    onChange: PropTypes.func
};

NumberInput.defaultProps = {
    id: 'numberInputId',
    value: '',
    readOnly: false,
    inputClass: 'form-control',
    name: 'name',
    onChange: () => true
};


export default NumberInput;
