import { connect } from 'react-redux';
import React, { Component } from 'react';
import { saveInitialSystemSetup } from 'store/actions/system';
import PropTypes from 'prop-types';
import StatusMessage from 'field/StatusMessage';
import FieldsPanel from 'field/FieldsPanel';
import ConfigButtons from 'component/common/ConfigButtons';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import * as FieldMapping from 'util/fieldMapping';
import * as FieldModelUtilities from 'util/fieldModelUtilities';

class SetupPage extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);

        this.state = {
            settingsData: {},
            fields: []
        };
    }

    componentWillReceiveProps(nextProps) {
        const { descriptors } = this.props;
        const newDescriptors = nextProps.descriptors;

        if (descriptors.length === 0 && newDescriptors.length > 0) {
            const settingsDescriptor = DescriptorUtilities.findDescriptorByNameAndContext(newDescriptors, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_SETTINGS, DescriptorUtilities.CONTEXT_TYPE.GLOBAL)[0];
            const { fields } = settingsDescriptor;
            const settingsData = FieldModelUtilities.createEmptyFieldModel(FieldMapping.retrieveKeys(fields), DescriptorUtilities.CONTEXT_TYPE.GLOBAL, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_SETTINGS);
            this.setState({
                settingsData,
                fields
            });
        }
    }

    handleChange({ target }) {
        const { type, name, value } = target;
        const updatedValue = type === 'checkbox' ? target.checked.toString() : value;
        const newState = Array.isArray(updatedValue) ? FieldModelUtilities.updateFieldModelValues(this.state.settingsData, name, updatedValue) : FieldModelUtilities.updateFieldModelSingleValue(this.state.settingsData, name, updatedValue);

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
        const { fields, settingsData } = this.state;
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
    saveSettings: PropTypes.func.isRequired,
    updateStatus: PropTypes.string,
    fieldErrors: PropTypes.object,
    errorMessage: PropTypes.string,
    actionMessage: PropTypes.string
};

SetupPage.defaultProps = {
    fieldErrors: {},
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
    saveSettings: setupData => dispatch(saveInitialSystemSetup(setupData))
});

export default connect(mapStateToProps, mapDispatchToProps)(SetupPage);
