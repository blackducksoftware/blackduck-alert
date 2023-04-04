import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import ProviderModal from 'page/provider/ProviderModal';
import StatusMessage from 'common/component/StatusMessage';

const useStyles = createUseStyles({
    editCell: {
        all: 'unset',
        cursor: 'pointer'
    }
});

const ProviderEditCell = ({ data }) => {
    const classes = useStyles();
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

            <button className={classes.editCell} onClick={() => handleClick()} type="button">
                <FontAwesomeIcon icon="pencil-alt" />
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

ProviderEditCell.propTypes = {
    data: PropTypes.object
};

export default ProviderEditCell;
