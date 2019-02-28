import React, { Component } from 'react';
import PropTypes from 'prop-types';

class LabeledField extends Component {
    render(inputDiv) {
        const field = inputDiv || this.props.field;
        return (
            <div className="form-group">
                <label className="col-sm-3 col-form-label text-right">{this.props.label}</label>
                {this.props.description &&
                <div className="d-inline-flex">
                    <span className="fa fa-question-circle" tabIndex="0" data-delay='{"show": 300, "hide": 100}' data-toggle="tooltip" title={this.props.description} />
                </div>
                }
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
    description: PropTypes.string,
    errorMessage: PropTypes.string
};

LabeledField.defaultProps = {
    field: null,
    errorMessage: null,
    description: null
};

export default LabeledField;
