import React from 'react';
import PropTypes from 'prop-types';

const CancelButton = ({ id, children, onClick }) => (
    <button id={id} className="btn btn-md btn-link" type="button" onClick={onClick}>{children}</button>
);

CancelButton.defaultProps = {
    children: 'Cancel',
    id: 'cancelButtonId',
    onClick: () => true
};

CancelButton.propTypes = {
    children: PropTypes.string,
    id: PropTypes.string,
    onClick: PropTypes.func
};

export default CancelButton;
