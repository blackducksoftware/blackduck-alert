import React from 'react';
import PropTypes from 'prop-types';

const CancelButton = ({ onClick, children }) => {
    return (
        <button className="btn btn-sm btn-link" type="reset" onClick={onClick}>{children}</button>
    );
};

CancelButton.defaultProps = {
    children: 'Cancel',
    onClick: () => true
};

CancelButton.propTypes = {
    children: PropTypes.string,
    onClick: PropTypes.func
};

export default CancelButton;