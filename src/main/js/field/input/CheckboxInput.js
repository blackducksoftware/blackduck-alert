import React from 'react';
import PropTypes from 'prop-types';

const CheckboxInput = (props) => {
    const { errorName, errorValue } = props;
    const {
        name, label, onChange, readOnly, id, isChecked, className
    } = props;
    return (
        <div className="form-group">
            <div className="offset-sm-3 col-sm-9">
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
                        {label}
                    </label>
                </div>
            </div>
            {errorName && errorValue &&
            <div className="offset-sm-3 col-sm-9">
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
    className: PropTypes.string
};

CheckboxInput.defaultProps = {
    id: 'id',
    errorName: '',
    errorValue: '',
    readOnly: false,
    isChecked: false
};

export default CheckboxInput;
