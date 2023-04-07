import React, { useState } from 'react';
import PropTypes from 'prop-types';
import StatusMessage from 'common/component/StatusMessage';
import IconButton from 'common/component/button/IconButton';
import JiraServerModal from 'page/channel/jira/server/JiraServerModal';

const JiraServerEditCell = ({ data }) => {
    const [showModal, setShowModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();

    const modalOptions = {
        type: 'EDIT',
        title: 'Edit Jira Server',
        submitText: 'Save'
    };

    function handleClick() {
        setShowModal(true);
    }

    return (
        <>
            { statusMessage && (
                <StatusMessage
                    actionMessage={statusMessage.type === 'success' ? statusMessage.message : null}
                    errorMessage={statusMessage.type === 'error' ? statusMessage.message : null}
                />
            )}

            <IconButton icon="pencil-alt" onClick={() => handleClick()} />

            { showModal && (
                <JiraServerModal
                    data={data}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    modalOptions={modalOptions}
                    setStatusMessage={setStatusMessage}
                    statusMessage="Successfully edited 1 Jira Server."
                />
            )}
        </>
    );
};

JiraServerEditCell.propTypes = {
    data: PropTypes.object
};

export default JiraServerEditCell;
