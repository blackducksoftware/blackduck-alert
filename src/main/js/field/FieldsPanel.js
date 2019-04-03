import React from 'react';
import PropTypes from 'prop-types';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as FieldMapping from 'util/fieldMapping';

class FieldsPanel extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        const createdFields = [];

        Object.keys(this.props.descriptorFields).forEach((key) => {
            const field = this.props.descriptorFields[key];
            const fieldKey = field.key;
            const values = FieldModelUtilities.getFieldModelValues(this.props.currentConfig, fieldKey);
            const isSet = FieldModelUtilities.isFieldModelValueSet(this.props.currentConfig, fieldKey);
            const newField = FieldMapping.createField(field, values, isSet, this.props.fieldErrors[fieldKey], this.props.handleChange);
            createdFields.push(newField);
        });

        return (
            <div>
                {createdFields}
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
