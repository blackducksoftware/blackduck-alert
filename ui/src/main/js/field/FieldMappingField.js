import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import DynamicSelectInput from 'field/input/DynamicSelect';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import TableDisplay from 'field/TableDisplay';

class FieldMappingField extends Component {
    constructor(props) {
        super(props);

        this.onFocusLeftHandClick = this.onFocusLeftHandClick.bind(this)
        this.onFocusRightHandClick = this.onFocusRightHandClick.bind(this)
        this.createNewRow = this.createNewRow.bind(this)
        this.createColumns = this.createColumns.bind(this)
        this.retrieveData = this.retrieveData.bind(this)
        this.onEdit = this.onEdit.bind(this)
        this.handleChange = this.handleChange.bind(this)
        this.saveModalData = this.saveModalData.bind(this)
        this.clearModal = this.clearModal.bind(this)
        this.retrieveOptionLabel = this.retrieveOptionLabel.bind(this)
        this.onDelete = this.onDelete.bind(this)

        this.state = ({
            leftHandOptions: [
                { value: 'LeftHandKey', label: 'Left hand' },
                { value: 'JirafieldsKey', label: 'jira fields' },
                { value: 'Option3Key', label: 'Option 3' }
            ],
            rightHandOptions: [
                { value: 'RightHandKey', label: 'Right hand' },
                { value: 'BDfieldsKey', label: 'bd fields' }
            ],
            rowKeyPair: {
                left: '',
                right: ''
            },
            progress: false,
            tableData: []
        });
    }

    componentDidMount() {
        this.onFocusLeftHandClick();
        this.onFocusRightHandClick();
    }

    componentDidUpdate(prevProps) {
        const oldValuesEmpty = FieldModelUtilities.areKeyToValuesEmpty(prevProps.currentConfig);
        const currentValuesEmpty = FieldModelUtilities.areKeyToValuesEmpty(this.props.currentConfig);
        if (oldValuesEmpty && !currentValuesEmpty) {
            this.onFocusLeftHandClick();
            this.onFocusRightHandClick();
        }
    }

    onFocusLeftHandClick() {
        // TODO retrieve left hand values
        return this.state.leftHandOptions;
    }

    onFocusRightHandClick() {
        // TODO retrieve right hand values
        return this.state.rightHandOptions;
    }

    onFocusClick(dataURL) {
        // TODO retrieves data from backend depending on URL
    }

    handleChange({ target }) {
        const { name, value } = target;

        this.setState({
            rowKeyPair: {
                ...this.state.rowKeyPair,
                [name]: value
            }
        });
    }

    retrieveOptionLabel(key, options) {
        return options.filter((option) => option.value == key)[0].label
    }

    createNewRow() {
        const { leftSideMapping, rightSideMapping } = this.props;
        const { rowKeyPair, leftHandOptions, rightHandOptions } = this.state;
        const { left, right } = rowKeyPair;
        return (
            <div>
                <DynamicSelectInput
                    id={'left'}
                    onChange={this.handleChange}
                    options={leftHandOptions}
                    label={leftSideMapping}
                    multiSelect={false}
                    value={left}
                />
                <DynamicSelectInput
                    id={'right'}
                    onChange={this.handleChange}
                    options={rightHandOptions}
                    label={rightSideMapping}
                    multiSelect={false}
                    value={right}
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

    retrieveData() {
        const { tableData, leftHandOptions, rightHandOptions } = this.state;

        return tableData.map((data) => {
            const leftLabel = this.retrieveOptionLabel(data.left, leftHandOptions)
            const rightLabel = this.retrieveOptionLabel(data.right, rightHandOptions)
            return { 'id': data.left, 'left': leftLabel, 'right': rightLabel }
        });
    }

    onEdit(selectedRow, callback) {
        console.log(`onEdit: ${JSON.stringify(selectedRow)}`)

        const entireRow = this.state.tableData.filter((row) => row.id == selectedRow.id)[0]
        this.setState({
            rowKeyPair: {
                left: entireRow.left,
                right: entireRow.right
            }
        }, callback)
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
        console.log(`delete: ${JSON.stringify(configsToDelete)}`)
        const { tableData } = this.state;
        if (configsToDelete) {
            // FIXME being passed a nested array for some reason so must get the initial array
            const filteredTable = tableData.filter((data) => configsToDelete[0].includes(data.id))
            this.setState({
                tableData: filteredTable
            })
        }
        callback();
    }

    saveModalData(callback) {
        const { tableData, rowKeyPair } = this.state;
        const { left, right } = rowKeyPair;

        tableData.push({
            id: left,
            left,
            right
        })

        this.setState({
            tableData
        });

        callback(true)
        return true;
    }

    render() {
        const { newMappingTitle } = this.props;
        return (
            <TableDisplay
                modalTitle={newMappingTitle}
                columns={this.createColumns()}
                newConfigFields={this.createNewRow}
                refreshData={this.retrieveData}
                onEditState={this.onEdit}
                onConfigSave={this.saveModalData}
                onConfigDelete={this.onDelete}
                data={this.retrieveData()}
                autoRefresh={false}
                tableRefresh={false}
                clearModalFieldState={this.clearModal}
            />
        )
    }
}

FieldMappingField.propTypes = {
    id: PropTypes.string,
    currentConfig: PropTypes.object,
    fieldKey: PropTypes.string.isRequired,
    leftSideMapping: PropTypes.string.isRequired,
    rightSideMapping: PropTypes.string.isRequired,
    newMappingTitle: PropTypes.string,
};

FieldMappingField.defaultProps = {
    id: 'fieldMappingFieldId',
    currentConfig: {},
    requiredRelatedFields: [],
    newMappingTitle: 'Create new mapping',
};

const mapStateToProps = (state) => ({
    csrfToken: state.session.csrfToken
});

export default connect(mapStateToProps, null)(FieldMappingField);
