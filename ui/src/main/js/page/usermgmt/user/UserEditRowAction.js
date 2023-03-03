import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import UserModal from 'page/usermgmt/user/UserModal';

const useStyles = createUseStyles({
    editCell: {
        '&:hover': {
            cursor: 'pointer',
        }
    }
});

const UserEditRowAction = ({ data }) => {
    const classes = useStyles();
    const [showModal, setShowModal] = useState(false);
    const [selectedData, setSelectedData] = useState(data);
    const modalOptions = {
        type: 'EDIT',
        submitText: 'Save Edit',
        title: 'Edit User'
    };

    function handleClick() {
        setShowModal(true);
        setSelectedData(data);
    }

    return (
        <>
            <span className={classes.editCell} onClick={() => handleClick()}>
                <FontAwesomeIcon icon="pencil-alt" />
            </span>
            { showModal && (
                <UserModal 
                    data={selectedData}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    modalOptions={modalOptions}
                />
            )}
        </>

    );
};

UserEditRowAction.propTypes = {
    data: PropTypes.object
};

export default UserEditRowAction;