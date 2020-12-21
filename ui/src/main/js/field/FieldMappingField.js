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

        this.state = ({
            rowKeyPair: {
                left: '',
                right: ''
            },
            tableData: [],
            id: 0
        });
    }

    // FIXME Verify if didMount and didUpdate are necessary now that we use text fields
    // componentDidMount() {
    // }
    //
    // componentDidUpdate(prevProps) {
    //     const oldValuesEmpty = FieldModelUtilities.areKeyToValuesEmpty(prevProps.currentConfig);
    //     const currentValuesEmpty = FieldModelUtilities.areKeyToValuesEmpty(this.props.currentConfig);
    //     if (oldValuesEmpty && !currentValuesEmpty) {
    //     }
    // }

    handleChange({ target }) {
        const { name, value } = target;
        this.setState({
            rowKeyPair: {
                ...this.state.rowKeyPair,
                [name]: value
            }
        });
    }

    createNewRow() {
        const { leftSideMapping, rightSideMapping } = this.props;
        const { left, right } = this.state.rowKeyPair;
        const valueOptions = ['{{providerName}}', '{{projectName}}', '{{projectVersion}}', '{{componentName}}', '{{componentVersion}}'];

        return (
            <div>
                <TextInput
                    name="left"
                    onChange={this.handleChange}
                    label={leftSideMapping}
                    value={left}
                />
                <TextInput
                    name="right"
                    onChange={this.handleChange}
                    label={rightSideMapping}
                    value={right}
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
                header: 'left',
                headerLabel: leftSideMapping,
                isKey: false,
                hidden: false
            },
            {
                header: 'right',
                headerLabel: rightSideMapping,
                isKey: false,
                hidden: false
            }
        ];
    }

    // FIXME edit adds a new row
    onEdit(selectedRow, callback) {
        const entireRow = this.state.tableData.filter((row) => row.id === selectedRow.id)[0];
        this.setState({
            rowKeyPair: {
                left: entireRow.left,
                right: entireRow.right
            }
        }, callback);
    }

    clearModal() {
        this.setState({
            rowKeyPair: {
                left: '',
                right: ''
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
        }
        callback();
    }

    saveModalData(callback) {
        const { tableData, rowKeyPair, id } = this.state;
        const { left, right } = rowKeyPair;

        tableData.push({
            id,
            left,
            right
        });

        this.setState({
            tableData,
            id: id + 1
        });

        const { onChange, fieldKey } = this.props;
        // TODO Function that adds content to the field model. If you want to save the data differently, modify this
        onChange({
            target: {
                name: fieldKey,
                value: tableData
            }
        });

        callback(true);
        return true;
    }

    render() {
        const { newMappingTitle } = this.props;
        const { tableData } = this.state;
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
            />
        );
        return (
            <LabeledField field={table} {...this.props} />
        );
    }
}

FieldMappingField.propTypes = {
    id: PropTypes.string,
    currentConfig: PropTypes.object,
    onChange: PropTypes.func.isRequired,
    fieldKey: PropTypes.string.isRequired,
    leftSideMapping: PropTypes.string.isRequired,
    rightSideMapping: PropTypes.string.isRequired,
    newMappingTitle: PropTypes.string
};

FieldMappingField.defaultProps = {
    id: 'fieldMappingFieldId',
    currentConfig: {},
    newMappingTitle: 'Create new mapping'
};

const mapStateToProps = (state) => ({
    csrfToken: state.session.csrfToken
});

const mapDispatchToProps = (dispatch) => ({});

export default connect(mapStateToProps, mapDispatchToProps)(FieldMappingField);
