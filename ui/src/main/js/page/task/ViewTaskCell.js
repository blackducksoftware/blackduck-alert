import React, { useState } from 'react';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import ViewTaskModal from 'page/task/ViewTaskModal';

const useStyles = createUseStyles({
    editCell: {
        '&:hover': {
            cursor: 'pointer'
        }
    }
});

const ViewTaskCell = ({ data }) => {
    const classes = useStyles();
    const [showModal, setShowModal] = useState(false);

    function handleClick() {
        setShowModal(true);
    }

    return (
        <>
            <span className={classes.editCell} onClick={() => handleClick()}>
                <FontAwesomeIcon icon="eye" />
            </span>
            { showModal ? (
                <ViewTaskModal 
                    data={data}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                />
            ) : null }
        </>

    );
};

export default ViewTaskCell;