import React, { Component } from 'react';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import ConfigurationLabel from 'component/common/ConfigurationLabel';
import PropTypes from "prop-types";
import { clearConfigFieldErrors, deleteConfig, getAllConfigs, testConfig, updateConfig } from 'store/actions/globalConfiguration';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import TableDisplay from 'field/TableDisplay';
import FieldsPanel from 'field/FieldsPanel';

const ProviderCommonKeys = {
    KEY_NAME: 'provider.common.config.name',
    KEY_ENABLED: 'provider.common.config.enabled'
}

class ProviderTable extends Component {
    constructor(props) {
        super(props);
        this.createTableData = this.createTableData.bind(this);
        this.retrieveData = this.retrieveData.bind(this);
        this.clearModalFieldState = this.clearModalFieldState.bind(this);
        this.createColumns = this.createColumns.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.onSave = this.onSave.bind(this);
        this.onConfigClose = this.onConfigClose.bind(this);
        this.onDelete = this.onDelete.bind(this);
        this.createModalFields = this.createModalFields.bind(this);
        this.onEdit = this.onEdit.bind(this);
        this.combineModelWithDefaults = this.combineModelWithDefaults.bind(this);


        this.state = {
            descriptor: null,
            providerConfig: {}
        };
    }

    componentDidMount() {
        const descriptor = this.props.descriptors.find(descriptor => descriptor.name === DescriptorUtilities.DESCRIPTOR_NAME.PROVIDER_BLACKDUCK)
        if (descriptor) {
            const emptyConfig = FieldModelUtilities.createFieldModelWithDefaults(descriptor.fields, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, descriptor.name);
            this.setState({
                descriptor,
                providerConfig: emptyConfig
            });
            this.props.getAllConfigs(descriptor.name);
        }
    }

    combineModelWithDefaults(providerConfig) {
        const { descriptor } = this.state;
        if (descriptor) {
            const emptyConfig = FieldModelUtilities.createFieldModelWithDefaults(descriptor.fields, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, descriptor.name);
            const updatedFieldModel = FieldModelUtilities.combineFieldModels(emptyConfig, providerConfig);
            if (providerConfig.id) {
                updatedFieldModel.id = providerConfig.id;
            }
            return updatedFieldModel;
        }
        return {};
    }

    createColumns() {
        return [
            {
                header: 'id',
                headerLabel: 'Id',
                isKey: true,
                hidden: true
            },
            {
                header: 'name',
                headerLabel: 'Alias',
                isKey: false,
                hidden: false
            },
            {
                header: 'createdAt',
                headerLabel: 'Created At',
                isKey: false,
                hidden: false
            },
            {
                header: 'lastUpdated',
                headerLabel: 'Last Updated',
                isKey: false,
                hidden: false
            },
            {
                header: 'enabled',
                headerLabel: 'Enabled',
                isKey: false,
                hidden: false
            }
        ];
    }

    onConfigClose() {
        this.props.clearFieldErrors();
    }

    clearModalFieldState() {
        if (this.state.providerConfig && Object.keys(this.state.providerConfig).length > 0) {
            this.setState({
                providerConfig: {}
            });
        }
    }

    retrieveData() {
        const { descriptor } = this.state;
        if (descriptor) {
            this.props.getAllConfigs(descriptor.name);
        }
    }

    handleChange(e) {
        const { name, value, type, checked } = e.target;
        const { providerConfig } = this.state;

        const updatedValue = type === 'checkbox' ? checked.toString().toLowerCase() === 'true' : value;
        const newProviderConfig = Object.assign(providerConfig, FieldModelUtilities.updateFieldModelSingleValue(providerConfig, name, updatedValue));

        this.setState({
            providerConfig: newProviderConfig
        });
    }

    onSave() {
        const { providerConfig } = this.state;
        const configToUpdate = this.combineModelWithDefaults(providerConfig);
        this.props.updateConfig(configToUpdate);
        return true;
    }

    onDelete(configsToDelete) {
        if (configsToDelete) {
            configsToDelete.forEach(configId => {
                this.props.deleteConfig(configId);
            });
        }
        this.retrieveData();
    }

    createModalFields() {
        const { providerConfig, descriptor } = this.state;
        const { fieldErrors } = this.props;
        const newConfig = this.combineModelWithDefaults(providerConfig);
        if (descriptor) {
            return (
                <div>
                    <FieldsPanel
                        descriptorFields={descriptor.fields}
                        self={this}
                        fieldErrors={fieldErrors}
                        stateName={'providerConfig'}
                        currentConfig={newConfig}
                    />
                </div>
            );
        }
        return <div />;
    }

    onEdit(selectedRow) {
        const { id } = selectedRow;
        const { providerConfigs } = this.props;
        const selectedConfig = providerConfigs.find(config => config.id === id);
        this.setState({
            providerConfig: selectedConfig
        });
    }

    checkJobPermissions(operation) {
        const { descriptors } = this.props;
        if (descriptors) {
            const descriptorList = DescriptorUtilities.findDescriptorByNameAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_NAME.PROVIDER_BLACKDUCK, DescriptorUtilities.CONTEXT_TYPE.GLOBAL);
            if (descriptorList) {
                return descriptorList.some(descriptor => DescriptorUtilities.isOperationAssigned(descriptor, operation));
            }
        }
        return false;
    }

    createTableData(providerConfigs) {
        if (!providerConfigs || providerConfigs.length <= 0) {
            return null;
        }
        const tableData = [];
        providerConfigs.forEach((providerConfig) => {
            if (FieldModelUtilities.hasAnyValuesExcludingId(providerConfig)) {
                const id = providerConfig.id;
                const name = FieldModelUtilities.getFieldModelSingleValue(providerConfig, ProviderCommonKeys.KEY_NAME);
                const enabled = FieldModelUtilities.getFieldModelSingleValue(providerConfig, ProviderCommonKeys.KEY_ENABLED);
                const createdAt = providerConfig.createdAt;
                const lastUpdated = providerConfig.lastUpdated;
                const entry = Object.assign({}, {
                    id,
                    name,
                    createdAt,
                    lastUpdated,
                    enabled
                });
                tableData.push(entry);
            }
        });
        return tableData;
    }

    render() {
        const { descriptor } = this.state;
        const { providerConfigs, inProgress, fetching, fieldErrors, errorMessage } = this.props;
        const descriptorHeader = descriptor && (
            <div>
                <ConfigurationLabel configurationName={descriptor.label} description={descriptor.description} />
            </div>
        );

        const canCreate = this.checkJobPermissions(DescriptorUtilities.OPERATIONS.CREATE);
        const canDelete = this.checkJobPermissions(DescriptorUtilities.OPERATIONS.DELETE);
        const data = this.createTableData(providerConfigs);
        const hasFieldErrors = fieldErrors && Object.keys(fieldErrors).length > 0;
        return (
            <div>
                {descriptorHeader}
                <div>
                    <TableDisplay
                        newConfigFields={this.createModalFields}
                        modalTitle="Black Duck Provider"
                        clearModalFieldState={this.clearModalFieldState}
                        onConfigSave={this.onSave}
                        onConfigDelete={this.onDelete}
                        onConfigClose={this.onConfigClose}
                        refreshData={this.retrieveData}
                        editState={this.onEdit}
                        data={data}
                        columns={this.createColumns()}
                        newButton={canCreate}
                        deleteButton={canDelete}
                        hasFieldErrors={hasFieldErrors}
                        errorDialogMessage={errorMessage}
                        inProgress={inProgress}
                        fetching={fetching}
                    />
                </div>
            </div>
        );
    }
}


ProviderTable.propTypes = {
    autoRefresh: PropTypes.bool,
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired,
    descriptorFetching: PropTypes.bool.isRequired,
    inProgress: PropTypes.bool.isRequired,
    providerConfigs: PropTypes.arrayOf(PropTypes.object).isRequired,
    errorMessage: PropTypes.string,
    fieldErrors: PropTypes.object,
    getAllConfigs: PropTypes.func.isRequired
};

ProviderTable.defaultProps = {
    autoRefresh: true,
    descriptors: [],
    inProgress: false,
    errorMessage: '',
    fieldErrors: {}

};

const mapStateToProps = state => ({
    descriptors: state.descriptors.items,
    descriptorFetching: state.descriptors.fetching,
    providerConfigs: state.globalConfiguration.allConfigs,
    errorMessage: state.globalConfiguration.error.message,
    fieldErrors: state.globalConfiguration.error.fieldErrors
});

const mapDispatchToProps = dispatch => ({
    getAllConfigs: descriptorName => dispatch(getAllConfigs(descriptorName)),
    updateConfig: config => dispatch(updateConfig(config)),
    testConfig: (config, destination) => dispatch(testConfig(config, destination)),
    deleteConfig: id => dispatch(deleteConfig(id)),
    clearFieldErrors: () => dispatch(clearConfigFieldErrors())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(ProviderTable));
