import React from 'react';
import PropTypes from 'prop-types';

const GeneralButton = ({
                           onClick, children, className, id, disabled
                       }) => (
    <button
        id={id}
        className={`btn btn-primary ${className}`}
        type="button"
        onClick={onClick}
        disabled={disabled}
    >{children}
    </button>
);

GeneralButton.defaultProps = {
    children: 'Click Me',
    className: 'btn-md',
    id: 'id',
    disabled: false
};

GeneralButton.propTypes = {
    children: PropTypes.string,
    className: PropTypes.string,
    id: PropTypes.string,
    onClick: PropTypes.func.isRequired,
    disabled: PropTypes.bool
};

export default GeneralButton;
