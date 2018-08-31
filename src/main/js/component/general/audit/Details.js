import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import DescriptorLabel from '../../common/DescriptorLabel';
import TextInput from '../../../field/input/TextInput';
import TextArea from '../../../field/input/TextArea';
import {ReactBsTable} from 'react-bootstrap-table';

class Details extends Component {
    constructor(props) {
        super(props);

        const {currentEntry} = props;
        const values = {};
        values.projectName = currentEntry.projectName;
        values.projectVersion = currentEntry.projectVersion;
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
        const descriptorList = this.props.descriptors.items['CHANNEL_DISTRIBUTION_CONFIG'];
        if (descriptorList) {
            const filteredList = descriptorList.filter(descriptor => descriptor.descriptorName === this.state.values.eventType)

            if (filteredList && filteredList.length > 0) {
                const foundDescriptor = filteredList[0];
                return (<DescriptorLabel keyPrefix='audit-detail-icon' descriptor={foundDescriptor}/>);
            } else {
                const cellText = "Unknown";
                return (<div className="inline">{cellText}</div>);
            }
        } else {
            const cellText = "Unknown";
            return (<div className="inline">{cellText}</div>);
        }
    }


    render(content) {
        let errorMessage = null;
        if (this.state.values.errorMessage) {
            errorMessage = <TextInput label="Error" readOnly name="errorMessage" value={this.state.values.errorMessage}/>;
        }
        let errorStackTrace = null;
        if (this.state.values.errorStackTrace) {
            errorStackTrace = <TextArea inputClass="stackTraceContainer" label="Stack Trace" readOnly name="errorStackTrace" value={this.state.values.errorStackTrace}/>;
        }

        return (
            <div className="expandableContainer">
                <div className="container-fluid">
                    <div className="row">
                        <div className="col-sm-4 text-right">
                            <label>Event Type:</label> {this.getEventType()}
                        </div>
                    </div>
                </div>
                <div className="tableContainer">
                    <TextArea inputClass="stackTraceContainer" label="Content" readOnly name="notificationContent" value={this.state.values.content}/>
                </div>
                {errorMessage}
                {errorStackTrace}
            </div>
        );
    }
}

Details.propTypes = {
    descriptors: PropTypes.object
};

Details.defaultProps = {
    descriptor: {}
};

const mapStateToProps = state => ({
    descriptors: state.descriptors
});

const mapDispatchToProps = dispatch => ({});

export default connect(mapStateToProps, mapDispatchToProps)(Details);
