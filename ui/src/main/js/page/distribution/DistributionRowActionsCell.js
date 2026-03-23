import React, { useState } from 'react';
import PropTypes from 'prop-types';
import DistributionDeleteModal from 'page/distribution/DistributionDeleteModal';
import StatusMessage from 'common/component/StatusMessage';
import Dropdown from 'react-bootstrap/Dropdown';
import RowActionsCell from '../../common/component/table/cell/RowActionsCell';
import { useHistory } from 'react-router-dom/cjs/react-router-dom';
import { DISTRIBUTION_URLS } from 'page/distribution/DistributionModel';

const DistributionRowActionsCell = ({ data, settings }) => {
    const history = useHistory();
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();
    const dataStagedForDelete = { models: [data] };


    function handleClick(type) {
        const url = type === 'edit'
            ? `${DISTRIBUTION_URLS.distributionConfigUrl}/${data.jobId}`
            : `${DISTRIBUTION_URLS.distributionConfigCopyUrl}/${data.jobId}`;

        history.push(url);
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
                <Dropdown.Item as="button" onClick={() => handleClick('edit')} disabled={settings.readonly}>
                    Edit
                </Dropdown.Item>
                <Dropdown.Item onClick={() => handleClick('copy')} disabled={settings.readonly}>
                    Copy
                </Dropdown.Item>
                <Dropdown.Divider />
                <Dropdown.Item onClick={handleDeleteClick} disabled={settings.readonly}>
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
                    setSelected={() => console.log('setting')}
                />
            )}
        </>

    );
};

DistributionRowActionsCell.propTypes = {
    data: PropTypes.object,
    settings: PropTypes.shape({
        readonly: PropTypes.bool
    })
};

export default DistributionRowActionsCell;
