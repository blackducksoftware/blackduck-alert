import React from 'react';
import PropTypes from 'prop-types';
import LabeledField from '../LabeledField';

export default class TextArea extends LabeledField {
    render() {
        const {
            inputClass, readOnly, name, value, onChange
        } = this.props;
        if (readOnly) {
            return super.render(<textarea rows="8" cols="60" readOnly className={inputClass} name={name} value={value} />);
        }
        return super.render(<textarea rows="8" cols="60" className={inputClass} name={name} value={value} onChange={onChange} />);
    }
}

TextArea.propTypes = {
    inputClass: PropTypes.string,
    name: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    readOnly: PropTypes.bool,
    value: PropTypes.string
};

TextArea.defaultProps = {
    readOnly: false,
    inputClass: 'textInput',
    value: null
};
