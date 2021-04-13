import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import TableDisplay from 'field/TableDisplay';
import TextInput from 'field/input/TextInput';
import LabeledField from 'field/LabeledField';

const FieldMappingField = (props) => {
    const {
        newMappingTitle, editMappingTitle, leftSideMapping, rightSideMapping, onChange, fieldMappingKey, storedMappings
    } = props;
    const [fieldMappingRow, setFieldMappingRow] = useState({
        rowId: -1,
        fieldName: '',
        fieldValue: ''
    });
    const [tableData, setTableData] = useState([]);
    const [id, setId] = useState(0);
    const [modalError, setModalError] = useState(null);
    const [modalTitle, setModalTitle] = useState(newMappingTitle);
    useEffect(() => {
        let currentId = 0;
        let fieldMappings = [];
        if (storedMappings) {
            fieldMappings = storedMappings.map((mapping) => JSON.parse(mapping));
            fieldMappings.forEach((parsedMapping) => {
                parsedMapping.id = currentId;
                currentId += 1;
            });
        }

        setId(currentId);
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
        const valueOptions = ['{{providerName}}', '{{projectName}}', '{{projectVersion}}', '{{componentName}}', '{{componentVersion}}'];

        return (
            <div>
                <TextInput
                    name="fieldName"
                    onChange={handleChange}
                    label={leftSideMapping}
                    value={fieldName}
                />
                <TextInput
                    name="fieldValue"
                    onChange={handleChange}
                    label={rightSideMapping}
                    value={fieldValue}
                    optionList={valueOptions}
                />
            </div>
        );
    };

    const createColumns = () => [
        {
            header: 'id',
            headerLabel: 'id',
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
            isKey: false,
            hidden: false
        }
    ];

    const onEdit = (selectedRow, callback) => {
        const entireRow = tableData.filter((row) => row.id === selectedRow.id)[0];
        setFieldMappingRow({
            rowId: entireRow.id,
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
            const filteredTable = tableData.filter((data) => !configsToDelete.includes(data.id));
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
        let currentId = id;
        const mappingIndex = tableData.findIndex((mapping) => mapping.id === rowId);
        if (mappingIndex >= 0) {
            tableData[mappingIndex].fieldName = fieldName;
            tableData[mappingIndex].fieldValue = fieldValue;
        } else {
            tableData.push({
                id,
                fieldName,
                fieldValue
            });
            currentId += 1;
        }

        setTableData(tableData);
        setId(currentId);
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
        <LabeledField {...props}>
            <TableDisplay
                modalTitle={modalTitle}
                columns={createColumns()}
                newConfigFields={createNewRow}
                refreshData={() => tableData}
                onEditState={onEdit}
                onConfigSave={saveModalData}
                onConfigDelete={onDelete}
                data={tableData}
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
    editMappingTitle: PropTypes.string
};

FieldMappingField.defaultProps = {
    id: 'fieldMappingFieldId',
    storedMappings: [],
    newMappingTitle: 'Create new mapping',
    editMappingTitle: 'Edit mapping'
};

export default FieldMappingField;
