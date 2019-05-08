import { connect } from 'react-redux';
import React, { Component } from 'react';
import { getInitialSystemSetup, saveInitialSystemSetup } from 'store/actions/system';
import PropTypes from 'prop-types';
import StatusMessage from 'field/StatusMessage';
import FieldsPanel from 'field/FieldsPanel';
import ConfigButtons from 'component/common/ConfigButtons';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import * as FieldMapping from 'util/fieldMapping';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import { getDescriptors } from 'store/actions/descriptors';

class SetupPage extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);

        this.state = {
            settingsData: {},
            fields: [],
            fieldKeys: []
        };
    }

    componentDidMount() {
        this.props.getDescriptors();
        this.props.getSettings();
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevProps.fetchingSetupStatus !== 'SYSTEM_SETUP_FETCHED' && this.props.fetchingSetupStatus === 'SYSTEM_SETUP_FETCHED') {
            const newState = FieldModelUtilities.checkModelOrCreateEmpty(this.props.currentSettingsData, this.state.fieldKeys);
            this.setState({
                settingsData: newState
            });
        }
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked.toString() : target.value;
        const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.settingsData, target.name, value);
        this.setState({
            settingsData: newState
        });
    }

    handleSubmit(evt) {
        evt.preventDefault();
        this.props.saveSettings(this.state.settingsData);
    }

    render() {
        const { errorMessage, actionMessage } = this.props;
        const settingsDescriptor = DescriptorUtilities.findDescriptorByNameAndContext(this.props.descriptors, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_SETTINGS, DescriptorUtilities.CONTEXT_TYPE.GLOBAL)[0];
        const { fields } = settingsDescriptor;
        const fieldKeys = FieldMapping.retrieveKeys(fields);
        const settingsData = FieldModelUtilities.createEmptyFieldModel(fieldKeys, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_SETTINGS);
        const saving = this.props.updateStatus === 'UPDATING' || this.props.updateStatus === 'FETCHING';

        return (
            <div className="settingsWrapper">
                <div className="settingsContainer">
                    <div className="settingsBox">
                        <StatusMessage errorMessage={errorMessage} actionMessage={actionMessage} />
                        <form method="POST" className="form-horizontal loginForm" onSubmit={this.handleSubmit} noValidate>
                            <FieldsPanel descriptorFields={fields} currentConfig={settingsData} fieldErrors={this.props.fieldErrors} handleChange={this.handleChange} />
                            <ConfigButtons includeSave type="submit" performingAction={saving} isFixed={false} />
                        </form>
                    </div>
                </div>
            </div>
        );
    }
}

SetupPage.propTypes = {
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired,
    fetchingSetupStatus: PropTypes.string.isRequired,
    getDescriptors: PropTypes.func.isRequired,
    getSettings: PropTypes.func.isRequired,
    saveSettings: PropTypes.func.isRequired,
    updateStatus: PropTypes.string,
    currentSettingsData: PropTypes.object,
    fieldErrors: PropTypes.object,
    errorMessage: PropTypes.string,
    actionMessage: PropTypes.string
};

SetupPage.defaultProps = {
    fieldErrors: {},
    currentSettingsData: {},
    updateStatus: '',
    errorMessage: null,
    actionMessage: null
};

const mapStateToProps = state => ({
    descriptors: state.descriptors.items,
    errorMessage: state.system.errorMessage,
    actionMessage: state.system.actionMessage,
    fetchingSetupStatus: state.system.fetchingSetupStatus,
    updateStatus: state.system.updateStatus,
    currentSettingsData: state.system.settingsData,
    fieldErrors: state.system.error
});

const mapDispatchToProps = dispatch => ({
    getDescriptors: () => dispatch(getDescriptors()),
    getSettings: () => dispatch(getInitialSystemSetup()),
    saveSettings: setupData => dispatch(saveInitialSystemSetup(setupData))
});

export default connect(mapStateToProps, mapDispatchToProps)(SetupPage);
