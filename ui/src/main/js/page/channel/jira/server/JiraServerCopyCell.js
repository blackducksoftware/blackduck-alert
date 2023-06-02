import React, { useState } from 'react';
import PropTypes from 'prop-types';
import StatusMessage from 'common/component/StatusMessage';
import IconButton from 'common/component/button/IconButton';
import JiraServerModal from 'page/channel/jira/server/JiraServerModal';

const JiraServerCopyCell = ({ data }) => {
    const [showModal, setShowModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();
    const [selectedData, setSelectedData] = useState(data);

    const modalOptions = {
        type: 'COPY',
        title: 'Copy Jira Server',
        submitText: 'Save',
        copyDescription: `Performing this action will create a new Jira Server connection by using the same settings as '${data.name}'`
    };

    function handleClick() {
        setStatusMessage();
        setShowModal(true);
        setSelectedData((slectedRow) => ({
            ...slectedRow,
            id: null,
            name: '',
            password: '',
            isPasswordSet: false
        }));
    }

    return (
        <>
            { statusMessage && (
                <StatusMessage
                    actionMessage={statusMessage.type === 'success' ? statusMessage.message : null}
                    errorMessage={statusMessage.type === 'error' ? statusMessage.message : null}
                />
            )}

            <IconButton icon="copy" onClick={() => handleClick()} />

            { showModal && (
                <JiraServerModal
                    data={selectedData}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    modalOptions={modalOptions}
                    setStatusMessage={setStatusMessage}
                    statusMessage="Successfully edited 1 Jira Server Board."
                />
            )}
        </>
    );
};

JiraServerCopyCell.propTypes = {
    data: PropTypes.object
};

export default JiraServerCopyCell;
