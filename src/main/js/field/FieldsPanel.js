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
        this.createPanel = this.createPanel.bind(this);
        this.createHeaders = this.createHeaders.bind(this);
        this.createFields = this.createFields.bind(this);
    }

    initializeFieldMapping(fields) {
        const fieldMapping = {};

        Object.keys(fields)
            .forEach((key) => {
                const field = fields[key];
                const { panel, header } = field;

                const panelName = panel || DEFAULT_PANEL;
                const foundPanel = this.parsePanel(panelName, header, field, fieldMapping);
                Object.assign(fieldMapping, { [panelName]: foundPanel });
            });

        return fieldMapping;
    }

    parsePanel(panelName, headerName, field, fieldMapping) {
        const foundPanel = fieldMapping[panelName];
        const newHeader = headerName ? this.parseHeader(foundPanel, headerName, field) : this.parseHeader(foundPanel, DEFAULT_PANEL, field);
        return Object.assign({}, foundPanel, newHeader);
    }

    parseHeader(foundPanel, headerName, field) {
        const foundHeader = [];
        if (foundPanel && foundPanel[headerName]) {
            foundHeader.push(...foundPanel[headerName]);
        }
        foundHeader.push(field);
        return { [headerName]: foundHeader };
    }

    createPanel(panelName, fieldMapping) {
        const panel = (panelName === DEFAULT_PANEL) ? <div>{this.createHeaders(fieldMapping)}</div> :
            <CollapsiblePane title={panelName}>{this.createHeaders(fieldMapping)}</CollapsiblePane>;

        return (
            <div key={panelName} className="form-group">
                <div className="col-sm-12">
                    {panel}
                </div>
            </div>
        );
    }

    createHeaders(fieldMapping) {
        const fieldRenders = [];
        Object.keys(fieldMapping)
            .forEach((key) => {
                if (key !== DEFAULT_PANEL) {
                    const header = (<h2 key={key}>{key}</h2>);
                    fieldRenders.push(header);
                }
                const fields = this.createFields(fieldMapping[key]);
                fieldRenders.push(...fields);
            });
        return fieldRenders;
    }

    createFields(fields) {
        const { currentConfig, fieldErrors, handleChange } = this.props;
        const createdFields = [];

        fields.forEach((field) => {
            const fieldKey = field.key;
            const newField = FieldMapping.createField(field, currentConfig, fieldErrors[fieldKey], handleChange);
            createdFields.push(newField);
        });
        return createdFields;
    }

    render() {
        const createdPanels = [];

        const sortedFields = this.initializeFieldMapping(this.props.descriptorFields);
        Object.keys(sortedFields)
            .forEach((key) => {
                createdPanels.push(this.createPanel(key, sortedFields[key]));
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
