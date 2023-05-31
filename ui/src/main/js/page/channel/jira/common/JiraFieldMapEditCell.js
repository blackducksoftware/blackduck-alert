import React, { useState } from 'react';
import PropTypes from 'prop-types';
import FieldMappingModal from 'page/channel/jira/common/FieldMappingModal';
import IconButton from 'common/component/button/IconButton';

const JiraFieldMapEditCell = ({ data, settings, customCallback }) => {
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

JiraFieldMapEditCell.propTypes = {
    data: PropTypes.object,
    settings: PropTypes.shape({
        tableData: PropTypes.object
    }),
    customCallback: PropTypes
};

export default JiraFieldMapEditCell;
