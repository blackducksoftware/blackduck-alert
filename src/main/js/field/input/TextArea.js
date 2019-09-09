import React, { Component } from 'react';
import PropTypes from 'prop-types';
import LabeledField from 'field/LabeledField';

class TextArea extends Component {
    render() {
        const {
            inputClass, sizeClass, readOnly, name, value, onChange, id
        } = this.props;

        const field = (
            <div className={`${sizeClass} d-inline-flex flex-column p-2`}>
                <textarea id={id} rows="8" cols="60" readOnly={readOnly} className={inputClass} name={name} value={value} onChange={!readOnly && onChange} />
            </div>);

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
    inputClass: 'textInput',
    sizeClass: 'col-sm-8',
    name: 'name',
    onChange: () => true
};


export default TextArea;
