import React from 'react';
import PropTypes from 'prop-types';

const NotificationCell = ({ data }) => {
    const { notificationType } = data.notification;

    return (
        <>
            {notificationType}
        </>
    );
};

NotificationCell.propTypes = {
    data: PropTypes.object
};

export default NotificationCell;
