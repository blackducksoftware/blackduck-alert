import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import ViewTaskModal from 'page/task/ViewTaskModal';

const useStyles = createUseStyles({
    editCell: {
        background: 'none',
        border: 'none',
        padding: 0,
        cursor: 'pointer',
        '&:focus': {
            outline: 0
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
            <button className={classes.editCell} onClick={() => handleClick()} type="button">
                <FontAwesomeIcon icon="eye" />
            </button>
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

ViewTaskCell.propTypes = {
    data: PropTypes.shape({
        fullyQualifiedType: PropTypes.string,
        nextRunTime: PropTypes.string,
        properties: PropTypes.array,
        type: PropTypes.string
    })
};

export default ViewTaskCell;
