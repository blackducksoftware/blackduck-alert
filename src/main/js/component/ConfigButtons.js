import React, { Component } from 'react';

import { submitContainers, submitButtons } from '../../css/main.css';

export default class ConfigButtons extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const includeTest = this.props.includeTest || "false";
        const buttonText = this.props.text || "Save";
        const buttonType = this.props.type || "button";
        const buttonTestText = this.props.testText || "Test";
        const buttonTestType = this.props.testType || "button";

        return (
            <div className={submitContainers}>
                { includeTest == "true" &&
                    <input className={submitButtons} type={buttonTestType} value={buttonTestText} onClick={this.props.onTestClick}></input>
                }
                <input className={submitButtons} type={buttonType} value={buttonText} onClick={this.props.onClick}></input>
            </div>
        )
    }
}
