import React, { Component } from 'react';

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
                <div className="col-sm-offset-4 col-sm-8">
                { includeCancel === true &&
                    <button className='btn-link' type="reset">Cancel</button>
                }
                { includeTest === true &&
                    <button className="btn btn-primary" type="button" onClick={this.props.onTestClick}>Test</button>
                }
                { includeSave === true &&
                    <button className="btn btn-primary" type="submit">Save</button>
                }
                </div>
            </div>
        )
    }
}
