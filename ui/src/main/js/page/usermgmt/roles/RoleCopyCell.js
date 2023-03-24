import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import RoleModal from 'page/usermgmt/roles/RoleModal';

const useStyles = createUseStyles({
    copyCell: {
        background: 'none',
        border: 'none',
        cursor: 'pointer',
        '&:focus': {
            outline: 0
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
        setSelectedData((userData) => ({ ...userData, id: null, username: '', password: '', emailAddress: email }));
    }

    return (
        <>
            <button className={classes.copyCell} onClick={() => handleClick()} type="button">
                <FontAwesomeIcon icon="copy" />
            </button>
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

RoleCopyCell.propTypes = {
    data: PropTypes.shape({
        id: PropTypes.string,
        roleName: PropTypes.string
    })
};

export default RoleCopyCell;
