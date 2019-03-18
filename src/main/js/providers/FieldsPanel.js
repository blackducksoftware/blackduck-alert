import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as FieldMapping from 'util/fieldMapping';

class FieldsPanel extends React.Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.state = {
            currentConfig: this.props.currentConfig
        };
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.props.currentConfig !== prevProps.currentConfig && (this.props.updateStatus === 'FETCHED' || this.props.updateStatus === 'UPDATED')) {
            const fieldModel = FieldModelUtilities.checkModelOrCreateEmpty(this.props.currentConfig, this.state.fieldKeys);
            this.setState({
                currentConfig: fieldModel
            });
        }
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.currentConfig, target.name, value);

        this.setState({
            currentConfig: newState
        });
    }

    render() {
        const currentFieldModel = this.state.currentConfig;
        const createdFields = [];

        Object.keys(this.props.descriptorFields).forEach((key) => {
            const field = this.props.descriptorFields[key];
            const fieldKey = field.key;
            const values = FieldModelUtilities.getFieldModelValues(currentFieldModel, fieldKey);
            const isSet = FieldModelUtilities.isFieldModelValueSet(currentFieldModel, fieldKey);
            const newField = FieldMapping.createField(field, values, isSet, this.props.fieldErrors[fieldKey], this.handleChange);
            createdFields.push(newField);
        });

        return (
            <div>
                {createdFields}
            </div>
        );
    }
}

FieldsPanel.defaultProps = {
    updateStatus: null,
    fieldErrors: {}
};

FieldsPanel.propTypes = {
    fieldKeys: PropTypes.array.isRequired,
    descriptorFields: PropTypes.array.isRequired,
    currentConfig: PropTypes.object.isRequired,
    updateStatus: PropTypes.string,
    fieldErrors: PropTypes.object
};

// Mapping redux state -> react props
const mapStateToProps = state => ({
    updateStatus: state.blackduck.updateStatus,
    fieldErrors: state.provider.error.fieldErrors
});

export default connect(mapStateToProps, null)(FieldsPanel);
