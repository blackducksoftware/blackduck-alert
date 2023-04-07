import React, { useState } from 'react';
import PropTypes from 'prop-types';
import RoleModal from 'page/usermgmt/roles/RoleModal';
import StatusMessage from 'common/component/StatusMessage';
import IconButton from 'common/component/button/IconButton';

const RoleEditCell = ({ data }) => {
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

            <IconButton icon="pencil-alt" onClick={() => handleClick()} />

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
