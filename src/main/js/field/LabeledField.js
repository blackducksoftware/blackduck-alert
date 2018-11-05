import React, {Component} from 'react';
import PropTypes from 'prop-types';

class LabeledField extends Component {
    render(inputDiv) {
        const field = inputDiv || this.props.field;
        return (
            <div className="form-group">
                <label className="col-sm-3 col-form-label text-right">{this.props.label}</label>
                {field}
                {this.props.errorMessage &&
                <div className="offset-sm-3 col-sm-8">
                    <p className="fieldError">{this.props.errorMessage}</p>
                </div>
                }
            </div>
        );
    }
}

LabeledField.propTypes = {
    field: PropTypes.node,
    label: PropTypes.string.isRequired,
    errorMessage: PropTypes.string
};

LabeledField.defaultProps = {
    field: null,
    errorMessage: null
};

export default LabeledField;
