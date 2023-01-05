import React, { useState } from 'react';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import RoleModal from 'page/user/roles/RoleModal';

const useStyles = createUseStyles({
    editCell: {
        '&:hover': {
            cursor: 'pointer'
        }
    }
});

const RoleEditCell = ({ data }) => {
    const classes = useStyles();
    const [showModal, setShowModal] = useState(false);

    function handleClick() {
        setShowModal(true);
    }

    return (
        <>
            <span className={classes.editCell} onClick={() => handleClick()}>
                <FontAwesomeIcon icon="pencil-alt" />
            </span>
            { showModal ? (
                <RoleModal 
                    data={data}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    type="edit"
                    title="Edit Role"
                    submitText="Save"
                />
            ) : null }

        </>

    );
};

export default RoleEditCell;
