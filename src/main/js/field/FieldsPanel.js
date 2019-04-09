import React from 'react';
import PropTypes from 'prop-types';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as FieldMapping from 'util/fieldMapping';
import CollapsiblePane from 'component/common/CollapsiblePane';

const DEFAULT_PANEL = 'default';

class FieldsPanel extends React.Component {
    constructor(props) {
        super(props);

        this.initializeFieldMapping = this.initializeFieldMapping.bind(this);
        this.parsePanel = this.parsePanel.bind(this);
        this.parseHeader = this.parseHeader.bind(this);
        this.createDefaultPanel = this.createDefaultPanel.bind(this);
        this.createPanel = this.createPanel.bind(this);
        this.createHeaders = this.createHeaders.bind(this);
        this.createFields = this.createFields.bind(this);
    }

    initializeFieldMapping(fields) {
        const fieldMapping = {};

        Object.keys(fields).forEach((key) => {
            const field = fields[key];
            const { panel, header } = field;

            const panelName = panel || DEFAULT_PANEL;
            const foundPanel = this.parsePanel(panelName, header, field, fieldMapping);
            Object.assign(fieldMapping, { [panelName]: foundPanel });
        });

        return fieldMapping;
    }

    parsePanel(panelName, headerName, field, fieldMapping) {
        let foundPanel = fieldMapping[panelName];
        if (!foundPanel) {
            foundPanel = {};
        }

        const newHeader = headerName ? this.parseHeader(foundPanel, headerName, field) : this.parseHeader(foundPanel, DEFAULT_PANEL, field);
        Object.assign(foundPanel, newHeader);
        return foundPanel;
    }

    parseHeader(foundPanel, headerName, field) {
        let foundHeader = foundPanel[headerName];
        if (!foundHeader) {
            foundHeader = [];
        }
        foundHeader.push(field);
        return { [headerName]: foundHeader };
    }

    createDefaultPanel(fieldMapping) {
        return (
            <div className="form-group">
                <div className="col-sm-12">
                    {this.createHeaders(fieldMapping)}
                </div>
            </div>
        );
    }

    createPanel(panelName, fieldMapping) {
        return (
            <div className="form-group">
                <div className="col-sm-12">
                    <CollapsiblePane title={panelName}>{this.createHeaders(fieldMapping)}</CollapsiblePane>
                </div>
            </div>
        );
    }

    createHeaders(fieldMapping) {
        const fieldRenders = [];
        Object.keys(fieldMapping).forEach((key) => {
            if (key !== DEFAULT_PANEL) {
                const header = (<h2>{key}</h2>);
                fieldRenders.push(header);
            }
            const fields = this.createFields(fieldMapping[key]);
            fieldRenders.push(...fields);
        });
        return fieldRenders;
    }

    createFields(fields) {
        const createdFields = [];

        fields.forEach((field) => {
            const fieldKey = field.key;
            const values = FieldModelUtilities.getFieldModelValues(this.props.currentConfig, fieldKey);
            const isSet = FieldModelUtilities.isFieldModelValueSet(this.props.currentConfig, fieldKey);
            const newField = FieldMapping.createField(field, values, isSet, this.props.fieldErrors[fieldKey], this.props.handleChange);
            createdFields.push(newField);
        });
        return createdFields;
    }

    render() {
        const createdPanels = [];

        const sortedFields = this.initializeFieldMapping(this.props.descriptorFields);
        Object.keys(sortedFields).forEach((key) => {
            const panel = (key === DEFAULT_PANEL) ? this.createDefaultPanel(sortedFields[key]) : this.createPanel(key, sortedFields[key]);
            createdPanels.push(panel);
        });

        return (
            <div>
                {createdPanels}
            </div>
        );
    }
}

FieldsPanel.propTypes = {
    descriptorFields: PropTypes.array.isRequired,
    currentConfig: PropTypes.object.isRequired,
    fieldErrors: PropTypes.object.isRequired,
    handleChange: PropTypes.func.isRequired
};

export default FieldsPanel;
