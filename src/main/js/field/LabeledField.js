import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Tooltip from 'react-bootstrap/Tooltip';
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';

class LabeledField extends Component {
    render(inputDiv) {
        const field = inputDiv || this.props.field;

        const { labelClass } = this.props;
        const labelClasses = `${labelClass} text-right`;

        let description = null;
        if (this.props.description) {
            description = (<div className="d-inline-flex">
                <OverlayTrigger
                    key="top"
                    placement="top"
                    delay={{ show: 200, hide: 100 }}
                    overlay={
                        <Tooltip id="description-tooltip">
                            {this.props.description}
                        </Tooltip>
                    }
                >
                    <span className="fa fa-question-circle" />
                </OverlayTrigger>
            </div>);
        } else if (this.props.showDescriptionPlaceHolder) {
            description = (<div className="descriptionPlaceHolder" />);
        }

        return (
            <div className="form-group">
                <label className={labelClasses}>{this.props.label}</label>
                {description}
                {field}
                {this.props.errorName && this.props.errorValue &&
                <div className="offset-sm-3 col-sm-8">
                    <p className="fieldError" name={this.props.errorName}>{this.props.errorValue}</p>
                </div>
                }
            </div>
        );
    }
}

LabeledField.propTypes = {
    field: PropTypes.node,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    description: PropTypes.string,
    showDescriptionPlaceHolder: PropTypes.bool,
    errorName: PropTypes.string,
    errorValue: PropTypes.string
};

LabeledField.defaultProps = {
    field: null,
    labelClass: 'col-sm-3 col-form-label',
    errorName: null,
    errorValue: null,
    description: null,
    showDescriptionPlaceHolder: true
};

export default LabeledField;
