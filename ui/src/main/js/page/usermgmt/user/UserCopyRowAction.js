import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import UserCopyModal from 'page/usermgmt/user/UserCopyModal';

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
    const selectedUsername = data.username;
    const email = selectedData.emailAddress ? selectedData.emailAddress : '';

    function handleClick() {
        setShowModal(true);
        setSelectedData(userData => ({...userData, id: null, username: '', password: '', emailAddress: email }))
    }

    return (
        <>
            <span className={classes.copyCell} onClick={() => handleClick()}>
                <FontAwesomeIcon icon="copy" />
            </span>
            { showModal ? (
                <UserCopyModal 
                    data={selectedData}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    copiedUsername={selectedUsername}
                />
            ) : null }
            
        </>

    );
};

UserCopyRowAction.propTypes = {
    data: PropTypes.object
};

export default UserCopyRowAction;