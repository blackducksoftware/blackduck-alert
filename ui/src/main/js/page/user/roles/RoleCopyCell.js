import React, { useState } from 'react';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import RoleModal from 'page/user/roles/RoleModal';

const useStyles = createUseStyles({
    copyCell: {
        '&:hover': {
            cursor: 'pointer',
        }
    }
});

const RoleCopyCell = ({ data }) => {
    const classes = useStyles();
    const [showModal, setShowModal] = useState(false);
    const [selectedData, setSelectedData] = useState(data);
    const selectedRoleName = data.roleName;
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
                <RoleModal 
                    data={selectedData}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    copiedRoleName={selectedRoleName}
                    type="copy"
                    title="Copy Role"
                    submitText="Save"
                />
            ) : null }

        </>

    );
};

export default RoleCopyCell;