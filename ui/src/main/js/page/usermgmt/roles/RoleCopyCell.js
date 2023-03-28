import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import RoleModal from 'page/usermgmt/roles/RoleModal';
import StatusMessage from 'common/component/StatusMessage';

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
    const [statusMessage, setStatusMessage] = useState();
    const [selectedData, setSelectedData] = useState(data);

    const COPY_ROLE_OPTIONS = {
        type: 'COPY',
        title: 'Copy Role',
        submitText: 'Save',
        copyDescription: `Performing this action will create a new role by using the same settings as '${data.roleName}'`
    };

    function handleClick() {
        setShowModal(true);
        setSelectedData((roleData) => ({ ...roleData, id: null }));
    }

    return (
        <>
            { statusMessage && (
                <StatusMessage
                    actionMessage={statusMessage.type === 'success' ? statusMessage.message : null}
                    errorMessage={statusMessage.type === 'error' ? statusMessage.message : null}
                />
            )}
            <button className={classes.copyCell} onClick={() => handleClick()} type="button">
                <FontAwesomeIcon icon="copy" />
            </button>
            { showModal && (
                <RoleModal
                    data={selectedData}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    modalOptions={COPY_ROLE_OPTIONS}
                    setStatusMessage={setStatusMessage}
                    statusMessage="Successfully created 1 role."
                />
            ) }
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
