import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import RoleModal from 'page/usermgmt/roles/RoleModal';
import StatusMessage from 'common/component/StatusMessage';

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
    const [statusMessage, setStatusMessage] = useState();

    const EDIT_ROLE_OPTIONS = {
        type: 'EDIT',
        title: 'Edit Role',
        submitText: 'Save'
    };

    function handleClick() {
        setShowModal(true);
    }

    return (
        <>
            { statusMessage && (
                <StatusMessage
                    actionMessage={statusMessage.type === 'success' ? statusMessage.message : null}
                    errorMessage={statusMessage.type === 'error' ? statusMessage.message : null}
                />
            )}
            <button className={classes.editCell} onClick={() => handleClick()} type="button">
                <FontAwesomeIcon icon="pencil-alt" />
            </button>
            { showModal && (
                <RoleModal
                    data={data}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    modalOptions={EDIT_ROLE_OPTIONS}
                    setStatusMessage={setStatusMessage}
                    statusMessage="Successfully edited 1 Role."
                />
            )}
        </>
    );
};

RoleEditCell.propTypes = {
    data: PropTypes.object
};

export default RoleEditCell;
