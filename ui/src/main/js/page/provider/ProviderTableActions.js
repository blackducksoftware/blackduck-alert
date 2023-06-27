import React, { useState } from 'react';
import PropTypes from 'prop-types';
import ProviderDeleteModal from 'page/provider/ProviderDeleteModal';
import ProviderModal from 'page/provider/ProviderModal';
import StatusMessage from 'common/component/StatusMessage';
import Button from 'common/component/button/Button';
import { fetchProviders } from 'store/actions/provider';
import { useDispatch, useSelector } from 'react-redux';

const ProviderTableActions = ({ data, selected, readonly, setSelected }) => {
    const modalOptions = {
        type: 'CREATE',
        submitText: 'Create',
        title: 'Create Provider'
    };

    const dispatch = useDispatch();
    const { fetching } = useSelector((state) => state.provider);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();

    function handleCreateProviderClick() {
        setStatusMessage();
        setShowCreateModal(true);
    }

    function handleDeleteProviderClick() {
        setStatusMessage();
        setShowDeleteModal(true);
    }

    function handleRefresh() {
        dispatch(fetchProviders());
    }

    return (
        <>
            {statusMessage && (
                <StatusMessage
                    actionMessage={statusMessage.type === 'success' ? statusMessage.message : null}
                    errorMessage={statusMessage.type === 'error' ? statusMessage.message : null}
                />
            )}

            <Button onClick={handleCreateProviderClick} type="button" icon="plus" text="Create Provider" />

            <Button
                onClick={handleDeleteProviderClick}
                type="button"
                icon="trash"
                isDisabled={selected.length === 0}
                text="Delete"
                buttonStyle="delete"
            />

            <Button onClick={handleRefresh} type="button" text="Refresh" isDisabled={fetching} showLoader={fetching} />

            {showCreateModal && (
                <ProviderModal
                    data={data}
                    isOpen={showCreateModal}
                    toggleModal={setShowCreateModal}
                    modalOptions={modalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage="Successfully created 1 new provider."
                    readonly={readonly}
                />
            )}

            { showDeleteModal && (
                <ProviderDeleteModal
                    data={data}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={selected}
                    setStatusMessage={setStatusMessage}
                    setSelected={setSelected}
                />
            )}
        </>
    );
};

ProviderTableActions.propTypes = {
    data: PropTypes.arrayOf(PropTypes.object),
    selected: PropTypes.array,
    readonly: PropTypes.bool,
    setSelected: PropTypes.func
};

export default ProviderTableActions;
