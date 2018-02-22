import React from 'react';
import PropTypes from 'prop-types';

const SaveButton = ({ onClick, children }) => {
    return (
        <button className="btn btn-primary" type="submit" onClick={onClick}>{children}</button>
    );
};

SaveButton.defaultProps = {
    children: 'Submit'
};

SaveButton.propTypes = {
    children: PropTypes.string,
    onClick: PropTypes.func.isRequired
};

export default SaveButton;