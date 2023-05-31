import React, { useState } from 'react';
import PropTypes from 'prop-types';
import StatusMessage from 'common/component/StatusMessage';
import IconButton from 'common/component/button/IconButton';
import AzureBoardModal from 'page/channel/azure/AzureBoardModal';

const AzureCopyCell = ({ data }) => {
    const [showModal, setShowModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();

    const modalOptions = {
        type: 'COPY',
        title: 'Copy Azure Board',
        submitText: 'Save',
        copyDescription: `Performing this action will create a new Azure Board by using the same settings as '${data.name}'`
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

AzureCopyCell.propTypes = {
    data: PropTypes.object
};

export default AzureCopyCell;
