import React from 'react';
import PropTypes from 'prop-types';
import * as FieldMapping from 'util/fieldMapping';
import CollapsiblePane from 'component/common/CollapsiblePane';
import * as FieldModelUtilities from 'util/fieldModelUtilities';

const DEFAULT_PANEL = 'default';

class FieldsPanel extends React.Component {
    constructor(props) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
        this.initializeFieldMapping = this.initializeFieldMapping.bind(this);
        this.parsePanel = this.parsePanel.bind(this);
        this.parseHeader = this.parseHeader.bind(this);
        this.createPanel = this.createPanel.bind(this);
        this.createHeaders = this.createHeaders.bind(this);
        this.createFields = this.createFields.bind(this);

        this.state = {
            hiddenFieldKeys: []
        };
    }

    handleChange({ target }) {
        const { self, stateName } = this.props;
        const { type, name, value } = target;
        const updatedValue = type === 'checkbox' ? target.checked.toString() : value;
        const newState = Array.isArray(updatedValue) ? FieldModelUtilities.updateFieldModelValues(self.state[stateName], name, updatedValue) : FieldModelUtilities.updateFieldModelSingleValue(self.state[stateName], name, updatedValue);

        self.setState({
            [stateName]: newState
        });
    }

    initializeFieldMapping(fields) {
        this.state = {
            hiddenFieldKeys: []
        };

        const fieldMapping = {};

        Object.keys(fields).forEach((key) => {
            const field = fields[key];

            if (field.type === 'HideCheckboxInput') {
                const isChecked = FieldModelUtilities.getFieldModelBooleanValue(this.props.currentConfig, field.key);
                if (!isChecked) {
                    field.relatedHiddenFields.forEach(hiddenField => {
                        this.state.hiddenFieldKeys.push(hiddenField);
                    });
                }
            }

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
        let panelFields = [];
        Object.keys(fieldMapping).forEach((key) => {
            const fields = fieldMapping[key];
            panelFields = panelFields.concat(fields);
        });
        const { keyToValues } = this.props.currentConfig;
        const hasValues = Object.keys(panelFields).some((fieldKey) => {
            // If starting with an empty database, the keyToValues will be undefined
            if (!keyToValues) {
                return false;
            }
            const field = panelFields[fieldKey];
            const { key, type } = field;
            if (!this.state.hiddenFieldKeys.includes(key)) {
                return (type === 'CheckboxInput' || type === 'HideCheckboxInput') ? FieldModelUtilities.checkboxHasValue(keyToValues[key]) : FieldModelUtilities.hasValuesOrIsSet(keyToValues[key]);
            }
            return false;
        });
        const panel = (panelName === DEFAULT_PANEL) ? <div>{this.createHeaders(fieldMapping)}</div> : (
            <CollapsiblePane
                id={panelName}
                title={panelName}
                expanded={hasValues}
            >{this.createHeaders(fieldMapping)}
            </CollapsiblePane>);

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
        Object.keys(fieldMapping).forEach((key) => {
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
        const { currentConfig, fieldErrors, metadata } = this.props;
        const createdFields = [];
        const { additionalFields } = metadata;
        const currentConfigCopy = JSON.parse(JSON.stringify(currentConfig));
        if (additionalFields && Object.keys(additionalFields).length !== 0) {
            Object.keys(additionalFields).forEach(key => {
                currentConfigCopy.keyToValues[key] = additionalFields[key];
            });
        }

        fields.forEach((field) => {
            const fieldKey = field.key;
            if (!this.state.hiddenFieldKeys.includes(fieldKey)) {
                const fieldError = fieldErrors ? fieldErrors[fieldKey] : null;
                const newField = FieldMapping.createField(field, currentConfigCopy, fieldError, this.handleChange);
                createdFields.push(newField);
            }
        });
        return createdFields;
    }

    render() {
        const createdPanels = [];

        const sortedFields = this.initializeFieldMapping(this.props.descriptorFields);
        Object.keys(sortedFields).forEach((key) => {
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
    self: PropTypes.object.isRequired,
    stateName: PropTypes.string.isRequired,
    metadata: PropTypes.shape({
        additionalFields: PropTypes.object
    })
};

FieldsPanel.defaultProps = {
    metadata: {
        additionalFields: {}
    }
}

export default FieldsPanel;
