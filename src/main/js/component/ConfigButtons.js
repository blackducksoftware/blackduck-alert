import React, { Component } from 'react';

import { submitContainers, submitButtons, submitContainersFixed, submitButtonsFixed } from '../../css/main.css';

export default class ConfigButtons extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const includeCancel = this.props.includeCancel || false;
        const includeTest = this.props.includeTest || false;
        var includeSave = true;
        if (this.props.includeSave != null && this.props.includeSave === false) {
            includeSave = false;
        }

        const saveButtonText = this.props.text || "Save";
        const saveButtonType = this.props.type || "button";
        const testButtonText = this.props.testText || "Test";
        const testButtonType = this.props.testType || "button";
        const cancelButtonText = this.props.cancelText || "Cancel";
        const cancelButtonType = this.props.cancelType || "button";

        const isFixed = this.props.isFixed || false;

        var containerClass = submitContainersFixed;
        var buttonClass = submitButtonsFixed;
        if (isFixed === false) {
            containerClass = submitContainers;
            buttonClass = submitButtons;
        }
        return (
            <div className={containerClass}>
                { includeCancel === true &&
                    <button className={buttonClass} type={cancelButtonType} onClick={this.props.onCancelClick}>{cancelButtonText}</button>
                }
                { includeTest === true &&
                    <button className={buttonClass} type={testButtonType} onClick={this.props.onTestClick}>{testButtonText}</button>
                }
                { includeSave === true &&
                    <button className={buttonClass} type={saveButtonType} onClick={this.props.onClick}>{saveButtonText}</button>
                }
            </div>
        )
    }
}
