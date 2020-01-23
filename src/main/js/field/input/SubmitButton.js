import React from 'react';
import PropTypes from 'prop-types';

const SubmitButton = ({ children, id }) => (
    <button id={id} className="btn btn-md btn-primary" type="submit">{children}</button>
);

SubmitButton.defaultProps = {
    children: 'Submit',
    id: 'id'
};

SubmitButton.propTypes = {
    children: PropTypes.string,
    id: PropTypes.string
};

export default SubmitButton;
