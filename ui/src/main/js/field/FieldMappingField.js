import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import TableDisplay from 'field/TableDisplay';
import TextInput from 'field/input/TextInput';
import LabeledField from 'field/LabeledField';

class FieldMappingField extends Component {
    constructor(props) {
        super(props);

        this.createNewRow = this.createNewRow.bind(this);
        this.createColumns = this.createColumns.bind(this);
        this.onEdit = this.onEdit.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.saveModalData = this.saveModalData.bind(this);
        this.clearModal = this.clearModal.bind(this);
        this.onDelete = this.onDelete.bind(this);

        let currentId = 0;
        let fieldMappings = [];
        const { storedMappings } = props;
        if (storedMappings) {
            fieldMappings = storedMappings.map((mapping) => JSON.parse(mapping));
            fieldMappings.forEach((parsedMapping) => {
                parsedMapping.id = currentId;
                currentId += 1;
            });
        }

        this.state = ({
            fieldMappingRow: {
                rowId: -1,
                fieldName: '',
                fieldValue: ''
            },
            tableData: fieldMappings,
            id: currentId,
            modalError: null
        });
    }

    handleChange({ target }) {
        const { name, value } = target;
        this.setState({
            fieldMappingRow: {
                ...this.state.fieldMappingRow,
                [name]: value
            }
        });
    }

    createNewRow() {
        const { leftSideMapping, rightSideMapping } = this.props;
        const { fieldName, fieldValue } = this.state.fieldMappingRow;
        const valueOptions = ['{{providerName}}', '{{projectName}}', '{{projectVersion}}', '{{componentName}}', '{{componentVersion}}'];

        return (
            <div>
                <TextInput
                    name="fieldName"
                    onChange={this.handleChange}
                    label={leftSideMapping}
                    value={fieldName}
                />
                <TextInput
                    name="fieldValue"
                    onChange={this.handleChange}
                    label={rightSideMapping}
                    value={fieldValue}
                    optionList={valueOptions}
                />
            </div>
        );
    }

    createColumns() {
        const { leftSideMapping, rightSideMapping } = this.props;
        return [
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
    }

    onEdit(selectedRow, callback) {
        const entireRow = this.state.tableData.filter((row) => row.id === selectedRow.id)[0];
        this.setState({
            fieldMappingRow: {
                rowId: entireRow.id,
                fieldName: entireRow.fieldName,
                fieldValue: entireRow.fieldValue
            }
        }, callback);
    }

    clearModal() {
        this.setState({
            fieldMappingRow: {
                rowId: -1,
                fieldName: '',
                fieldValue: ''
            }
        });
    }

    onDelete(configsToDelete, callback) {
        const { tableData } = this.state;
        if (configsToDelete) {
            const filteredTable = tableData.filter((data) => !configsToDelete.includes(data.id));
            this.setState({
                tableData: filteredTable
            });
            const { onChange, fieldMappingKey } = this.props;
            const fieldMappingValue = filteredTable.map((mappingEntry) => JSON.stringify(mappingEntry));
            onChange({
                target: {
                    name: fieldMappingKey,
                    value: fieldMappingValue
                }
            });
        }
        callback();
    }

    saveModalData(callback) {
        const { tableData, fieldMappingRow, id } = this.state;
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

        this.setState({
            tableData,
            id: currentId,
            modalError: null
        });

        const { onChange, fieldMappingKey } = this.props;
        const fieldMappingValue = tableData.map((mappingEntry) => JSON.stringify(mappingEntry));
        onChange({
            target: {
                name: fieldMappingKey,
                value: fieldMappingValue
            }
        });

        callback(true);
        return true;
    }

    render() {
        const { newMappingTitle } = this.props;
        const { tableData, modalError } = this.state;
        const table = (
            <TableDisplay
                modalTitle={newMappingTitle}
                columns={this.createColumns()}
                newConfigFields={this.createNewRow}
                refreshData={() => tableData}
                onEditState={this.onEdit}
                onConfigSave={this.saveModalData}
                onConfigDelete={this.onDelete}
                data={tableData}
                enableCopy={false}
                tableSearchable={false}
                autoRefresh={false}
                tableRefresh={false}
                clearModalFieldState={this.clearModal}
                errorDialogMessage={modalError}
            />
        );
        return (
            <LabeledField field={table} {...this.props} />
        );
    }
}

FieldMappingField.propTypes = {
    id: PropTypes.string,
    onChange: PropTypes.func.isRequired,
    storedMappings: PropTypes.array,
    fieldMappingKey: PropTypes.string.isRequired,
    leftSideMapping: PropTypes.string.isRequired,
    rightSideMapping: PropTypes.string.isRequired,
    newMappingTitle: PropTypes.string
};

FieldMappingField.defaultProps = {
    id: 'fieldMappingFieldId',
    storedMappings: [],
    newMappingTitle: 'Create new mapping'
};

const mapStateToProps = (state) => ({
    csrfToken: state.session.csrfToken
});

export default connect(mapStateToProps, null)(FieldMappingField);
