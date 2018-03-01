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
                <div className="col-sm-3"></div>
                <div className="col-sm-8">
                    { includeTest &&
                        <div style={{display: 'inline-block', paddingRight: '12px', marginRight: '12px', borderRight: '1px solid #aaa'}}>
                            <GeneralButton onClick={this.props.onTestClick}>Test Configuration</GeneralButton>
                        </div>

                    }
                    { includeSave &&
                    <SubmitButton>Save</SubmitButton>
                    }
                    { includeCancel &&
                    <CancelButton />
                    }
                </div>
            </div>
        )
    }
}
