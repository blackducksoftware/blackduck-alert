import React, { Component } from 'react';
import CancelButton from '../../field/input/CancelButton';
import SubmitButton from '../../field/input/SubmitButton';
import GeneralButton from '../../field/input/GeneralButton';

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

        return (
            <div className="form-group">
                <div className="col-sm-offset-3 col-sm-8">
                { includeCancel === true &&
                    <CancelButton />
                }
                { includeTest === true &&
                    <GeneralButton onClick={this.props.onTestClick}>Test Configuration</GeneralButton>
                }
                { includeSave === true &&
                    <SubmitButton>Save</SubmitButton>
                }
                </div>
            </div>
        )
    }
}
