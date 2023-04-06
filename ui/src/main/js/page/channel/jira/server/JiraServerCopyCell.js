import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import StatusMessage from 'common/component/StatusMessage';
import JiraServerModal from './JiraServerModal';

const useStyles = createUseStyles({
    copyCell: {
        background: 'none',
        border: 'none',
        cursor: 'pointer',
        '&:focus': {
            outline: 0
        }
    }
});

const AzureCopyCell = ({ data }) => {
    const classes = useStyles();
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
        setSelectedData((data) => ({
            ...data,
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
            <button className={classes.copyCell} onClick={() => handleClick()} type="button">
                <FontAwesomeIcon icon="copy" />
            </button>
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

AzureCopyCell.propTypes = {
    data: PropTypes.object
};

export default AzureCopyCell;
