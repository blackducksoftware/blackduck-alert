import React from 'react';
import PropTypes from 'prop-types';

const GeneralButton = ({ onClick, children, className, id }) => (
    <button id={id} className={`btn btn-primary ${className}`} type="button" onClick={onClick}>{children}</button>
);

GeneralButton.defaultProps = {
    id: null,
    children: 'Click Me',
    className: 'btn-sm'
};

GeneralButton.propTypes = {
    id: PropTypes.string,
    children: PropTypes.string,
    className: PropTypes.string,
    onClick: PropTypes.func.isRequired
};

export default GeneralButton;
