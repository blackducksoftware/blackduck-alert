import React, { useState } from 'react';
import PropTypes from 'prop-types';
import ProviderModal from 'page/provider/ProviderModal';
import StatusMessage from 'common/component/StatusMessage';
import IconButton from 'common/component/button/IconButton';

const ProviderEditCell = ({ data, settings }) => {
    const [showModal, setShowModal] = useState(false);
    const [selectedData, setSelectedData] = useState(data);
    const [statusMessage, setStatusMessage] = useState();
    const successMessage = `Successfully updated ${data.name}`;

    const modalOptions = {
        type: 'EDIT',
        submitText: 'Save Edit',
        title: 'Edit Provider'
    };

    function handleClick() {
        setStatusMessage();
        setShowModal(true);
        setSelectedData(data);
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
                <ProviderModal
                    data={selectedData}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    modalOptions={modalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage={successMessage}
                    readonly={settings.readonly}
                />
            )}
        </>

    );
};

ProviderEditCell.propTypes = {
    data: PropTypes.object,
    settings: PropTypes.shape({
        readonly: PropTypes.bool
    })
};

export default ProviderEditCell;
