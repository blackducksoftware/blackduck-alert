import React, { useState } from 'react';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import EditGithubAccountModal from 'page/channel/github/EditGithubAccountModal';

const useStyles = createUseStyles({
    editCell: {
        '&:hover': {
            cursor: 'pointer',
        }
    }
});

const EditGithubRowAction = ({ data }) => {
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
                <EditGithubAccountModal 
                    data={selectedData}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                />
            ) : null }

        </>

    );
};

export default EditGithubRowAction;