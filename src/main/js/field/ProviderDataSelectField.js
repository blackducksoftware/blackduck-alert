import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Select from "react-select";
import LabeledField from "./LabeledField";
import { verifyLoginByStatus } from "../store/actions/session";
import { KEY_PROVIDER_NAME } from '../dynamic/DistributionConfiguration';

class ProviderDataSelectField extends Component {
    constructor(props) {
        super(props);

        this.noOptionsMessage = this.noOptionsMessage.bind(this);
        this.componentDidUpdate = this.componentDidUpdate.bind(this);
        this.getProvider = this.getProvider.bind(this);
        this.providerDataFetched = this.providerDataFetched.bind(this);
        this.fetchProviderData = this.fetchProviderData.bind(this);
        this.providerDataError = this.providerDataError.bind(this);

        this.state = {
            fetched: false,
            errorMessage: 'Please select a provider',
            providerData: []
        };
    }

    noOptionsMessage() {
        return this.state.errorMessage;
    }

    componentDidUpdate() {
        const provider = this.getProvider();
        if (!this.state.fetched && provider && provider !== '') {
            this.fetchProviderData(this.props.providerDataEndpoint, provider)
                .then(providerData => this.providerDataFetched(providerData));
        }
    }

    getProvider() {
        const { currentConfig } = this.props;
        const providerValues = currentConfig.keyToValues[KEY_PROVIDER_NAME].values;
        return providerValues && providerValues.length > 0 ? providerValues[0] : null;
    }

    providerDataFetched(providerData) {
        this.setState({
            fetched: true,
            errorMessage: 'No applicable values were found',
            providerData: providerData
        });
    }

    providerDataError(fetched, errorMessage) {
        this.setState({
            fetched: fetched,
            errorMessage: errorMessage,
            providerData: []
        });
    }

    fetchProviderData(endpoint, providerName) {
        const resolvedEndpoint = endpoint.replace('{provider}', providerName);
        const requestUrl = `/alert/api${resolvedEndpoint}`;
        return fetch(requestUrl, {
            credentials: 'same-origin'
        })
            .then((response) => {
                response.json()
                    .then((json) => {
                        if (!response.ok) {
                            verifyLoginByStatus(response.status);
                            this.providerDataError(true, 'There was a problem with the request');
                        } else {
                            const providerData = json.map(item => {
                                const dataValue = item.value;
                                return { icon: null, key: dataValue, label: dataValue, value: dataValue };
                            });
                            this.providerDataFetched(providerData);
                        }
                    });
            })
            .catch((error) => {
                console.log(`Unable to connect to Server: ${error}`);
                this.providerDataError(true, error);
            });
    }

    render() {
        const {
            id, inputClass, searchable, placeholder, value, removeSelected, multiSelect, components, selectSpacingClass, readOnly, onChange
        } = this.props;

        const selectClasses = `${selectSpacingClass} d-inline-flex p-2`;

        const handleChange = (selectedOptions) => {
            const optionValue = selectedOptions ? selectedOptions.value : null;
            const parsedArray = (Array.isArray(selectedOptions) && selectedOptions.length > 0) ? selectedOptions.map(mappedOption => mappedOption.value) : optionValue;
            onChange({
                target: {
                    name: id,
                    value: parsedArray
                }
            });
        };

        const { providerData } = this.state;
        const options = providerData ? providerData : [];
        const selectValue = options.filter(option => value.includes(option.value));

        const field = (<div className={selectClasses}>
            <Select
                id={id}
                className={inputClass}
                onChange={handleChange}
                isSearchable={searchable}
                removeSelected={removeSelected}
                options={options}
                placeholder={placeholder}
                value={selectValue}
                isMulti={multiSelect}
                closeMenuOnSelect={!multiSelect}
                components={components}
                isDisabled={readOnly}
                noOptionsMessage={this.noOptionsMessage}
            />
        </div>);
        return (
            <LabeledField field={field} {...this.props} />
        );
    }
}

ProviderDataSelectField.propTypes = {
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
    onChange: PropTypes.func.isRequired
};

ProviderDataSelectField.defaultProps = {
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

export default ProviderDataSelectField;
