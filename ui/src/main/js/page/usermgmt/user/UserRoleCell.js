import React from 'react';
import PropTypes from 'prop-types';

const UserRoleCell = ({ data }) => {
    const { roleNames } = data;
    const multiRole = roleNames.length > 1;
    const displayRole = `${roleNames[0]} and ${roleNames.length - 1} more...`;

    return (
        <>
            {multiRole ? displayRole : roleNames}
        </>
    );
};

UserRoleCell.propTypes = {
    data: PropTypes.object
};

export default UserRoleCell;
