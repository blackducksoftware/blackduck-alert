import React from 'react';
import PropTypes from 'prop-types';
import Tooltip from 'react-bootstrap/Tooltip';
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';

const CheckboxInput = (props) => {
    const { errorName, errorValue } = props;
    const {
        name, label, onChange, readOnly, id, isChecked, className, description
    } = props;
    return (
        <div className="form-group">
            <div className="offset-sm-3 col-sm-8">
                <div className="form-check">
                    <label className={className}>
                        <input
                            id={id}
                            type="checkbox"
                            className="form-check-input"
                            readOnly={readOnly}
                            disabled={readOnly}
                            name={name}
                            checked={isChecked}
                            value={label}
                            onChange={onChange}
                        />
                        {description &&
                        <div className="d-inline-flex checkboxDescription">
                            <OverlayTrigger
                                key='top'
                                placement='top'
                                delay={{ show: 200, hide: 100 }}
                                overlay={
                                    <Tooltip id='description-tooltip'>
                                        {description}
                                    </Tooltip>
                                }
                            >
                                <span className="fa fa-question-circle" />
                            </OverlayTrigger>
                        </div>
                        }
                        {label}
                    </label>
                </div>
            </div>
            {errorName && errorValue &&
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
    description: PropTypes.string,
};

CheckboxInput.defaultProps = {
    id: 'id',
    errorName: '',
    errorValue: '',
    readOnly: false,
    isChecked: false,
    description: null
};

export default CheckboxInput;
