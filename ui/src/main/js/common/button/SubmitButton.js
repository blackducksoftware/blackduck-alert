import React from 'react';
import PropTypes from 'prop-types';

const SubmitButton = ({ id, children }) => (
    <button id={id} className="btn btn-md btn-primary" type="submit">{children}</button>
);

SubmitButton.defaultProps = {
    children: 'Submit',
    id: 'submitButtonId'
};

SubmitButton.propTypes = {
    children: PropTypes.string,
    id: PropTypes.string
};

export default SubmitButton;
