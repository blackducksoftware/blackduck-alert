import React from 'react';
import PropTypes from 'prop-types';

const CheckboxInput = (props) => {
    const { errorName, errorValue } = props;
    const {
        name, value, label, onChange, readOnly, id
    } = props;
    // Sometimes we get checked value as a string
    const isChecked = value === 'true' || value;
    return (
        <div className="form-group">
            <div className="col-sm-offset-3 col-sm-8">
                <div className="checkbox">
                    <label>
                        <input
                            id={id}
                            type="checkbox"
                            className="checkboxInput"
                            readOnly={readOnly}
                            disabled={readOnly}
                            name={name}
                            checked={isChecked}
                            value={label}
                            onChange={onChange}
                        />
                        { label }
                    </label>
                </div>
            </div>
            { errorName && errorValue &&
                <div className="col-sm-offset-3 col-sm-8">
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
    value: PropTypes.bool, // should be renamed to isChecked
    label: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    readOnly: PropTypes.bool
};

CheckboxInput.defaultProps = {
    id: null,
    errorName: '',
    errorValue: '',
    readOnly: false,
    value: false
};

export default CheckboxInput;
