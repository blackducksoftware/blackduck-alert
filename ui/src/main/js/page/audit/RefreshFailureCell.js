import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import IconButton from 'common/component/button/IconButton';
import StatusMessage from 'common/component/StatusMessage';
import { useDispatch, useSelector } from 'react-redux';
import { sendJob, sendNotification } from '../../store/actions/audit';

const RefreshFailureCell = ({ data, settings }) => {
    const dispatch = useDispatch();
    const [statusMessage, setStatusMessage] = useState();
    const { error, hasError, refreshNotificationSuccess, refreshJobSuccess } = useSelector((state) => state.audit);
    useEffect(() => {s
        if (hasError) {
            setStatusMessage({
                message: `${error.status}: ${error.statusText}`,
                type: 'error'
            });
        }

        if (refreshNotificationSuccess) {
            setStatusMessage({
                message: 'Successfully refreshed notification.',
                type: 'success'
            });
        }

        if (refreshJobSuccess) {
            setStatusMessage({
                message: 'Successfully refreshed job.',
                type: 'success'
            });
        }
    }, [hasError, error, refreshNotificationSuccess, refreshJobSuccess]);

    const handleRefresh = () => {
        setStatusMessage();
        if (settings.type === 'notification') {
            dispatch(sendNotification(data.id, settings.params));
        }

        if (settings.type === 'job') {
            dispatch(sendJob(settings.notificationId, data.configId));
        }
    };

    return (
        <>
            { statusMessage && (
                <StatusMessage
                    actionMessage={statusMessage.type === 'success' ? statusMessage.message : null}
                    errorMessage={statusMessage.type === 'error' ? statusMessage.message : null}
                />
            )}

            <IconButton icon="sync" onClick={handleRefresh} />
        </>
    );
};

RefreshFailureCell.propTypes = {
    data: PropTypes.object,
    settings: PropTypes.object
};

export default RefreshFailureCell;
