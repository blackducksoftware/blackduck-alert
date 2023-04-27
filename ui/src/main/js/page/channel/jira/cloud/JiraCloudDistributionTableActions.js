import React, { useState } from 'react';
import PropTypes from 'prop-types';
import ProviderDeleteModal from 'page/provider/ProviderDeleteModal';
import FieldMappingModal from 'page/channel/jira/cloud/FieldMappingModal';
import Button from 'common/component/button/Button';

const JiraCloudDistributionTableActions = ({ data, selected, updateTableData }) => {
    const modalOptions = {
        type: 'CREATE',
        title: 'Create a New Mapping'
    };

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);

    function handleCreateClick() {
        setShowCreateModal(true);
    }

    function handleDeleteClick() {
        setShowDeleteModal(true);
    }

    return (
        <>
            <Button onClick={handleCreateClick} type="button" icon="plus" text="Add Jira Field Mapping" style="default" />

            <Button onClick={handleDeleteClick} type="button" icon="trash" isDisabled={selected.length === 0} text="Delete" style="delete" />

            { showCreateModal && (
                <FieldMappingModal
                    tableData={data}
                    isOpen={showCreateModal}
                    toggleModal={setShowCreateModal}
                    modalOptions={modalOptions}
                    updateTableData={updateTableData}
                />
            )}

            { showDeleteModal && (
                <ProviderDeleteModal
                    data={data}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={selected}
                    setStatusMessage={setStatusMessage}
                />
            )}
        </>
    );
};

JiraCloudDistributionTableActions.propTypes = {
    data: PropTypes.arrayOf(PropTypes.object),
    selected: PropTypes.array
};

export default JiraCloudDistributionTableActions;
