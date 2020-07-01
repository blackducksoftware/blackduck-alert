import React, { Component } from 'react';
import PropTypes from 'prop-types';
import LabeledField from 'field/LabeledField';


class CheckboxInput extends Component {
    render() {
        const {
            name, onChange, readOnly, id, isChecked
        } = this.props;

        const field = (<div className="d-inline-flex p-2 checkbox">
            <input
                id={id}
                type="checkbox"
                readOnly={readOnly}
                disabled={readOnly}
                name={name}
                checked={isChecked}
                onChange={onChange}
            />
        </div>);
        return (
            <LabeledField field={field} {...this.props} />
        );
    }
}

CheckboxInput.propTypes = {
    id: PropTypes.string,
    readOnly: PropTypes.bool,
    name: PropTypes.string,
    isChecked: PropTypes.bool,
    onChange: PropTypes.func
};

CheckboxInput.defaultProps = {
    id: 'checkboxInputId',
    isChecked: false,
    readOnly: false,
    name: 'name',
    onChange: () => true
};


export default CheckboxInput;
