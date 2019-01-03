import connect from "react-redux/es/connect/connect";
import React, { Component } from "react";
import SettingsConfiguration from "./component/general/settings/SettingsConfiguration";

class SetupPage extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div className="settingsWrapper">
                <div className="settingsContainer">
                    <div className="settingsBox">
                        <SettingsConfiguration />
                    </div>
                </div>
            </div>
        )
    }
}

const mapStateToProps = state => ({});

const mapDispatchToProps = dispatch => ({});

export default connect(mapStateToProps, mapDispatchToProps)(SetupPage);
