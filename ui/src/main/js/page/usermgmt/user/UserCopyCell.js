import React, { useState } from 'react';
import PropTypes from 'prop-types';
import UserModal from 'page/usermgmt/user/UserModal';
import StatusMessage from 'common/component/StatusMessage';
import IconButton from 'common/component/button/IconButton';

const UserCopyCell = ({ data }) => {
    const [showModal, setShowModal] = useState(false);
    const [selectedData, setSelectedData] = useState(data);
    const [statusMessage, setStatusMessage] = useState();

    const modalOptions = {
        type: 'COPY',
        submitText: 'Create',
        title: `Copy User, '${data.username}'`,
        copyDescription: `Performing this action will create a new user by using the same settings as '${data.username}'`
    };

    function handleClick() {
        setStatusMessage();
        setShowModal(true);
        setSelectedData((userData) => ({
            ...userData,
            id: null,
            username: '',
            password: '',
            passwordSet: false,
            emailAddress: selectedData.emailAddress || '',
            roleNames: selectedData.roleNames
        }));
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

            { showModal ? (
                <UserModal
                    data={selectedData}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    modalOptions={modalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage="Successfully created one new user."
                />
            ) : null }
        </>

    );
};

UserCopyCell.propTypes = {
    data: PropTypes.object
};

export default UserCopyCell;
