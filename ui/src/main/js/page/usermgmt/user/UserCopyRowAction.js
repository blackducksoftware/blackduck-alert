import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import UserModal from 'page/usermgmt/user/UserModal';
import StatusMessage from 'common/component/StatusMessage';

const useStyles = createUseStyles({
    copyCell: {
        '&:hover': {
            cursor: 'pointer',
        }
    }
});

const UserCopyRowAction = ({ data }) => {
    const classes = useStyles();
    const [showModal, setShowModal] = useState(false);
    const [selectedData, setSelectedData] = useState(data);
    const [statusMessage, setStatusMessage] = useState();

    const modalOptions = {
        type: 'COPY',
        submitText: 'Create',
        title: `Copy User, '${data.username}'`,
        copiedUsername: data.username
    };

    function handleClick() {
        setStatusMessage();
        setShowModal(true);
        setSelectedData((userData) => ({
            ...userData, 
            id: null, 
            username: '', 
            password: '', 
            passwordSet: false,
            emailAddress: selectedData.emailAddress || '',
            roleNames: selectedData.roleNames
        }));
    }

    return (
        <>
            { statusMessage && (
                <StatusMessage 
                    actionMessage={statusMessage.type === 'success' ?  statusMessage.message : null}
                    errorMessage={statusMessage.type === 'error' ?  statusMessage.message : null}
                />
            )}

            <span className={classes.copyCell} onClick={handleClick}>
                <FontAwesomeIcon icon="copy" />
            </span>

            { showModal ? (
                <UserModal 
                    data={selectedData} 
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    modalOptions={modalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage="Successfully created one new user."
                />
            ) : null }
        </>

    );
};

UserCopyRowAction.propTypes = {
    data: PropTypes.object
};

export default UserCopyRowAction;