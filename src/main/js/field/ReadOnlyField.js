import React, { Component } from 'react';
import PropTypes from 'prop-types';
import LabeledField from 'field/LabeledField';


class ReadOnlyField extends Component {
    render() {
        const field = (<div className="d-inline-flex p-2 col-sm-8"><p className="form-control-static">{this.props.value}</p></div>);
        return (
            <LabeledField field={field} {...this.props} />
        );
    }
}

ReadOnlyField.propTypes = {
    value: PropTypes.string
};

ReadOnlyField.defaultProps = {
    value: ''
};

export default ReadOnlyField;
