import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import DynamicSelectInput from 'field/input/DynamicSelect';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import TableDisplay from 'field/TableDisplay';
import { createNewConfigurationRequest } from 'util/configurationRequestBuilder';

class FieldMappingField extends Component {
    constructor(props) {
        super(props);

        this.onFocusClick = this.onFocusClick.bind(this)
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
            leftHandOptions: [],
            rightHandOptions: [],
            rowKeyPair: {
                left: '',
                right: ''
            },
            progress: false,
            tableData: []
        });
    }

    componentDidMount() {
        this.onFocusClick();
    }

    componentDidUpdate(prevProps) {
        const oldValuesEmpty = FieldModelUtilities.areKeyToValuesEmpty(prevProps.currentConfig);
        const currentValuesEmpty = FieldModelUtilities.areKeyToValuesEmpty(this.props.currentConfig);
        if (oldValuesEmpty && !currentValuesEmpty) {
            this.onFocusClick();
        }
    }

    onFocusClick() {
        const {
            fieldKey, csrfToken, currentConfig, endpoint, requiredRelatedFields
        } = this.props;

        const newFieldModel = FieldModelUtilities.createFieldModelFromRequestedFields(currentConfig, requiredRelatedFields);
        const request = createNewConfigurationRequest(`/alert${endpoint}/${fieldKey}`, csrfToken, newFieldModel);
        request.then((response) => {
            if (response.ok) {
                response.json().then((data) => {
                    const { leftSideOptions, rightSideOptions } = data;
                    const leftSelectOptions = leftSideOptions.options.map((item) => {
                        return {
                            label: item.label,
                            value: item.value
                        };
                    });
                    const rightSelectOptions = rightSideOptions.options.map((item) => {
                        return {
                            label: item.label,
                            value: item.value
                        };
                    });

                    this.setState({
                        leftHandOptions: leftSelectOptions,
                        rightHandOptions: rightSelectOptions
                    });
                });
            } else {
                // FIXME implement
                // response.json()
                //     .then((data) => {
                //         this.setState({
                //             options: [],
                //             fieldError: {
                //                 severity: 'ERROR',
                //                 fieldMessage: data.message
                //             }
                //         }, this.emptyFieldValue);
                //     });
            }
        });


    }

    handleChange({ target }) {
        const { name, value } = target;

        // FIXME Call the passed onChange which is the dynamic one and mix it with this to properly fill data
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
        const { tableData } = this.state;
        if (configsToDelete) {
            const filteredTable = tableData.filter((data) => configsToDelete.includes(data.id))
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
                enableCopy={false}
                tableSearchable={false}
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
    onChange: PropTypes.func.isRequired,
    fieldKey: PropTypes.string.isRequired,
    leftSideMapping: PropTypes.string.isRequired,
    rightSideMapping: PropTypes.string.isRequired,
    endpoint: PropTypes.string.isRequired,
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
