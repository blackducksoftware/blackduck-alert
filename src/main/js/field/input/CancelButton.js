import React from 'react';
import PropTypes from 'prop-types';

const CancelButton = ({ onClick, children }) => {
    return (
        <button className="btn btn-link" type="button" onClick={onClick}>{children}</button>
    );
};

CancelButton.defaultProps = {
    children: 'Cancel'
};

CancelButton.propTypes = {
    children: PropTypes.string,
    onClick: PropTypes.func.isRequired
};

export default CancelButton;