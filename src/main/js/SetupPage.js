import connect from "react-redux/es/connect/connect";
import React, { Component } from "react";
import { getInitialSystemSetup, saveInitialSystemSetup } from "./store/actions/system";
import SettingsConfigurationForm from "./component/general/settings/SettingsConfigurationForm";

class SetupPage extends Component {
    constructor(props) {
        super(props);
        this.getSettings = this.getSettings.bind(this);
        this.saveSettings = this.saveSettings.bind(this);
    }

    render() {
        return (
            <div className="settingsWrapper">
                <div className="settingsContainer">
                    <div className="settingsBox">
                        <SettingsConfigurationForm fetchingSetupStatus={this.props.fetchingSetupStatus}
                                                   updateStatus={this.props.updateStatus}
                                                   currentSetupData={this.props.currentSettingsData}
                                                   fieldErrors={this.props.fieldErrors}
                                                   getSettings={this.getSettings}
                                                   saveSettings={this.saveSettings} />
                    </div>
                </div>
            </div>
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
    currentSettingsData: state.system.settingsData,
    fieldErrors: state.system.error
});

const mapDispatchToProps = dispatch => ({
    getSettings: () => dispatch(getInitialSystemSetup()),
    saveSystemSetup: (setupData) => dispatch(saveInitialSystemSetup(setupData))
});

export default connect(mapStateToProps, mapDispatchToProps)(SetupPage);
