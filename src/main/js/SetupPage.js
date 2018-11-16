import connect from "react-redux/es/connect/connect";
import React, {Component} from "react";

class SetupPage extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div className="wrapper">
                <span>System Setup data form goes here....</span>
            </div>
        )
    }
}

const mapStateToProps = state => ({
    fetching: state.system.fetching,
    updateStatus: state.system.updateStatus,
    errorMessage: state.system.errorMessage
});

const mapDispatchToProps = dispatch => ({
    saveSystemSetup: (setupData) => dispatch(saveSystemSetup(setupData))
});

export default connect(mapStateToProps, mapDispatchToProps)(SetupPage);
