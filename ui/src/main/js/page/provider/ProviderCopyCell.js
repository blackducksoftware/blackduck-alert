import React, { useState } from 'react';
import PropTypes from 'prop-types';
import ProviderModal from 'page/provider/ProviderModal';
import StatusMessage from 'common/component/StatusMessage';
import IconButton from 'common/component/button/IconButton';

const ProviderCopyCell = ({ data }) => {
    const [showModal, setShowModal] = useState(false);
    const [selectedData, setSelectedData] = useState(data);
    const [statusMessage, setStatusMessage] = useState();
    const successMessage = 'Successfully created 1 new provider.';

    const modalOptions = {
        type: 'COPY',
        submitText: 'Save',
        title: 'Copy Provider',
        copyDescription: `Performing this action will create a new provider by using the same settings as '${data.name}'`
    };

    function handleClick() {
        setStatusMessage();
        setShowModal(true);
        setSelectedData((rowData) => ({
            ...rowData,
            id: null,
            name: '',
            createdAt: null,
            lastUpdated: null
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
                <ProviderModal
                    data={selectedData}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    modalOptions={modalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage={successMessage}
                />
            )}
        </>

    );
};

ProviderCopyCell.propTypes = {
    data: PropTypes.object
};

export default ProviderCopyCell;
