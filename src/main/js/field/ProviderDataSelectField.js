import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Select from "react-select";
import LabeledField from "./LabeledField";
import { verifyLoginByStatus } from "../store/actions/session";

class ProviderDataSelectField extends Component {
    constructor(props) {
        super(props);

        this.componentDidUpdate = this.componentDidUpdate.bind(this);
        this.providerDataFetched = this.providerDataFetched.bind(this);
        this.fetchProviderData = this.fetchProviderData.bind(this);
        this.providerDataError = this.providerDataError.bind(this);

        this.state = {
            fetched: false,
            noOptionsMessage: 'Please select a provider',
            providerData: []
        };
    }

    componentDidUpdate() {
        const { provider } = this.props;
        console.log(`Provider: ${provider}`)
        if (!this.state.fetched && !provider && provider !== '') {
            this.fetchProviderData(this.props.providerDataEndpoint, provider)
                .then(providerData => this.providerDataFetched(providerData));
        }
    }

    providerDataFetched(providerData) {
        this.setState({
                fetched: true,
                noOptionsMessage: 'Unknown error',
                providerData: providerData
            },
            () => {
                console.log(`Callback Post Fetch: ${this.state.providerData}`);
            });
        console.log(`Post Fetch: ${this.state.providerData}`);
    }

    providerDataError(fetched, errorMessage) {
        this.setState({
            fetched: fetched,
            noOptionsMessage: errorMessage,
            providerData: []
        });
    }

    fetchProviderData(endpoint, providerName) {
        console.log('Fetching provider data');
        const resolvedEndpoint = endpoint.replace('{provider}', providerName);
        const requestUrl = `/alert/api${resolvedEndpoint}`;
        return fetch(requestUrl, {
            credentials: 'same-origin'
        })
            .then((response) => {
                response.json()
                    .then((json) => {
                        if (!response.ok) {
                            console.log(json.message);
                            verifyLoginByStatus(response.status);
                            this.providerDataError(true, 'There was a problem with the request');
                        } else {
                            console.log(`Provider data fetched: ${json}`);
                            this.providerDataFetched(json);
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

        const handleChange = (option) => {
            const optionValue = option ? option.value : null;
            const parsedArray = (Array.isArray(option) && option.length > 0) ? option.map(mappedOption => mappedOption.value) : optionValue;
            onChange({
                target: {
                    name: id,
                    value: parsedArray
                }
            });
        };

        console.log("Rendering...");
        console.log(this.state.providerData);

        const field = (<div className={selectClasses}>
            <Select
                id={id}
                className={inputClass}
                onChange={handleChange}
                isSearchable={searchable}
                removeSelected={removeSelected}
                options={this.state.providerData}
                placeholder={placeholder}
                value={value}
                isMulti={multiSelect}
                closeMenuOnSelect={!multiSelect}
                components={components}
                isDisabled={readOnly}
                noOptionsMessage={this.state.noOptionsMessage}
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
    provider: PropTypes.string,
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
    provider: '',
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
