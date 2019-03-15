import React from 'react';
import { connect } from "react-redux";

import * as FieldMapping from "util/fieldMapping";

class FieldsPanel extends React.Component {
    constructor(props) {
        super(props)
    }

    render() {
        let createdFields = [];
        for (const field of this.props.fields) {
            let fieldColumnMapping = {
                id: field.key,
                name: field.key,
                label: field.label
            }
            createdFields.push(FieldMapping.getField(field.type, fieldColumnMapping));
        }

        return (
            <div>
                {createdFields}
            </div>
        );
    }
}

// Default values
FieldsPanel.defaultProps = {
    fields: []
};

// Mapping redux state -> react props
const mapStateToProps = state => ({
    currentDescriptor: state.provider.descriptor,
    currentConfig: state.provider.config,
    actionMessage: state.provider.actionMessage,
    updateStatus: state.provider.updateStatus,
    errorMessage: state.provider.error.message,
    fieldErrors: state.provider.error.fieldErrors
});

// Mapping redux actions -> react props
const mapDispatchToProps = dispatch => ({
    getConfig: () => dispatch(getConfig()),
    updateConfig: config => dispatch(updateConfig(config)),
    testConfig: config => dispatch(testConfig(config))
});

export default connect(mapStateToProps, mapDispatchToProps)(FieldsPanel);