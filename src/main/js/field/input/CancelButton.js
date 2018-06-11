import React from 'react';
import PropTypes from 'prop-types';

const CancelButton = ({ onClick, children, id }) => (
    <button id={id} className="btn btn-sm btn-link" type="reset" onClick={onClick}>{children}</button>
);

CancelButton.defaultProps = {
    id: null,
    children: 'Cancel',
    onClick: () => true
};

CancelButton.propTypes = {
    id: PropTypes.string,
    children: PropTypes.string,
    onClick: PropTypes.func
};

export default CancelButton;
