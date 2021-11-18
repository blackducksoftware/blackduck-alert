import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import TableDisplay from 'common/table/TableDisplay';
import TextInput from 'common/input/TextInput';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/input/field/LabeledField';

const FieldMappingField = ({
    id,
    description,
    editMappingTitle,
    errorName,
    errorValue,
    fieldMappingKey,
    label,
    labelClass,
    leftSideMapping,
    newMappingTitle,
    onChange,
    readonly,
    required,
    rightSideMapping,
    showDescriptionPlaceHolder,
    storedMappings
}) => {
    const [fieldMappingRow, setFieldMappingRow] = useState({
        rowId: -1,
        fieldName: '',
        fieldValue: ''
    });
    const [tableData, setTableData] = useState([]);
    const [tableId, setTableId] = useState(0);
    const [modalError, setModalError] = useState(null);
    const [modalTitle, setModalTitle] = useState(newMappingTitle);
    useEffect(() => {
        let currenttableId = 0;
        let fieldMappings = [];
        if (storedMappings) {
            fieldMappings = storedMappings.map((mapping) => JSON.parse(mapping));
            fieldMappings.forEach((parsedMapping) => {
                parsedMapping.tableId = currenttableId;
                currenttableId += 1;
            });
        }

        setTableId(currenttableId);
        setTableData(fieldMappings);
    }, []);

    const handleChange = ({ target }) => {
        const { name, value } = target;
        setFieldMappingRow({
            ...fieldMappingRow,
            [name]: value

        });
    };

    const createNewRow = () => {
        const { fieldName, fieldValue } = fieldMappingRow;
        const valueOptions = ['{{providerType}}', '{{projectName}}', '{{projectVersion}}', '{{componentName}}', '{{componentVersion}}', '{{componentUsage}}', '{{componentLicense}}', '{{severity}}', '{{policyCategory}}', '{{shortTermUpgradeGuidance}}', '{{longTermUpgradeGuidance}}'];

        return (
            <div>
                <TextInput
                    name="fieldName"
                    onChange={handleChange}
                    label={leftSideMapping}
                    value={fieldName}
                    readOnly={readonly}
                />
                <TextInput
                    name="fieldValue"
                    onChange={handleChange}
                    label={rightSideMapping}
                    value={fieldValue}
                    readOnly={readonly}
                    optionList={valueOptions}
                />
            </div>
        );
    };

    const createColumns = () => [
        {
            header: 'tableId',
            headerLabel: 'tableId',
            isKey: true,
            hidden: true
        },
        {
            header: 'fieldName',
            headerLabel: leftSideMapping,
            isKey: false,
            hidden: false
        },
        {
            header: 'fieldValue',
            headerLabel: rightSideMapping,
            hidden: false
        }
    ];

    const onEdit = (selectedRow, callback) => {
        const entireRow = tableData.filter((row) => row.tableId === selectedRow.tableId)[0];
        setFieldMappingRow({
            rowId: entireRow.tableId,
            fieldName: entireRow.fieldName,
            fieldValue: entireRow.fieldValue
        });
        setModalTitle(editMappingTitle);
        callback();
    };

    const clearModal = () => {
        setFieldMappingRow({
            rowId: -1,
            fieldName: '',
            fieldValue: ''
        });
    };

    const onDelete = (configsToDelete, callback) => {
        if (configsToDelete) {
            const filteredTable = tableData.filter((data) => !configsToDelete.includes(data.tableId));
            setTableData(filteredTable);
            const fieldMappingValue = filteredTable.map((mappingEntry) => JSON.stringify(mappingEntry));
            onChange({
                target: {
                    name: fieldMappingKey,
                    value: fieldMappingValue
                }
            });
        }
        callback();
    };

    const saveModalData = (callback) => {
        const { rowId, fieldName, fieldValue } = fieldMappingRow;
        let currenttableId = tableId;
        const mappingIndex = tableData.findIndex((mapping) => mapping.tableId === rowId);
        if (mappingIndex >= 0) {
            tableData[mappingIndex].fieldName = fieldName;
            tableData[mappingIndex].fieldValue = fieldValue;
        } else {
            tableData.push({
                tableId,
                fieldName,
                fieldValue
            });
            currenttableId += 1;
        }

        setTableData(tableData);
        setTableId(currenttableId);
        setModalError(null);
        setModalTitle(newMappingTitle);

        const fieldMappingValue = tableData.map((mappingEntry) => JSON.stringify(mappingEntry));
        onChange({
            target: {
                name: fieldMappingKey,
                value: fieldMappingValue
            }
        });

        callback(true);
        return true;
    };
    return (
        <LabeledField
            id={id}
            description={description}
            errorName={errorName}
            errorValue={errorValue}
            label={label}
            labelClass={labelClass}
            required={required}
            showDescriptionPlaceHolder={showDescriptionPlaceHolder}
        >
            <TableDisplay
                modalTitle={modalTitle}
                columns={createColumns()}
                newConfigFields={createNewRow}
                newButton={!readonly}
                deleteButton={!readonly}
                saveButton={!readonly}
                refreshData={() => tableData}
                onEditState={onEdit}
                onConfigSave={saveModalData}
                onConfigDelete={onDelete}
                data={tableData}
                enableEdit={!readonly}
                enableCopy={false}
                tableSearchable={false}
                autoRefresh={false}
                tableRefresh={false}
                clearModalFieldState={clearModal}
                errorDialogMessage={modalError}
            />
        </LabeledField>
    );
};

FieldMappingField.propTypes = {
    id: PropTypes.string,
    onChange: PropTypes.func.isRequired,
    storedMappings: PropTypes.array,
    fieldMappingKey: PropTypes.string.isRequired,
    leftSideMapping: PropTypes.string.isRequired,
    rightSideMapping: PropTypes.string.isRequired,
    newMappingTitle: PropTypes.string,
    editMappingTitle: PropTypes.string,
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    readonly: PropTypes.bool,
    required: PropTypes.bool,
    showDescriptionPlaceHolder: PropTypes.bool
};

FieldMappingField.defaultProps = {
    id: 'fieldMappingFieldId',
    storedMappings: [],
    newMappingTitle: 'Create new mapping',
    editMappingTitle: 'Edit mapping',
    readonly: false,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT
};

export default FieldMappingField;
