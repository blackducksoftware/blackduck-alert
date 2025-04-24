import React, { useState } from 'react';
import PropTypes from 'prop-types';
import Modal from 'common/component/modal/Modal';
import TextInput from 'common/component/input/TextInput';
import CheckboxInput from "../../../../common/component/input/CheckboxInput";

const FieldMappingModal = ({ tableData, selectedData, selectedIndex, isOpen, toggleModal, modalOptions, updateTableData }) => {
    const [model, setModel] = useState(selectedData || { fieldName: '', fieldValue: '', treatValueAsJson: false });
    const { title, type } = modalOptions;

    function handleClose() {
        toggleModal(false);
    }

    function handleSubmit() {
        if (type === 'EDIT') {
            tableData.splice(selectedIndex, 1, model);
            updateTableData(tableData);
        }

        if (type === 'CREATE') {
            updateTableData([...tableData, model]);
        }

        handleClose();
    }

    const handleChange = (e) => {
        setModel({ ...model, [e.target.name]: e.target.value });
    };

    const handleCheckBoxChange = (e) => {
        setModel({...model, [e.target.name]:e.target.checked});
    }

    return (
        <Modal
            isOpen={isOpen}
            size="lg"
            title={title}
            closeModal={handleClose}
            handleCancel={handleClose}
            handleSubmit={() => handleSubmit()}
            submitText="Add"
        >
            <TextInput
                id="jira-field-input"
                name="fieldName"
                label="Jira Field"
                onChange={handleChange}
                value={model.fieldName}
            />
            <TextInput
                id="jira-value-input"
                name="fieldValue"
                label="Value"
                onChange={handleChange}
                value={model.fieldValue}
            />
            <CheckboxInput
                id="jira-value-json"
                name="treatValueAsJson"
                label="Treat value as JSON"
                description="If checked, Alert will treat the value as a JSON object or JSON Array.  This overrides any custom field processing that Alert does by inspecting the custom field type.  Alert will parse the value as JSON and send the JSON as the value for the custom field. "
                onChange={handleCheckBoxChange}
                isChecked={model.treatValueAsJson}
            />
        </Modal>
    );
};

FieldMappingModal.propTypes = {
    tableData: PropTypes.oneOfType([
        PropTypes.arrayOf(PropTypes.object)
    ]),
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    modalOptions: PropTypes.shape({
        type: PropTypes.string,
        submitText: PropTypes.string,
        title: PropTypes.string,
        copyDescription: PropTypes.string
    }),
    selectedData: PropTypes.oneOfType([PropTypes.object, PropTypes.array]),
    selectedIndex: PropTypes.string,
    updateTableData: PropTypes.func
};

export default FieldMappingModal;
