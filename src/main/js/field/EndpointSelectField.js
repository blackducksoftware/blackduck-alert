import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { createNewConfigurationRequest } from 'util/configurationRequestBuilder';
import DynamicSelectInput from 'field/input/DynamicSelect';
import * as FieldModelUtilities from 'util/fieldModelUtilities';

class EndpointSelectField extends Component {
    constructor(props) {
        super(props);

        this.onSendClick = this.onSendClick.bind(this);
        this.emptyFieldValue = this.emptyFieldValue.bind(this);
        this.state = ({
            options: [],
            progress: false
        });
    }

    componentDidMount() {
        this.onSendClick();
    }

    componentDidUpdate(prevProps) {
        const oldValuesEmpty = FieldModelUtilities.areKeyToValuesEmpty(prevProps.currentConfig);
        const currentValuesEmpty = FieldModelUtilities.areKeyToValuesEmpty(this.props.currentConfig);
        if (oldValuesEmpty && !currentValuesEmpty) {
            this.onSendClick();
        }
    }

    onSendClick() {
        this.setState({
            fieldError: this.props.errorValue,
            success: false
        });
        const {
            fieldKey, csrfToken, currentConfig, endpoint, requestedDataFieldKeys, value
        } = this.props;

        const newFieldModel = FieldModelUtilities.createFieldModelFromRequestedFields(currentConfig, requestedDataFieldKeys);
        const request = createNewConfigurationRequest(`/alert${endpoint}/${fieldKey}`, csrfToken, newFieldModel);
        request.then((response) => {
            if (response.ok) {
                response.json().then((data) => {
                    const options = data.map(item => {
                        const dataValue = item.value;
                        return {
                            key: dataValue,
                            label: item.label,
                            value: dataValue
                        };
                    });
                    const selectedValues = options.filter(option => value.includes(option.value));
                    if (options.length === 0 || selectedValues.length === 0) {
                        this.emptyFieldValue();
                    }

                    this.setState({
                        options,
                        success: true
                    });
                });
            } else {
                response.json()
                .then((data) => {
                    this.setState({
                        options: [],
                        fieldError: {
                            severity: 'ERROR',
                            fieldMessage: data.message
                        }
                    }, this.emptyFieldValue);
                });
            }
        });
    }

    emptyFieldValue() {
        const { fieldKey, onChange } = this.props;
        const eventObject = {
            target: {
                name: fieldKey,
                value: []
            }
        };

        onChange(eventObject);
    }

    render() {
        return (
            <div>
                <DynamicSelectInput onChange={this.props.onChange} onFocus={this.onSendClick}
                                    options={this.state.options} {...this.props} />
            </div>
        );
    }
}

EndpointSelectField.propTypes = {
    id: PropTypes.string,
    currentConfig: PropTypes.object,
    onChange: PropTypes.func.isRequired,
    endpoint: PropTypes.string.isRequired,
    fieldKey: PropTypes.string.isRequired,
    requestedDataFieldKeys: PropTypes.array
};

EndpointSelectField.defaultProps = {
    id: 'endpointSelectFieldId',
    currentConfig: {},
    requestedDataFieldKeys: []
};

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken
});

export default connect(mapStateToProps, null)(EndpointSelectField);
