import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import UserModal from 'page/usermgmt/user/UserModal';

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
    const modalOptions = {
        type: 'COPY',
        submitText: 'Create',
        title: `Copy User, '${data.username}'`,
        copiedUsername: data.username
    };

    function handleClick() {
        setShowModal(true);
        setSelectedData(userData => ({
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
            <span className={classes.copyCell} onClick={handleClick}>
                <FontAwesomeIcon icon="copy" />
            </span>
            { showModal ? (
                <UserModal 
                    data={selectedData} 
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    modalOptions={modalOptions}
                />
            ) : null }
            
        </>

    );
};

UserCopyRowAction.propTypes = {
    data: PropTypes.object
};

export default UserCopyRowAction;