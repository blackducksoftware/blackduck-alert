import React from 'react';
import PropTypes from 'prop-types';
import LabeledField from '../LabeledField';

export default class TextArea extends LabeledField {
    render() {
        const {
            inputClass, readOnly, name, value, onChange, id
        } = this.props;
        if (readOnly) {
            return super.render(
                <div className="col-sm-8">
                    <textarea id={id} rows="8" cols="60" readOnly className={inputClass} name={name} value={value} />
                </div>
            );
        }
        return super.render(
            <div className="col-sm-8"> 
                <textarea id={id} rows="8" cols="60" className={inputClass} name={name} value={value} onChange={onChange} />
            </div>);
    }
}

TextArea.propTypes = {
    id: PropTypes.string,
    inputClass: PropTypes.string,
    name: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    readOnly: PropTypes.bool,
    value: PropTypes.string
};

TextArea.defaultProps = {
    id: null,
    readOnly: false,
    inputClass: 'textInput',
    value: null
};
