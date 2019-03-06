import React, { Component } from 'react';
import PropTypes from 'prop-types';
import LabeledField from 'field/LabeledField';

class TextArea extends Component {
    render() {
        const {
            inputClass, sizeClass, readOnly, name, value, onChange, id
        } = this.props;

        let field = null;
        if (readOnly) {
            field = (<div className={sizeClass}>
                <textarea id={id} rows="8" cols="60" readOnly className={inputClass} name={name} value={value} />
            </div>);
        } else {
            field = (<div className={sizeClass}>
                <textarea id={id} rows="8" cols="60" className={inputClass} name={name} value={value} onChange={onChange} />
            </div>);
        }

        return (
            <LabeledField field={field} {...this.props} />
        );
    }
}

TextArea.propTypes = {
    id: PropTypes.string,
    readOnly: PropTypes.bool,
    inputClass: PropTypes.string,
    sizeClass: PropTypes.string,
    name: PropTypes.string,
    value: PropTypes.string,
    onChange: PropTypes.func.isRequired
};

TextArea.defaultProps = {
    id: 'id',
    value: '',
    readOnly: false,
    inputClass: 'textInput',
    sizeClass: 'col-sm-8',
    name: 'name'
};


export default TextArea;
