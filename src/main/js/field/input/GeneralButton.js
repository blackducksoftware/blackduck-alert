import React from 'react';
import PropTypes from 'prop-types';

const GeneralButton = ({ onClick, children, className, id }) => (
    <button id={id} className={`btn btn-primary ${className}`} type="button" onClick={onClick}>{children}</button>
);

GeneralButton.defaultProps = {
    children: 'Click Me',
    className: 'btn-md',
    id: 'id'
};

GeneralButton.propTypes = {
    children: PropTypes.string,
    className: PropTypes.string,
    id: PropTypes.string,
    onClick: PropTypes.func.isRequired
};

export default GeneralButton;
