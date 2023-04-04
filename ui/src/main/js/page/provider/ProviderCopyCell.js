import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import ProviderModal from 'page/provider/ProviderModal';
import StatusMessage from 'common/component/StatusMessage';

const useStyles = createUseStyles({
    copyCell: {
        all: 'unset',
        cursor: 'pointer'
    }
});

const ProviderCopyCell = ({ data }) => {
    const classes = useStyles();
    const [showModal, setShowModal] = useState(false);
    const [selectedData, setSelectedData] = useState(data);
    const [statusMessage, setStatusMessage] = useState();
    const successMessage = `Successfully created 1 new provider.`;

    const modalOptions = {
        type: 'COPY',
        submitText: 'Save',
        title: 'Copy Provider',
        copyDescription: `Performing this action will create a new provider by using the same settings as '${data.name}'`
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

            <button className={classes.copyCell} onClick={() => handleClick()} type="button">
                <FontAwesomeIcon icon="copy" />
            </button>

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
