import React, { useState } from 'react';
import PropTypes from 'prop-types';
import FieldMappingModal from 'page/channel/jira/cloud/FieldMappingModal';
import IconButton from 'common/component/button/IconButton';

const JiraCloudEditCell = ({ data, settings, customCallback }) => {
    const [showModal, setShowModal] = useState(false);
    const { tableData } = settings;
    const selectedIndex = tableData.indexOf(data);

    const modalOptions = {
        type: 'EDIT',
        title: 'Edit Mapping'
    };

    function handleClick() {
        setShowModal(true);
    }
    
    return (
        <>
            <IconButton icon="pencil-alt" onClick={() => handleClick()} />

            { showModal && (
                <FieldMappingModal
                    tableData={tableData}
                    selectedData={data}
                    selectedIndex={selectedIndex}
                    updateTableData={customCallback}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    modalOptions={modalOptions}
                />
            )}
        </>

    );
};

JiraCloudEditCell.propTypes = {
    data: PropTypes.object
};

export default JiraCloudEditCell;
