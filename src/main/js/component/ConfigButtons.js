import React, { Component } from 'react';

import { submitContainers, submitButtons, submitContainersFixed, submitButtonsFixed } from '../../css/main.css';

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

        const isFixed = this.props.isFixed || "true";

        var containerClass = submitContainersFixed;
        var buttonClass = submitButtonsFixed;
        if (isFixed === "false") {
            containerClass = submitContainers;
            buttonClass = submitButtons;
        }
        return (
            <div className={containerClass}>
                { includeCancel == "true" &&
                    <input className={buttonClass} type={cancelButtonType} value={cancelButtonText} onClick={this.props.onCancelClick}></input>
                }
                { includeTest == "true" &&
                    <input className={buttonClass} type={testButtonType} value={testButtonText} onClick={this.props.onTestClick}></input>
                }
                <input className={buttonClass} type={saveButtonType} value={saveButtonText} onClick={this.props.onClick}></input>
            </div>
        )
    }
}
