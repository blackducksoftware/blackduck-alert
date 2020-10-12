import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ConfigButtons from 'component/common/ConfigButtons';
import FieldsPanel from 'field/FieldsPanel';
import ConfigurationLabel from 'component/common/ConfigurationLabel';

import {
    deleteConfig,
    getConfig,
    testConfig,
    updateConfig,
    validateConfig
} from 'store/actions/globalConfiguration';
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
        this.createEmptyModel = this.createEmptyModel.bind(this);

        const { descriptor } = this.props;
        const { fields, name } = descriptor;
        const fieldKeys = FieldMapping.retrieveKeys(fields);
        const fieldModel = FieldModelUtilities.createEmptyFieldModel(fieldKeys, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, name);
        this.state = {
            currentConfig: fieldModel,
            currentDescriptor: descriptor,
            currentFields: fields,
            showTest: false
        };
    }

    componentDidMount() {
        const { currentConfig } = this.state;
        const { getConfigAction } = this.props;
        getConfigAction(currentConfig.descriptorName);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        const { currentFields, currentDescriptor, currentConfig: stateCurrentConfig } = this.state;
        const { updateStatus, updateConfigAction, currentConfig: propsCurrentConfig } = this.props;
        if (propsCurrentConfig !== prevProps.currentConfig && updateStatus === 'DELETED') {
            const newState = FieldModelUtilities.createFieldModelWithDefaults(currentFields, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, currentDescriptor.name);
            this.setState({
                currentConfig: newState
            });
        } else if (propsCurrentConfig !== prevProps.currentConfig && (updateStatus === 'FETCHED' || updateStatus === 'UPDATED')) {
            const fieldModel = FieldModelUtilities.checkModelOrCreateModelWithDefaults(propsCurrentConfig, currentFields);
            const newConfigModel = FieldModelUtilities.checkContextAndDescriptor(fieldModel, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, currentDescriptor.name);
            this.setState({
                currentConfig: newConfigModel
            });
        } else if (prevProps.updateStatus === 'VALIDATING' && updateStatus === 'VALIDATED') {
            updateConfigAction(stateCurrentConfig);
        }
    }

    createEmptyModel() {
        const { currentFields, currentDescriptor } = this.state;
        const filteredFieldKeys = currentFields.filter((field) => {
            const { type } = field;
            return type !== 'EndpointButtonField';
        }).map((field) => field.key);
        return FieldModelUtilities.createEmptyFieldModel(filteredFieldKeys, currentDescriptor.context, currentDescriptor.name);
    }

    handleTest() {
        const { currentDescriptor, currentConfig } = this.state;
        const { testFields } = currentDescriptor;
        if (testFields && testFields.length > 0) {
            this.setState({
                showTest: true
            });
        } else {
            const fieldModel = currentConfig;
            const { testConfigAction } = this.props;
            testConfigAction(fieldModel);
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
        const { validateConfigAction } = this.props;
        const filteredFieldKeys = currentFields.filter((field) => {
            const { type } = field;
            return type !== 'EndpointButtonField';
        }).map((field) => field.key);
        const newConfig = this.createEmptyModel();
        newConfig.id = currentConfig.id;
        Object.keys(currentConfig.keyToValues)
            .filter((key) => filteredFieldKeys.includes(key))
            .forEach((key) => {
                newConfig.keyToValues[key] = currentConfig.keyToValues[key];
            });
        validateConfigAction(newConfig);
    }

    handleDelete() {
        const { currentConfig } = this.state;
        const { deleteConfigAction } = this.props;
        if (currentConfig.id) {
            const emptyConfig = this.createEmptyModel();
            this.setState({
                currentConfig: emptyConfig
            }, () => deleteConfigAction(currentConfig.id));
        }
    }

    render() {
        const { currentDescriptor, showTest } = this.state;
        const {
            label, description, fields, type, testFields
        } = currentDescriptor;
        const {
            errorMessage, actionMessage, fieldErrors, testConfigAction
        } = this.props;
        const { currentConfig } = this.state;
        const { lastUpdated } = currentConfig;
        const includeTestButton = (type !== DescriptorUtilities.DESCRIPTOR_TYPE.COMPONENT) || testFields && testFields.length > 0;
        const displayTest = DescriptorUtilities.isOperationAssigned(currentDescriptor, OPERATIONS.EXECUTE) && includeTestButton;
        const displaySave = DescriptorUtilities.isOneOperationAssigned(currentDescriptor, [OPERATIONS.CREATE, OPERATIONS.WRITE]);
        const displayDelete = DescriptorUtilities.isOperationAssigned(currentDescriptor, OPERATIONS.DELETE) && (type !== DescriptorUtilities.DESCRIPTOR_TYPE.COMPONENT);
        const body = (!Array.isArray(fields) || !fields.length)
            ? (
                <div className="form-horizontal">There is no global configuration required. The configuration is handled in the distribution jobs.</div>
            ) : (
                <form className="form-horizontal" onSubmit={this.handleSubmit} noValidate>
                    <div>
                        <FieldsPanel
                            descriptorFields={fields}
                            currentConfig={currentConfig}
                            fieldErrors={fieldErrors}
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
                        sendTestMessage={testConfigAction}
                        showTestModal={showTest}
                        handleCancel={this.handleTestCancel}
                        fieldModel={currentConfig}
                        testFields={testFields}
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
                <StatusMessage
                    id="global-config-status-message"
                    errorMessage={errorMessage}
                    actionMessage={actionMessage}
                />

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
    getConfigAction: PropTypes.func.isRequired,
    updateConfigAction: PropTypes.func.isRequired,
    testConfigAction: PropTypes.func.isRequired,
    deleteConfigAction: PropTypes.func.isRequired,
    validateConfigAction: PropTypes.func.isRequired
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
const mapStateToProps = (state) => ({
    currentConfig: state.globalConfiguration.config,
    actionMessage: state.globalConfiguration.actionMessage,
    updateStatus: state.globalConfiguration.updateStatus,
    errorMessage: state.globalConfiguration.error.message,
    fieldErrors: state.globalConfiguration.error.fieldErrors
});

// Mapping redux actions -> react props
const mapDispatchToProps = (dispatch) => ({
    getConfigAction: (descriptorName) => dispatch(getConfig(descriptorName)),
    updateConfigAction: (config) => dispatch(updateConfig(config)),
    testConfigAction: (config) => dispatch(testConfig(config)),
    deleteConfigAction: (id) => dispatch(deleteConfig(id)),
    validateConfigAction: (config) => dispatch(validateConfig(config))
});

export default connect(mapStateToProps, mapDispatchToProps)(GlobalConfiguration);
