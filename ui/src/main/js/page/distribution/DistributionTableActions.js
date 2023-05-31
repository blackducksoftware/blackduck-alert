import React, { useState } from 'react';
import PropTypes from 'prop-types';
import Button from 'common/component/button/Button';
import DistributionDeleteModal from 'page/distribution/DistributionDeleteModal';
import StatusMessage from 'common/component/StatusMessage';
import { DISTRIBUTION_URLS } from 'page/distribution/DistributionModel';
import { useHistory } from 'react-router-dom/cjs/react-router-dom';

const DistributionTableActions = ({ data, selected, setSelected }) => {
    const history = useHistory();
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();

    function handleCreateProviderClick() {
        history.push(DISTRIBUTION_URLS.distributionConfigUrl);
    }

    function handleDeleteProviderClick() {
        setStatusMessage();
        setShowDeleteModal(true);
    }

    return (
        <>
            { statusMessage && (
                <StatusMessage
                    actionMessage={statusMessage.type === 'success' ? statusMessage.message : null}
                    errorMessage={statusMessage.type === 'error' ? statusMessage.message : null}
                />
            )}

            <Button onClick={handleCreateProviderClick} type="button" icon="plus" text="Create Job" />

            <Button
                onClick={handleDeleteProviderClick}
                type="button"
                icon="trash"
                isDisabled={selected.length === 0}
                text="Delete"
                buttonStyle="delete"
            />

            { showDeleteModal && (
                <DistributionDeleteModal
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

DistributionTableActions.propTypes = {
    data: PropTypes.arrayOf(PropTypes.object),
    selected: PropTypes.array,
    setSelected: PropTypes.func
};

export default DistributionTableActions;
