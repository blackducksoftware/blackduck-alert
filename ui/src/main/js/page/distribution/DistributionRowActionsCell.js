import React, { useState } from 'react';
import PropTypes from 'prop-types';
import DistributionDeleteModal from 'page/distribution/DistributionDeleteModal';
import StatusMessage from 'common/component/StatusMessage';
import Dropdown from 'react-bootstrap/Dropdown';
import RowActionsCell from 'common/component/table/cell/RowActionsCell';
import { DISTRIBUTION_URLS } from 'page/distribution/DistributionModel';
import { useNavigate } from 'react-router-dom';

const DistributionRowActionsCell = ({ data, settings }) => {
    const navigate = useNavigate();
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();
    const dataStagedForDelete = { models: [data] };
    const { paramsConfig, setParamsConfig, readonly } = settings;

    function handleCopyClick() {
        navigate(`${DISTRIBUTION_URLS.distributionConfigCopyUrl}/${data.jobId}`);
    }

    function handleEditClick() {
        navigate(`${DISTRIBUTION_URLS.distributionConfigUrl}/${data.jobId}`);
    }

    function handleDeleteClick() {
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

            <RowActionsCell>
                <Dropdown.Item as="button" onClick={handleEditClick} disabled={readonly}>
                    Edit
                </Dropdown.Item>
                <Dropdown.Item as="button" onClick={handleCopyClick} disabled={readonly}>
                    Copy
                </Dropdown.Item>
                <Dropdown.Divider />
                <Dropdown.Item as="button" onClick={handleDeleteClick} disabled={readonly}>
                    Delete
                </Dropdown.Item>
            </RowActionsCell>

            { showDeleteModal && (
                <DistributionDeleteModal
                    data={dataStagedForDelete}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={[data.jobId]}
                    setStatusMessage={setStatusMessage}
                    paramsConfig={paramsConfig}
                    setParamsConfig={setParamsConfig}
                />
            )}
        </>

    );
};

DistributionRowActionsCell.propTypes = {
    data: PropTypes.object,
    settings: PropTypes.shape({
        readonly: PropTypes.bool,
        paramsConfig: PropTypes.object,
        setParamsConfig: PropTypes.func
    })
};

export default DistributionRowActionsCell;
