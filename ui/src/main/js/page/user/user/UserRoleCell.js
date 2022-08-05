import React from 'react';

const UserRoleCell = ({ data }) => {
    const { roleNames } = data;
    const multiRole = roleNames.length > 1;
    const displayRole = `${roleNames[0]} and ${roleNames.length} more...`

    return (
        <>
            {multiRole ? displayRole : roleNames}
        </>
    );
};

export default UserRoleCell;