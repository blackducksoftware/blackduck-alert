import React from 'react';
import PropTypes from 'prop-types';

const TestButton = ({ onClick, children }) => {
    return (
        <button className="btn btn-primary" type="button" onClick={onClick}>{children}</button>
    );
};

TestButton.defaultProps = {
    children: 'Test'
};

TestButton.propTypes = {
    children: PropTypes.string,
    onClick: PropTypes.func.isRequired
};

export default TestButton;