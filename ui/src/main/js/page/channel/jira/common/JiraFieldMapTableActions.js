import React, { useState } from 'react';
import PropTypes from 'prop-types';
import FieldMappingDeleteModal from 'page/channel/jira/common/FieldMappingDeleteModal';
import FieldMappingModal from 'page/channel/jira/common/FieldMappingModal';
import Button from 'common/component/button/Button';

const JiraFieldMapTableActions = ({ data, selected, updateTableData, setSelected }) => {
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
            <Button onClick={handleCreateClick} type="button" icon="plus" text="Add Jira Field Mapping" />

            <Button onClick={handleDeleteClick} type="button" icon="trash" isDisabled={selected.length === 0} text="Delete" buttonStyle="delete" />

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
                <FieldMappingDeleteModal
                    data={data}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={selected}
                    updateTableData={updateTableData}
                    setSelected={setSelected}
                />
            )}
        </>
    );
};

JiraFieldMapTableActions.propTypes = {
    data: PropTypes.arrayOf(PropTypes.object),
    selected: PropTypes.array,
    setSelected: PropTypes.func,
    updateTableData: PropTypes.func
};

export default JiraFieldMapTableActions;
