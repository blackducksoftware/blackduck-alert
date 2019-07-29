import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Select from "react-select";
import LabeledField from "./LabeledField";
import { verifyLoginByStatus } from "../store/actions/session";

class ProviderDataSelectField extends Component {
    constructor(props) {
        super(props);

        this.noOptionsMessage = this.noOptionsMessage.bind(this);
        this.fetchUserEmails = this.fetchUserEmails.bind(this);

        this.state = {
            fetched: false,
            userEmails: []
        };
    }

    componentDidUpdate() {
        if (!this.state.fetched) {
            this.fetchUserEmails(this.props.providerDataEndpoint, 'provider_blackduck');
            console.log(this.state.userEmails);
        }
    }

    noOptionsMessage() {
        return 'Please select a provider';
    }

    fetchUserEmails(endpoint, providerName) {
        // dispatch(fetchingUserEmails());
        console.log(`Fetching user emails`);
        const resolvedEndpoint = endpoint.replace('{provider}', providerName);
        // const requestUrl = `${PROVIDER_PROJECTS_URL_PREFIX}${resolvedEndpoint}`; /alert/api
        const requestUrl = `/alert/api${resolvedEndpoint}`;

        let userEmails = [];
        const fetchedUserEmails = fetch(requestUrl, {
            credentials: 'same-origin'
        })
            .then((response) => {
                response.json()
                    .then((json) => {
                        if (!response.ok) {
                            // dispatch(userEmailsError(json.message));
                            console.log(json.message);
                            verifyLoginByStatus(response.status);
                            // dispatch(verifyLoginByStatus(response.status));
                        } else {
                            // dispatch(userEmailsFetched(projects));
                            console.log(`User emails fetched`);
                            const emailOptions = json.map(email => ({
                                label: email,
                                value: email
                            }));
                            console.log(emailOptions);
                            return emailOptions;
                        }
                    });
            })
            .catch((error) => {
                // dispatch(userEmailsError(`Unable to connect to Server: ${error}`));
                console.log(`Unable to connect to Server: ${error}`);
                console.error(error);
            });

        this.setState({
            fetched: true,
            userEmails: fetchedUserEmails
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

        const field = (<div className={selectClasses}>
            <Select
                id={id}
                className={inputClass}
                onChange={handleChange}
                isSearchable={searchable}
                removeSelected={removeSelected}
                options={this.userEmails}
                placeholder={placeholder}
                value={value}
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
