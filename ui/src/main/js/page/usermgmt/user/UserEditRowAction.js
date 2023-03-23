import React, { useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import UserModal from 'page/usermgmt/user/UserModal';
import StatusMessage from 'common/component/StatusMessage';

const useStyles = createUseStyles({
    editCell: {
        '&:hover': {
            cursor: 'pointer'
        }
    }
});

const UserEditRowAction = ({ data }) => {
    const classes = useStyles();
    const [showModal, setShowModal] = useState(false);
    const [selectedData, setSelectedData] = useState(data);
    const [statusMessage, setStatusMessage] = useState();
    const successMessage = `Successfully updated ${data.username}`
    
    const modalOptions = {
        type: 'EDIT',
        submitText: 'Save Edit',
        title: 'Edit User'
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
                    actionMessage={statusMessage.type === 'success' ?  statusMessage.message : null}
                    errorMessage={statusMessage.type === 'error' ?  statusMessage.message : null}
                />
            )}

            <span className={classes.editCell} onClick={() => handleClick()}>
                <FontAwesomeIcon icon="pencil-alt" />
            </span>

            { showModal && (
                <UserModal 
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

UserEditRowAction.propTypes = {
    data: PropTypes.object
};

export default UserEditRowAction;