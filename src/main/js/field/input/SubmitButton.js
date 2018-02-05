import React from 'react';
import PropTypes from 'prop-types';

const SubmitButton = ({ label, onClick, disabled }) => (
    <button
        type="submit"
        className="btn btn-form btn-primary"
        onClick={onClick}
        disabled={disabled}
    >{ label }
    </button>
);

SubmitButton.defaultProps = {
    onClick: () => true,
    disabled: false
};

SubmitButton.propTypes = {
    label: PropTypes.string.isRequired,
    disabled: PropTypes.bool,
    onClick: PropTypes.func
};

export default SubmitButton;
