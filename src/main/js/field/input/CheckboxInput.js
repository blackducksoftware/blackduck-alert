import React from 'react';
import PropTypes from 'prop-types';
import Tooltip from 'react-bootstrap/Tooltip';
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';

const CheckboxInput = (props) => {
    const { errorName, errorValue } = props;
    const {
        name, label, onChange, readOnly, id, isChecked, labelSpacingClass, className, description
    } = props;
    const labelClasses = `${labelSpacingClass} text-right ${className}`;

    return (
        <div className="form-group">
            <label className={labelClasses}> {label} </label>
            {description &&
            <div className="d-inline-flex">
                <OverlayTrigger
                    key="top"
                    placement="top"
                    delay={{ show: 200, hide: 100 }}
                    overlay={
                        <Tooltip id="description-tooltip">
                            {description}
                        </Tooltip>
                    }
                >
                    <span className="fa fa-question-circle" />
                </OverlayTrigger>
            </div>
            }
            <div className="d-inline-flex p-2 checkbox">
                <input
                    id={id}
                    type="checkbox"
                    readOnly={readOnly}
                    disabled={readOnly}
                    name={name}
                    checked={isChecked}
                    value={label}
                    onChange={onChange}
                />
            </div>
            {
                errorName && errorValue &&
                <div className="offset-sm-3 col-sm-8">
                    <p className="fieldError" name={errorName}>{errorValue}</p>;
                </div>
            }
        </div>
    );
};

CheckboxInput.propTypes = {
    id: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.string,
    name: PropTypes.string.isRequired,
    isChecked: PropTypes.bool,
    label: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    readOnly: PropTypes.bool,
    className: PropTypes.string,
    labelSpacingClass: PropTypes.string,
    description: PropTypes.string
};

CheckboxInput.defaultProps = {
    id: 'id',
    errorName: '',
    errorValue: '',
    className: '',
    labelSpacingClass: 'col-sm-3 col-form-label',
    readOnly: false,
    isChecked: false,
    description: null
};

export default CheckboxInput;
