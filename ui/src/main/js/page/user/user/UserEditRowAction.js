import React, { useState } from 'react';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import UserEditModal from 'page/user/user/UserEditModal';

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
    const [selectedData, setSelectedData] = useState(data)

    function handleClick() {
        setShowModal(true);
        setSelectedData(data);
    }

    return (
        <>
            <span className={classes.editCell} onClick={() => handleClick()}>
                <FontAwesomeIcon icon="pencil-alt" />
            </span>
            { showModal ? (
                <UserEditModal 
                    data={selectedData}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                />
            ) : null }
            
        </>

    );
};

export default UserEditRowAction;