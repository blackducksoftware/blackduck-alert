import React from 'react';
import PropTypes from 'prop-types';

const SubmitButton = ({ onClick, children, id }) => (
    <button id={id} className="btn btn-sm btn-primary" type="submit" onClick={onClick}>{children}</button>
);

SubmitButton.defaultProps = {
    children: 'Submit',
    onClick: () => true
};

SubmitButton.propTypes = {
    children: PropTypes.string,
    onClick: PropTypes.func
};

export default SubmitButton;
