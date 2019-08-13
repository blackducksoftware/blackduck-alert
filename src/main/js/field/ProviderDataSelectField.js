import React, { Component } from 'react';
import PropTypes from 'prop-types';
import AsyncSelect from "react-select/lib/Async";
import LabeledField from "./LabeledField";
import { verifyLoginByStatus } from "../store/actions/session";
import { KEY_PROVIDER_NAME } from '../dynamic/DistributionConfiguration';

class ProviderDataSelectField extends Component {
    constructor(props) {
        super(props);

        this.getProvider = this.getProvider.bind(this);
        this.fetchProviderData = this.fetchProviderData.bind(this);
    }

    getProvider() {
        const { currentConfig } = this.props;
        const providerValues = currentConfig.keyToValues[KEY_PROVIDER_NAME].values;
        return providerValues && providerValues.length > 0 ? providerValues[0] : null;
    }

    fetchProviderData(endpoint, providerName, inputValue, callback) {
        const resolvedEndpoint = endpoint.replace('{provider}', providerName);
        const requestUrl = `/alert/api${resolvedEndpoint}?offset=0&limit=100&q=${inputValue}`;
        fetch(requestUrl, {
            credentials: 'same-origin'
        }).then((response) => {
            response.json()
                .then((json) => {
                    if (!response.ok) {
                        verifyLoginByStatus(response.status);
                        callback([]);
                    } else {
                        const options = json.map(item => {
                            const dataValue = item.value;
                            return { icon: null, key: dataValue, label: dataValue, value: dataValue };
                        });
                        callback(options);
                    }
                });
        }).catch((error) => {
            console.log(`Unable to connect to Server: ${error}`);
            callback([]);
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

        const loadOptions = (inputValue) => {
            return new Promise(resolve => {
                const provider = this.getProvider();
                setTimeout(() => {
                        this.fetchProviderData(this.props.providerDataEndpoint, provider, inputValue, resolve);
                    },
                    250
                );
            });
        };

        const field = (<div className={selectClasses}>
            <AsyncSelect
                id={id}
                className={inputClass}
                onInputChange={handleChange}
                isSearchable={searchable}
                removeSelected={removeSelected}
                loadOptions={loadOptions}
                cacheOptions={true}
                defaultOptions={true}
                placeholder={placeholder}
                // value={selectValue}
                isMulti={multiSelect}
                closeMenuOnSelect={!multiSelect}
                components={components}
                isDisabled={readOnly}
                noOptionsMessage={() => 'No available options. Is the provider configured correctly?'}
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
