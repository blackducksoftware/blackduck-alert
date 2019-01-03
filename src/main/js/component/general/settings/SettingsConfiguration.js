import React, { Component } from "react";
import { getSystemSetup, saveSystemSetup } from "../../../store/actions/system";
import connect from "react-redux/es/connect/connect";
import SettingsConfigurationForm from "./SettingsConfigurationForm";

class SettingsConfiguration extends Component {
    constructor(props) {
        super(props);
        this.getSettings = this.getSettings.bind(this);
        this.saveSettings = this.saveSettings.bind(this);
    }

    render() {
        return (
            <SettingsConfigurationForm fetchingSetupStatus={this.props.fetchingSetupStatus}
                                       updateStatus={this.props.updateStatus}
                                       currentSetupData={this.props.currentSetupData}
                                       fieldErrors={this.props.fieldErrors}
                                       getSettings={this.getSettings}
                                       saveSettings={this.saveSettings} />
        )
    }

    getSettings() {
        this.props.getSettings();
    }

    saveSettings(setupData) {
        this.props.saveSystemSetup(setupData)
    }
}

const mapStateToProps = state => ({
    fetchingSetupStatus: state.system.fetchingSetupStatus,
    updateStatus: state.system.updateStatus,
    currentSetupData: state.system.setupData,
    fieldErrors: state.system.error
});

const mapDispatchToProps = dispatch => ({
    getSettings: () => dispatch(getSystemSetup()),
    saveSystemSetup: (setupData) => dispatch(saveSystemSetup(setupData))
});

export default connect(mapStateToProps, mapDispatchToProps)(SettingsConfiguration);
