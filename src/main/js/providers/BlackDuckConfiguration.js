import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import NumberInput from 'field/input/NumberInput';
import PasswordInput from 'field/input/PasswordInput';
import TextInput from 'field/input/TextInput';
import ConfigButtons from 'component/common/ConfigButtons';

import { getConfig, testConfig, updateConfig } from 'store/actions/blackduck';
import * as FieldModelUtil from 'util/fieldModelUtilities';

const KEY_BLACKDUCK_URL = "blackduck.url";
const KEY_BLACKDUCK_API_KEY = "blackduck.api.key";
const KEY_BLACKDUCK_TIMEOUT = "blackduck.timeout";


const fieldNames = [
    KEY_BLACKDUCK_URL,
    KEY_BLACKDUCK_TIMEOUT,
    KEY_BLACKDUCK_API_KEY
];

class BlackDuckConfiguration
    extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            blackDuckApiKey: this.props.blackDuckApiKey,
            blackDuckApiKeyIsSet: this.props.blackDuckApiKeyIsSet,
            blackDuckTimeout: this.props.blackDuckTimeout,
            blackDuckUrl: this.props.blackDuckUrl
        };
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTest = this.handleTest.bind(this);
        this.state = {
            settingsData: FieldModelUtil.createEmptyFieldModel(fieldNames, DescriptorUtil.CONTEXT_TYPE.GLOBAL, 'provider_blackduck')
        };
    }

    componentDidMount() {
        this.props.getConfig();
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.updateStatus === 'FETCHED' || nextProps.updateStatus === 'UPDATED') {
            const newState = FieldModelUtil.checkModelOrCreateEmpty(nextProps, fieldNames);
            this.setState({
                settingsData: newState
            });
        }
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        this.setState({
            [target.name]: value
        });
    }

    handleTest() {
        const { id } = this.props;
        this.props.testConfig({ id, ...this.state });
    }

    handleSubmit(evt) {
        evt.preventDefault();
        const { id } = this.props;
        this.props.updateConfig({ id, ...this.state });
    }

    render() {
        const { errorMessage, testStatus, updateStatus } = this.props;
        return (
            <div>
                <h1>
                    <span className="fa fa-laptop" />
                    Black Duck
                </h1>

                {testStatus && testStatus === 'SUCCESS' && <div className="alert alert-success">
                    <div>Test was successful!</div>
                </div>}

                {errorMessage && <div className="alert alert-danger">
                    {errorMessage}
                </div>}

                {updateStatus === 'UPDATED' && <div className="alert alert-success">
                    {'Update successful'}
                </div>}

                <form className="form-horizontal" onSubmit={this.handleSubmit}>
                    <div>
                        <TextInput
                            id={KEY_BLACKDUCK_URL}
                            label="Host Name"
                            name={KEY_BLACKDUCK_URL}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, KEY_BLACKDUCK_URL)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(KEY_BLACKDUCK_URL)}
                            errorValue={this.props.fieldErrors[FieldModelUtil.createFieldModelErrorKey(KEY_BLACKDUCK_URL)]}
                        />
                        <PasswordInput
                            id={KEY_BLACKDUCK_API_KEY}
                            label="Password"
                            name={KEY_BLACKDUCK_API_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, KEY_BLACKDUCK_API_KEY)}
                            isSet={FieldModelUtil.isFieldModelValueSet(fieldModel, KEY_BLACKDUCK_API_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(KEY_BLACKDUCK_API_KEY)}
                            errorValue={this.props.fieldErrors[FieldModelUtil.createFieldModelErrorKey(KEY_BLACKDUCK_API_KEY)]}
                        />
                        <NumberInput
                            id={KEY_BLACKDUCK_TIMEOUT}
                            label="SMTP Port"
                            name={KEY_BLACKDUCK_TIMEOUT}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, KEY_BLACKDUCK_TIMEOUT)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(KEY_BLACKDUCK_TIMEOUT)}
                            errorValue={this.props.fieldErrors[FieldModelUtil.createFieldModelErrorKey(KEY_BLACKDUCK_TIMEOUT)]}
                        />
                    </div>
                    <ConfigButtons isFixed={false} includeSave includeTest type="submit" onTestClick={this.handleTest} />
                </form>
            </div>
        );
    }
}

// Used for compile/validation of properties
BlackDuckConfiguration.propTypes = {
    blackDuckApiKey: PropTypes.string,
    blackDuckApiKeyIsSet: PropTypes.bool.isRequired,
    blackDuckTimeout: PropTypes.number.isRequired,
    blackDuckUrl: PropTypes.string.isRequired,
    id: PropTypes.string,
    fieldErrors: PropTypes.arrayOf(PropTypes.object),
    updateStatus: PropTypes.string,
    errorMessage: PropTypes.string,
    testStatus: PropTypes.string,
    getConfig: PropTypes.func.isRequired,
    updateConfig: PropTypes.func.isRequired,
    testConfig: PropTypes.func.isRequired
};

// Default values
BlackDuckConfiguration.defaultProps = {
    blackDuckApiKey: '',
    id: null,
    errorMessage: null,
    updateStatus: null,
    fieldErrors: [],
    testStatus: ''
};

// Mapping redux state -> react props
const mapStateToProps = state => ({
    blackDuckApiKey: state.config.blackDuckApiKey,
    blackDuckApiKeyIsSet: state.config.blackDuckApiKeyIsSet,
    blackDuckTimeout: state.config.blackDuckTimeout,
    blackDuckUrl: state.config.blackDuckUrl,
    testStatus: state.config.testStatus,
    updateStatus: state.config.updateStatus,
    errorMessage: state.config.error.message,
    fieldErrors: state.config.error.fieldErrors,
    id: state.config.id
});

// Mapping redux actions -> react props
const mapDispatchToProps = dispatch => ({
    getConfig: () => dispatch(getConfig()),
    updateConfig: config => dispatch(updateConfig(config)),
    testConfig: config => dispatch(testConfig(config))
});

export default connect(mapStateToProps, mapDispatchToProps)(BlackDuckConfiguration);
