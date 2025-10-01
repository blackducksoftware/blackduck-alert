import React, { useState } from 'react';
import PropTypes from 'prop-types';
import Button from 'common/component/button/Button';
import DistributionDeleteModal from 'page/distribution/DistributionDeleteModal';
import StatusMessage from 'common/component/StatusMessage';
import { DISTRIBUTION_URLS } from 'page/distribution/DistributionModel';
import { useHistory } from 'react-router-dom/cjs/react-router-dom';
import { fetchDistribution } from 'store/actions/distribution';
import { useDispatch, useSelector } from 'react-redux';

const DistributionTableActions = ({ data, selected, setSelected, paramsConfig, setParamsConfig }) => {
    const dispatch = useDispatch();
    const history = useHistory();
    const { fetching } = useSelector((state) => state.distribution);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();

    function handleCreateProviderClick() {
        history.push(DISTRIBUTION_URLS.distributionConfigUrl);
    }

    function handleDeleteProviderClick() {
        setStatusMessage();
        setShowDeleteModal(true);
    }

    function handleRefresh() {
        dispatch(fetchDistribution(paramsConfig));
    }

    return (
        <>
            {statusMessage && (
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

            <Button onClick={handleRefresh} type="button" text="Refresh" isDisabled={fetching} showLoader={fetching} />

            {showDeleteModal && (
                <DistributionDeleteModal
                    data={data}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={selected}
                    setStatusMessage={setStatusMessage}
                    setSelected={setSelected}
                    paramsConfig={paramsConfig}
                    setParamsConfig={setParamsConfig}
                />
            )}
        </>
    );
};

DistributionTableActions.propTypes = {
    data: PropTypes.shape({
        models: PropTypes.arrayOf(PropTypes.object)
    }),
    selected: PropTypes.array,
    setSelected: PropTypes.func,
    paramsConfig: PropTypes.object,
    setParamsConfig: PropTypes.func
};

export default DistributionTableActions;
