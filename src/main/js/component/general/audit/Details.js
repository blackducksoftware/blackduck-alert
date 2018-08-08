import React, { Component } from 'react';

import TextInput from '../../../field/input/TextInput';
import LabeledField from '../../../field/LabeledField';
import TextArea from '../../../field/input/TextArea';

import { ReactBsTable, BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';

class Details extends Component {
    constructor(props) {
        super(props);

        const { currentEntry } = props;
        const values = {};
        values.notificationProjectName = currentEntry.notificationProjectName;
        values.notificationProjectVersion = currentEntry.notificationProjectVersion;
        values.content = currentEntry.content;

        values.eventType = currentEntry.eventType;

        values.errorMessage = currentEntry.errorMessage;
        values.errorStackTrace = currentEntry.errorStackTrace;
        this.state = {
            message: '',
            values
        };
    }

    getEventType() {
        let fontAwesomeClass = '';
        let cellText = '';
        if (this.state.values.eventType === 'channel_email') {
            fontAwesomeClass = 'fa fa-envelope fa-fw';
            cellText = 'Group Email';
        } else if (this.state.values.eventType === 'channel_hipchat') {
            fontAwesomeClass = 'fa fa-comments fa-fw';
            cellText = 'HipChat';
        } else if (this.state.values.eventType === 'channel_slack') {
            fontAwesomeClass = 'fa fa-slack  fa-fw';
            cellText = 'Slack';
        }

        return (<div className="inline">
            <span key="icon" className={fontAwesomeClass} aria-hidden="true" />
            {cellText}
        </div>);
    }


    render(content) {
        let errorMessage = null;
        if (this.state.values.errorMessage) {
            errorMessage = <TextInput label="Error" readOnly name="errorMessage" value={this.state.values.errorMessage} />;
        }
        let errorStackTrace = null;
        if (this.state.values.errorStackTrace) {
            errorStackTrace = <TextArea inputClass="stackTraceContainer" label="Stack Trace" readOnly name="errorStackTrace" value={this.state.values.errorStackTrace} />;
        }

        return (
            <div className="expandableContainer">
                <div className="container-fluid">
                    <div className="row">
                        <div className="col-sm-4 text-right">
                            <label>Event Type:</label> { this.getEventType() }
                        </div>
                    </div>
                </div>
                <div className="tableContainer">
                    <TextArea inputClass="stackTraceContainer" label="Content" readOnly name="notificationContent" value={this.state.values.content} />
                </div>
                {errorMessage}
                {errorStackTrace}
            </div>
        );
    }
}
export default Details;
