import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ConfigButtons from 'component/common/ConfigButtons';
import FieldsPanel from 'field/FieldsPanel';
import ConfigurationLabel from 'component/common/ConfigurationLabel';

import { deleteConfig, getConfig, testConfig, updateConfig } from 'store/actions/globalConfiguration';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import { OPERATIONS } from 'util/descriptorUtilities';
import * as FieldMapping from 'util/fieldMapping';
import StatusMessage from 'field/StatusMessage';
import ChannelTestModal from 'dynamic/ChannelTestModal';

class GlobalConfiguration extends React.Component {
    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTest = this.handleTest.bind(this);
        this.handleTestCancel = this.handleTestCancel.bind(this);
        this.handleDelete = this.handleDelete.bind(this);

        const { fields, name } = this.props.descriptor;
        const fieldKeys = FieldMapping.retrieveKeys(fields);
        const fieldModel = FieldModelUtilities.createEmptyFieldModel(fieldKeys, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, name);
        this.state = {
            currentConfig: fieldModel,
            currentDescriptor: this.props.descriptor,
            currentFields: fields,
            showTest: false,
            destinationName: ''
        };
    }

    componentDidMount() {
        const fieldModel = this.state.currentConfig;
        this.props.getConfig(fieldModel.descriptorName);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.props.currentConfig !== prevProps.currentConfig && this.props.updateStatus === 'DELETED') {
            const newState = FieldModelUtilities.createFieldModelWithDefaults(this.state.currentFields, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, this.state.currentDescriptor.name);
            this.setState({
                currentConfig: newState
            });
        } else if (this.props.currentConfig !== prevProps.currentConfig && (this.props.updateStatus === 'FETCHED' || this.props.updateStatus === 'UPDATED')) {
            const fieldModel = FieldModelUtilities.checkModelOrCreateModelWithDefaults(this.props.currentConfig, this.state.currentFields);
            this.setState({
                currentConfig: fieldModel
            });
        }
    }

    handleTest() {
        const { testFieldLabel } = this.state.currentDescriptor;
        if (testFieldLabel) {
            this.setState({
                showTest: true,
                destinationName: testFieldLabel
            });
        } else {
            const fieldModel = this.state.currentConfig;
            this.props.testConfig(fieldModel, '');
        }
    }

    handleTestCancel() {
        this.setState({
            showTest: false
        });
    }

    handleSubmit(event) {
        event.preventDefault();
        event.stopPropagation();
        const { currentFields, currentConfig } = this.state;
        const filteredFieldKeys = currentFields.filter((field) => {
            const { type } = field;
            return type !== 'EndpointButtonField';
        }).map(field => field.key);
        const newConfig = FieldModelUtilities.createEmptyFieldModel(filteredFieldKeys, currentConfig.context, currentConfig.descriptorName);
        newConfig.id = currentConfig.id;
        Object.keys(currentConfig.keyToValues)
        .filter(key => filteredFieldKeys.includes(key))
        .forEach((key) => {
            newConfig.keyToValues[key] = currentConfig.keyToValues[key];
        });
        this.props.updateConfig(currentConfig);
    }

    handleDelete() {
        const { currentConfig } = this.state;
        if (currentConfig.id) {
            this.props.deleteConfig(currentConfig.id);
        }
    }

    render() {
        const {
            label, description, fields, type, testFields
        } = this.state.currentDescriptor;
        const { errorMessage, actionMessage } = this.props;
        const { currentConfig } = this.state;
        const { lastUpdated } = currentConfig;
        const displayTest = DescriptorUtilities.isOperationAssigned(this.state.currentDescriptor, OPERATIONS.EXECUTE) && testFields && testFields.length > 0;
        const displaySave = DescriptorUtilities.isOneOperationAssigned(this.state.currentDescriptor, [OPERATIONS.CREATE, OPERATIONS.WRITE]);
        const displayDelete = DescriptorUtilities.isOperationAssigned(this.state.currentDescriptor, OPERATIONS.DELETE) && (type !== DescriptorUtilities.DESCRIPTOR_TYPE.COMPONENT);
        const body = (!Array.isArray(fields) || !fields.length) ?
            (
                <div className="form-horizontal">There is no global configuration required. The configuration is handled in the distribution jobs.</div>
            ) : (
                <form className="form-horizontal" onSubmit={this.handleSubmit} noValidate>
                    <div>
                        <FieldsPanel
                            descriptorFields={fields}
                            currentConfig={currentConfig}
                            fieldErrors={this.props.fieldErrors}
                            handleChange={this.handleChange}
                            self={this}
                            stateName="currentConfig"
                        />
                    </div>
                    <ConfigButtons
                        includeSave={displaySave}
                        includeTest={displayTest}
                        includeDelete={displayDelete}
                        type="submit"
                        onTestClick={this.handleTest}
                        onDeleteClick={this.handleDelete}
                        confirmDeleteMessage="Are you sure you want to delete the configuration?"
                    />
                    <ChannelTestModal
                        sendTestMessage={this.props.testConfig}
                        showTestModal={this.state.showTest}
                        handleCancel={this.handleTestCancel}
                        destinationName={this.state.destinationName}
                        fieldModel={currentConfig}
                    />
                </form>
            );

        return (
            <div>
                <ConfigurationLabel
                    configurationName={label}
                    description={description}
                    lastUpdated={lastUpdated}
                />
                <StatusMessage errorMessage={errorMessage} actionMessage={actionMessage} />

                {body}
            </div>
        );
    }
}

// Used for compile/validation of properties
GlobalConfiguration.propTypes = {
    descriptor: PropTypes.object.isRequired,
    currentConfig: PropTypes.object,
    fieldErrors: PropTypes.object,
    errorMessage: PropTypes.string,
    actionMessage: PropTypes.string,
    updateStatus: PropTypes.string,
    getConfig: PropTypes.func.isRequired,
    updateConfig: PropTypes.func.isRequired,
    testConfig: PropTypes.func.isRequired,
    deleteConfig: PropTypes.func.isRequired
};

// Default values
GlobalConfiguration.defaultProps = {
    currentConfig: {},
    errorMessage: null,
    actionMessage: null,
    updateStatus: null,
    fieldErrors: {}
};

// Mapping redux state -> react props
const mapStateToProps = state => ({
    currentConfig: state.globalConfiguration.config,
    actionMessage: state.globalConfiguration.actionMessage,
    updateStatus: state.globalConfiguration.updateStatus,
    errorMessage: state.globalConfiguration.error.message,
    fieldErrors: state.globalConfiguration.error.fieldErrors
});

// Mapping redux actions -> react props
const mapDispatchToProps = dispatch => ({
    getConfig: descriptorName => dispatch(getConfig(descriptorName)),
    updateConfig: config => dispatch(updateConfig(config)),
    testConfig: (config, destination) => dispatch(testConfig(config, destination)),
    deleteConfig: id => dispatch(deleteConfig(id))
});

export default connect(mapStateToProps, mapDispatchToProps)(GlobalConfiguration);
