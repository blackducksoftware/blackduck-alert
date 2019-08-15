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

        this.state = ({
            options: [],
            progress: false,
        });
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
            fieldKey, csrfToken, currentConfig, endpoint
        } = this.props;

        const request = createNewConfigurationRequest(`/alert${endpoint}/${fieldKey}`, csrfToken, currentConfig);
        request.then((response) => {
            if (response.ok) {
                response.json().then((data) => {
                    const options = data.map(item => {
                        const dataValue = item.value;
                        return { icon: null, key: dataValue, label: dataValue, value: dataValue };
                    });

                    this.setState({
                        options,
                        success: true
                    });
                });

            } else {
                response.json().then((data) => {
                    this.setState({
                        options: [],
                        fieldError: data.message
                    });
                });
            }
        });
    }

    render() {
        return (
            <div>
                <DynamicSelectInput onChange={this.props.onChange} onFocus={this.onSendClick} options={this.state.options} {...this.props} />
            </div>
        );
    }
}

EndpointSelectField.propTypes = {
    id: PropTypes.string,
    providerDataEndpoint: PropTypes.string,
    currentConfig: PropTypes.object,
    inputClass: PropTypes.string,
    labelClass: PropTypes.string,
    selectSpacingClass: PropTypes.string,
    components: PropTypes.object,
    value: PropTypes.array,
    placeholder: PropTypes.string,
    searchable: PropTypes.bool,
    removeSelected: PropTypes.bool,
    multiSelect: PropTypes.bool,
    onChange: PropTypes.func.isRequired,
    endpoint: PropTypes.string.isRequired,
    fieldKey: PropTypes.string.isRequired
};

EndpointSelectField.defaultProps = {
    id: 'id',
    value: [],
    currentConfig: {},
    placeholder: 'Choose a value',
    components: {},
    inputClass: 'typeAheadField',
    labelClass: 'col-sm-3',
    selectSpacingClass: 'col-sm-8',
    searchable: false,
    removeSelected: false,
    multiSelect: false
};

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken
});

export default connect(mapStateToProps, null)(EndpointSelectField);
