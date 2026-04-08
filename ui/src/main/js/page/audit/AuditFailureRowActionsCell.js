import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import PropTypes from 'prop-types';
import { sendJob, sendNotification } from 'store/actions/audit';
import Dropdown from 'react-bootstrap/Dropdown';
import AuditFailureModal from 'page/audit/AuditFailureModal';
import StatusMessage from 'common/component/StatusMessage';
import RowActionsCell from 'common/component/table/cell/RowActionsCell';

const AuditFailureRowActionsCell = ({ data, settings }) => {
    const dispatch = useDispatch();
    const [showViewAuditFailureModal, setShowViewAuditFailureModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();
    const { error, hasError, refreshNotificationSuccess, refreshJobSuccess } = useSelector((state) => state.audit);


    function handleShowModal() {
        setShowViewAuditFailureModal(true);
    }

    useEffect(() => {
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

    const handleRefreshJob = () => {
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
            {statusMessage && (
                <StatusMessage
                    actionMessage={statusMessage.type === 'success' ? statusMessage.message : null}
                    errorMessage={statusMessage.type === 'error' ? statusMessage.message : null}
                />
            )}

            <RowActionsCell>
                <Dropdown.Item as="button" onClick={handleRefreshJob}>
                    Refresh Job
                </Dropdown.Item>
                <Dropdown.Item as="button" onClick={handleShowModal}>
                    View Details
                </Dropdown.Item>
            </RowActionsCell>

            { showViewAuditFailureModal && (
                <AuditFailureModal
                    data={data}
                    isOpen={showViewAuditFailureModal}
                    toggleModal={setShowViewAuditFailureModal}
                />
            )}
        </>
    );
};

AuditFailureRowActionsCell.propTypes = {
    data: PropTypes.object,
    settings: PropTypes.object
};

export default AuditFailureRowActionsCell;
