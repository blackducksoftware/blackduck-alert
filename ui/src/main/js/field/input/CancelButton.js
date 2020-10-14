import React from 'react';
import PropTypes from 'prop-types';

const CancelButton = ({ onClick, children, id }) => (
    <button id={id} className="btn btn-md btn-link" type="reset" onClick={onClick}>{children}</button>
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
