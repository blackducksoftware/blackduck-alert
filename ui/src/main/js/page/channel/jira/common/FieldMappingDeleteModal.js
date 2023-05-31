import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import Modal from 'common/component/modal/Modal';
import Card from 'common/component/Card';

const useStyles = createUseStyles({
    deleteConfirmMessage: {
        margin: [0, 0, '20px', '30px'],
        fontSize: '16px',
        fontWeight: 'bold'
    },
    cardContainer: {
        display: 'flex',
        marginLeft: '50px'
    },
    deleteOptions: {
        overflowY: 'auto'
    }
});

function getStagedForDelete(data, selected) {
    const staged = data.filter((fieldModel) => selected.includes(fieldModel.fieldName));
    return staged.map((fieldModel) => ({ ...fieldModel, staged: true }));
}

const FieldMappingDeleteModal = ({ isOpen, toggleModal, data, selected, setSelected, updateTableData }) => {
    const classes = useStyles();

    const [selectedFieldModels, setSelectedFieldModels] = useState(getStagedForDelete(data, selected));
    const isMultiDelete = selectedFieldModels.length > 1;

    function handleClose() {
        toggleModal(false);
    }

    function handleDelete() {
        const updatedData = data.filter((fieldMapping) => !selected.includes(fieldMapping.fieldName));
        updateTableData(updatedData);
        setSelected([]);
        handleClose();
    }

    useEffect(() => {
        setSelectedFieldModels(getStagedForDelete(data, selected));
    }, [selected]);

    function toggleSelect(selection) {
        const toggledFieldModels = selectedFieldModels.map((fieldModel) => {
            if (fieldModel.fieldName === selection.fieldName) {
                return { ...fieldModel, staged: !fieldModel.staged };
            }
            return fieldModel;
        });

        setSelectedFieldModels(toggledFieldModels);
    }

    return (
        <>
            <Modal
                isOpen={isOpen}
                size="sm"
                title={isMultiDelete ? 'Delete Jira Fields' : 'Delete Jira Field'}
                closeModal={handleClose}
                handleCancel={handleClose}
                handleSubmit={handleDelete}
                submitText="Delete"
            >
                <div className={classes.deleteConfirmMessage}>
                    { isMultiDelete ? 'Are you sure you want to delete these Jira Field?' : 'Are you sure you want to delete this Jira Field?' }
                </div>
                <div>
                    { selectedFieldModels?.map((fieldModel) => (
                        <div className={classes.cardContainer} key={fieldModel.fieldName}>
                            <input type="checkbox" checked={fieldModel.staged} onChange={() => toggleSelect(fieldModel)} />
                            <Card icon={['fab', 'jira']} label={fieldModel.fieldName} description={fieldModel.fieldValue} />
                        </div>
                    ))}
                </div>
            </Modal>
        </>

    );
};

FieldMappingDeleteModal.propTypes = {
    data: PropTypes.object,
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    selected: PropTypes.array,
    setSelected: PropTypes.func,
    updateTableData: PropTypes.func
};

export default FieldMappingDeleteModal;
