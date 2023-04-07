import React, { useState } from 'react';
import PropTypes from 'prop-types';
import StatusMessage from 'common/component/StatusMessage';
import IconButton from 'common/component/button/IconButton';
import AzureBoardModal from 'page/channel/azure/AzureBoardModal';

const AzureEditCell = ({ data }) => {
    const [showModal, setShowModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();

    const modalOptions = {
        type: 'EDIT',
        title: 'Edit Azure Board',
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
                <AzureBoardModal
                    data={data}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    modalOptions={modalOptions}
                    setStatusMessage={setStatusMessage}
                    statusMessage="Successfully edited 1 Azure Board."
                />
            )}
        </>
    );
};

AzureEditCell.propTypes = {
    data: PropTypes.object
};

export default AzureEditCell;
