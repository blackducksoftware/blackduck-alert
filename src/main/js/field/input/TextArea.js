import React from 'react';
import PropTypes from 'prop-types';
import LabeledField from '../LabeledField';

export default class TextArea extends LabeledField {
    render() {
        const {
            inputClass, sizeClass, readOnly, name, value, onChange, id
        } = this.props;
        if (readOnly) {
            return super.render(
                <div className={sizeClass}>
                    <textarea id={id} rows="8" cols="60" readOnly className={inputClass} name={name} value={value}/>
                </div>
            );
        }
        return super.render(
            <div className={sizeClass}>
                <textarea id={id} rows="8" cols="60" className={inputClass} name={name} value={value} onChange={onChange}/>
            </div>);
    }
}

TextArea.propTypes = {
    inputClass: PropTypes.string,
    name: PropTypes.string.isRequired,
    onChange: PropTypes.func,
    readOnly: PropTypes.bool,
    value: PropTypes.string,
    sizeClass: PropTypes.string
};

TextArea.defaultProps = {
    readOnly: false,
    inputClass: 'textInput',
    value: null,
    sizeClass: 'col-sm-8'
};
