import React, { Component } from 'react';
import { getSystemSetup, saveSystemSetup } from 'store/actions/system';
import connect from 'react-redux/es/connect/connect';
import SettingsConfigurationForm from 'component/settings/SettingsConfigurationForm';
import PropTypes from 'prop-types';
import ConfigurationLabel from 'component/common/ConfigurationLabel';

const configurationDescription = 'This page allows you to configure the admin settings.';

class SettingsConfiguration extends Component {
    constructor(props) {
        super(props);
        this.getSettings = this.getSettings.bind(this);
        this.saveSettings = this.saveSettings.bind(this);
    }

    getSettings() {
        this.props.getSettings();
    }

    saveSettings(setupData) {
        this.props.saveSettings(setupData);
    }

    render() {
        const { errorMessage, actionMessage } = this.props;
        return (
            <div>
                <ConfigurationLabel fontAwesomeIcon="cog" configurationName="Settings" description={configurationDescription} />
                <SettingsConfigurationForm
                    fetchingSetupStatus={this.props.fetchingSetupStatus}
                    updateStatus={this.props.updateStatus}
                    settingsData={this.props.currentSettingsData}
                    fieldErrors={this.props.fieldErrors}
                    getSettings={this.getSettings}
                    saveSettings={this.saveSettings}
                    errorMessage={errorMessage}
                    actionMessage={actionMessage}
                />
            </div>
        );
    }
}

SettingsConfiguration.propTypes = {
    fetchingSetupStatus: PropTypes.string.isRequired,
    getSettings: PropTypes.func.isRequired,
    saveSettings: PropTypes.func.isRequired,
    errorMessage: PropTypes.string,
    actionMessage: PropTypes.string,
    updateStatus: PropTypes.string,
    currentSettingsData: PropTypes.object,
    fieldErrors: PropTypes.object
};

SettingsConfiguration.defaultProps = {
    fieldErrors: {},
    currentSettingsData: {},
    updateStatus: '',
    errorMessage: null,
    actionMessage: null
};

const mapStateToProps = state => ({
    errorMessage: state.system.errorMessage,
    actionMessage: state.system.actionMessage,
    fetchingSetupStatus: state.system.fetchingSetupStatus,
    updateStatus: state.system.updateStatus,
    currentSettingsData: state.system.settingsData,
    fieldErrors: state.system.error
});

const mapDispatchToProps = dispatch => ({
    getSettings: () => dispatch(getSystemSetup()),
    saveSettings: setupData => dispatch(saveSystemSetup(setupData))
});

export default connect(mapStateToProps, mapDispatchToProps)(SettingsConfiguration);
