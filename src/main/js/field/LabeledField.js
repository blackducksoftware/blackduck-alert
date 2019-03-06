import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Tooltip from 'react-bootstrap/Tooltip';
import Overlay from 'react-bootstrap/Overlay';

class LabeledField extends Component {
    constructor(props) {
        super(props);

        this.state = {
            showDescription: false
        };
    }

    render() {
        const { showDescription } = this.state;
        const { field, labelClass, description, showDescriptionPlaceHolder, label, errorName, errorValue } = this.props;

        const labelClasses = `${labelClass} text-right`;

        let descriptionField = null;
        if (description) {
            descriptionField = (<div className="d-inline-flex">
                <span
                    className="fa fa-question-circle descriptionIcon"
                    onClick={() => this.setState({ showDescription: !showDescription })}
                    ref={(icon) => {
                        this.target = icon;
                    }}
                />
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
            <div className="form-group">
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
