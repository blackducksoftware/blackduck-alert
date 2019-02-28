import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Tooltip from 'react-bootstrap/Tooltip';
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';

class LabeledField extends Component {
    render(inputDiv) {
        const field = inputDiv || this.props.field;
        return (
            <div className="form-group">
                <label className="col-sm-3 col-form-label text-right">{this.props.label}</label>
                {this.props.description &&
                <div className="d-inline-flex">
                    <OverlayTrigger
                        key='top'
                        placement='top'
                        delay={{ show: 200, hide: 100 }}
                        overlay={
                            <Tooltip id='description-tooltip'>
                                {this.props.description}
                            </Tooltip>
                        }
                    >
                        <span className="fa fa-question-circle" />
                    </OverlayTrigger>
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
