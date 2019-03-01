import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';
import Tooltip from 'react-bootstrap/Tooltip';
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';

const SelectInput = (props) => {
    const { errorName, errorValue } = props;
    const {
        label, onChange, id, className, description, options, isSearchable, placeholder, value
    } = props;
    return (

        <div className="form-group">
            <label className="col-sm-4 col-form-label text-right">{label}</label>
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
            <div className="d-inline-flex p-2 col-sm-4">
                <Select
                    id={id}
                    className={className}
                    onChange={onChange}
                    isSearchable={isSearchable}
                    options={options}
                    placeholder={placeholder}
                    value={value}
                />
            </div>
            {errorName && errorValue &&
            <div className="offset-sm-3 col-sm-8">
                <p className="fieldError" name={errorName}>{errorValue}</p>;
            </div>
            }
        </div>
    );
};

SelectInput.propTypes = {
    id: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.string,
    label: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    className: PropTypes.string,
    description: PropTypes.string,
    options: PropTypes.array,
    isSearchable: PropTypes.bool,
    placeholder: PropTypes.string,
    value: PropTypes.object
};

SelectInput.defaultProps = {
    id: 'id',
    errorName: '',
    errorValue: '',
    description: null,
    options: [],
    isSearchable: true,
    placeholder: 'Choose a value',
    value: { label: '-- none --', value: '' }
};

export default SelectInput;
