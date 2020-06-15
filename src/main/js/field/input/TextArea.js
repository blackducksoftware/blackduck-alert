import React, { Component } from 'react';
import PropTypes from 'prop-types';
import LabeledField from 'field/LabeledField';

class TextArea extends Component {
    render() {
        const {
            inputClass, sizeClass, readOnly, name, value, onChange, id
        } = this.props;
        const divClasses = `${sizeClass} d-inline-flex`;

        let field;
        if (readOnly) {
            field = (<div className={divClasses}>
                <textarea id={id} rows="8" cols="60" readOnly className={inputClass} name={name} value={value} />
            </div>);
        } else {
            field = (<div className={divClasses}>
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
    onChange: PropTypes.func
};

TextArea.defaultProps = {
    id: 'id',
    value: '',
    readOnly: false,
    inputClass: 'form-control',
    sizeClass: 'col-sm-8',
    name: 'name',
    onChange: () => true
};


export default TextArea;
