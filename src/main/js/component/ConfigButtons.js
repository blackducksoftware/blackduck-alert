import React, { Component } from 'react';

import { submitContainers, submitButtons } from '../../css/main.css';

export default class ConfigButtons extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const includeCancel = this.props.includeCancel || "false";
        const includeTest = this.props.includeTest || "false";

        const saveButtonText = this.props.text || "Save";
        const saveButtonType = this.props.type || "button";
        const testButtonText = this.props.testText || "Test";
        const testButtonType = this.props.testType || "button";
        const cancelButtonText = this.props.cancelText || "Cancel";
        const cancelButtonType = this.props.cancelType || "button";

        return (
            <div className={submitContainers}>
                { includeCancel == "true" &&
                    <input className={submitButtons} type={cancelButtonType} value={cancelButtonText} onClick={this.props.onCancelClick}></input>
                }
                { includeTest == "true" &&
                    <input className={submitButtons} type={testButtonType} value={testButtonText} onClick={this.props.onTestClick}></input>
                }
                <input className={submitButtons} type={saveButtonType} value={saveButtonText} onClick={this.props.onClick}></input>
            </div>
        )
    }
}
