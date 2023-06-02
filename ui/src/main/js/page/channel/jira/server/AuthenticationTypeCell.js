import React from 'react';
import PropTypes from 'prop-types';

function getAuthType(type) {
    switch (type) {
        case 'BASIC':
            return 'Basic';
        case 'PERSONAL_ACCESS_TOKEN':
            return 'Personal Access Token';
        default:
            return 'Unknown';
    }
}

const AuthenticationTypeCell = ({ data }) => {
    const { authorizationMethod } = data;
    return (
        <>
            {getAuthType(authorizationMethod)}
        </>
    )
}

AuthenticationTypeCell.propTypes = {
    data: PropTypes.shape({
        authorizationMethod: PropTypes.string
    })
};

export default AuthenticationTypeCell;
