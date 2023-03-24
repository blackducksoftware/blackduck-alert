import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import RoleModal from 'page/usermgmt/roles/RoleModal';

const useStyles = createUseStyles({
    editCell: {
        background: 'none',
        border: 'none',
        cursor: 'pointer',
        '&:focus': {
            outline: 0
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
            <button className={classes.editCell} onClick={() => handleClick()} type="button">
                <FontAwesomeIcon icon="pencil-alt" />
            </button>
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

RoleEditCell.propTypes = {
    data: PropTypes.object
};

export default RoleEditCell;
