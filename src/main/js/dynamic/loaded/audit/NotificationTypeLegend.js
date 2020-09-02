import React from 'react';
import PropTypes from 'prop-types';
import { OverlayTrigger, Popover } from 'react-bootstrap';

const NotificationTypeLegend = ({ notificationTypes }) => {
    const notificationTypeLength = (notificationTypes) ? notificationTypes.length : 0;
    const notificationTypeList = (notificationTypes) ? notificationTypes.map((notificationType) => <div>{notificationType}</div>) : [];
    const notificationTypeMessage = (notificationTypeLength == 1) ? notificationTypes[0] : `${notificationTypeLength} Types`;
    return (
        <OverlayTrigger
            trigger={['hover', 'focus']}
            placement="right"
            overlay={(
                <Popover id="popover" title="Notification Type Legend">
                    {notificationTypeList}
                </Popover>
            )}
        >
            <span>
                {notificationTypeMessage}
            </span>
        </OverlayTrigger>
    );
};

NotificationTypeLegend.propTypes = {
    notificationTypes: PropTypes.array
};

NotificationTypeLegend.defaultProps = {
    notificationTypes: []
};

export default NotificationTypeLegend;
