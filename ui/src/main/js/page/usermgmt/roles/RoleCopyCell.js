import React, { useState } from 'react';
import PropTypes from 'prop-types';
import RoleModal from 'page/usermgmt/roles/RoleModal';
import StatusMessage from 'common/component/StatusMessage';
import IconButton from 'common/component/button/IconButton';

const RoleCopyCell = ({ data }) => {
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

            <IconButton icon="copy" onClick={() => handleClick()} />

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
