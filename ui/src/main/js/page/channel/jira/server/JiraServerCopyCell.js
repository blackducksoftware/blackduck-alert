import React, { useState } from 'react';
import PropTypes from 'prop-types';
import StatusMessage from 'common/component/StatusMessage';
import IconButton from 'common/component/button/IconButton';
import JiraServerModal from 'page/channel/jira/server/JiraServerModal';

const JiraServerCopyCell = ({ data, settings }) => {
    const [showModal, setShowModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();
    const { paramsConfig } = settings;

    const modalOptions = {
        type: 'COPY',
        title: 'Copy Jira Server',
        submitText: 'Save',
        copyDescription: `Performing this action will create a new Jira Server connection by using the same settings as '${data.name}'`
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

            <IconButton icon="copy" onClick={() => handleClick()} />

            { showModal && (
                <JiraServerModal
                    data={data}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    modalOptions={modalOptions}
                    setStatusMessage={setStatusMessage}
                    statusMessage="Successfully edited 1 Jira Server Board."
                    paramsConfig={paramsConfig}
                />
            )}
        </>
    );
};

JiraServerCopyCell.propTypes = {
    data: PropTypes.object,
    settings: PropTypes.shape({
        paramsConfig: PropTypes.object
    })
};

export default JiraServerCopyCell;
