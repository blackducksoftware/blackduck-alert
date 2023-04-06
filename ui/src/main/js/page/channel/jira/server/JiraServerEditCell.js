import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import StatusMessage from 'common/component/StatusMessage';
import JiraServerModal from './JiraServerModal';

const useStyles = createUseStyles({
    editCell: {
        background: 'none',
        border: 'none',
        cursor: 'pointer',
        '&:focus': {
            outline: 0
        }
    }
});

const JiraServerEditCell = ({ data }) => {
    const classes = useStyles();
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
            <button className={classes.editCell} onClick={() => handleClick()} type="button">
                <FontAwesomeIcon icon="pencil-alt" />
            </button>
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
