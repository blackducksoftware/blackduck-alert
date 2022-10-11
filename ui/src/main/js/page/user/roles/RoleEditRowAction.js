import React, { useState } from 'react';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import RoleEditModal from 'page/user/roles/RoleEditModal';

const useStyles = createUseStyles({
    editCell: {
        '&:hover': {
            cursor: 'pointer',
        }
    }
});

const RoleEditRowAction = ({ rowData, data }) => {
    const classes = useStyles();
    const [showModal, setShowModal] = useState(false);
    // const [selectedData, setSelectedData] = useState(data)

    function handleClick() {
        setShowModal(true);
        // setSelectedData(rowData);
    }
    // console.log('rowDatarowDatarowData', selectedData);
    return (
        <>
            <span className={classes.editCell} onClick={() => handleClick()}>
                <FontAwesomeIcon icon="pencil-alt" />
            </span>
            { showModal ? (
                <RoleEditModal 
                    data={data}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                />
            ) : null }

        </>

    );
};

export default RoleEditRowAction;