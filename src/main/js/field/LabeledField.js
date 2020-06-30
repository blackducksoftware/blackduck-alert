import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Tooltip from 'react-bootstrap/Tooltip';
import Overlay from 'react-bootstrap/Overlay';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

class LabeledField extends Component {
    constructor(props) {
        super(props);

        this.state = {
            showDescription: false
        };
    }

    render() {
        const { showDescription } = this.state;
        const {
            field, labelClass, description, showDescriptionPlaceHolder, label, errorName, errorValue, required
        } = this.props;

        const baseClasses = `${labelClass} text-right`;
        const labelClasses = (required) ? `${baseClasses} required` : baseClasses;

        let descriptionField = null;
        if (description) {
            descriptionField = (<div className="d-inline-flex">
                <span
                    className="descriptionIcon"
                    onClick={() => this.setState({ showDescription: !showDescription })}
                    ref={(icon) => {
                        this.target = icon;
                    }}
                >
                    <FontAwesomeIcon icon="question-circle" className="alert-icon" size="lg" />
                </span>
                <Overlay
                    rootClose
                    placement="top"
                    show={showDescription}
                    onHide={() => this.setState({ showDescription: false })}
                    target={() => this.target}
                    container={this}
                >
                    <Tooltip id="description-tooltip">
                        {description}
                    </Tooltip>
                </Overlay>
            </div>);
        } else if (showDescriptionPlaceHolder) {
            descriptionField = (<div className="descriptionPlaceHolder" />);
        }

        return (
            <div key={label} className="form-group">
                <label className={labelClasses}>{label}</label>
                {descriptionField}
                {field}
                {errorName && errorValue &&
                <div className="offset-sm-3 col-sm-8">
                    <p className="fieldError" name={errorName}>{errorValue}</p>
                </div>
                }
            </div>
        );
    }
}

LabeledField.propTypes = {
    id: PropTypes.string,
    field: PropTypes.node,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    description: PropTypes.string,
    showDescriptionPlaceHolder: PropTypes.bool,
    errorName: PropTypes.string,
    errorValue: PropTypes.string,
    required: PropTypes.bool
};

LabeledField.defaultProps = {
    id: 'labeledFieldId',
    field: null,
    labelClass: 'col-sm-3 col-form-label',
    errorName: null,
    errorValue: null,
    description: null,
    showDescriptionPlaceHolder: true,
    required: false
};

export default LabeledField;
