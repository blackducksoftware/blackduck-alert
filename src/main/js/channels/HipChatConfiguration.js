import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import TextInput from 'field/input/TextInput';
import PasswordInput from 'field/input/PasswordInput';
import ConfigButtons from 'component/common/ConfigButtons';
import { deleteConfig, getConfig, updateConfig } from 'store/actions/hipChatConfig';
import ChannelTestModal from 'component/common/ChannelTestModal';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import ConfigurationLabel from 'component/common/ConfigurationLabel';

const ID_KEY = 'id';
const KEY_API_KEY = 'channel.hipchat.api.key';
const KEY_HOST_SERVER = 'channel.hipchat.host.server';

const fieldDescriptions = {
    [KEY_API_KEY]: 'The API key of the user you want to use to authenticate with the HipChat server.',
    [KEY_HOST_SERVER]: 'The URL for your HipChat server.'
};

const fieldNames = [
    ID_KEY,
    KEY_API_KEY,
    KEY_HOST_SERVER
];

const configurationDescription = 'This page allows you to configure the HipChat server that Alert will send messages to.';

class HipChatConfiguration extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            currentHipChatConfig: FieldModelUtilities.createEmptyFieldModel(fieldNames, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_HIPCHAT)
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentDidMount() {
        this.props.getConfig();
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.props.currentHipChatConfig !== prevProps.currentHipChatConfig && (this.props.updateStatus === 'FETCHED' || this.props.updateStatus === 'UPDATED')) {
            const newState = FieldModelUtilities.checkModelOrCreateEmpty(this.props.currentHipChatConfig, fieldNames);
            this.setState({
                currentHipChatConfig: newState
            });
        }
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.currentHipChatConfig, target.name, value);
        this.setState({
            currentHipChatConfig: newState
        });
    }

    handleSubmit(event) {
        event.preventDefault();
        event.stopPropagation();
        const fieldModel = this.state.currentHipChatConfig;
        let emptyModel = !FieldModelUtilities.keysHaveValueOrIsSet(fieldModel, [KEY_API_KEY, KEY_HOST_SERVER]);
        if (emptyModel) {
            this.props.deleteConfig(fieldModel);
        } else {
            this.props.updateConfig(fieldModel);
        }

    }

    render() {
        const fieldModel = this.state.currentHipChatConfig;
        const { errorMessage, actionMessage } = this.props;
        return (
            <div>
                <ConfigurationLabel fontAwesomeIcon="comments" configurationName="HipChat" description={configurationDescription} />
                {errorMessage && <div className="alert alert-danger">
                    {errorMessage}
                </div>}

                {actionMessage && <div className="alert alert-success">
                    {actionMessage}
                </div>}

                <form className="form-horizontal" onSubmit={this.handleSubmit} noValidate="true">
                    <PasswordInput
                        id={KEY_API_KEY}
                        label="API Key"
                        description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_API_KEY)}
                        name={KEY_API_KEY}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_API_KEY)}
                        isSet={FieldModelUtilities.isFieldModelValueSet(fieldModel, KEY_API_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_API_KEY)}
                        errorValue={this.props.fieldErrors[KEY_API_KEY]}
                    />
                    <div>
                        <TextInput
                            id={KEY_HOST_SERVER}
                            label="HipChat Host Server Url"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_HOST_SERVER)}
                            name={KEY_HOST_SERVER}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_HOST_SERVER)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_HOST_SERVER)}
                            errorValue={this.props.fieldErrors[KEY_HOST_SERVER]}
                        />
                    </div>

                    <ConfigButtons submitId="hipChat-submit" cancelId="hipChat-cancel" includeSave includeTest onTestClick={this.props.openHipChatConfigTest} />
                    <div>
                        <ChannelTestModal
                            destinationName="Room ID"
                            showTestModal={this.props.showTestModal}
                            cancelTestModal={this.props.closeHipChatConfigTest}
                            sendTestMessage={(destination) => {
                                this.props.testConfig(this.state.currentHipChatConfig, destination);
                            }}
                            modalTesting={this.props.modalTesting}
                        />
                    </div>
                </form>
            </div>
        );
    }
}

HipChatConfiguration.propTypes = {
    openHipChatConfigTest: PropTypes.func.isRequired,
    closeHipChatConfigTest: PropTypes.func.isRequired,
    currentHipChatConfig: PropTypes.object,
    errorMessage: PropTypes.string,
    updateStatus: PropTypes.string,
    actionMessage: PropTypes.string,
    fieldErrors: PropTypes.object,
    getConfig: PropTypes.func.isRequired,
    testConfig: PropTypes.func.isRequired,
    showTestModal: PropTypes.bool.isRequired,
    modalTesting: PropTypes.bool.isRequired,
    updateConfig: PropTypes.func.isRequired,
    deleteConfig: PropTypes.func.isRequired
};

HipChatConfiguration.defaultProps = {
    currentHipChatConfig: {},
    errorMessage: null,
    updateStatus: null,
    actionMessage: null,
    fieldErrors: {}
};

// Mapping redux state -> react props
const mapStateToProps = state => ({
    currentHipChatConfig: state.hipChatConfig.config,
    showTestModal: state.hipChatConfig.showTestModal,
    modalTesting: state.hipChatConfig.modalTesting,
    updateStatus: state.hipChatConfig.updateStatus,
    actionMessage: state.hipChatConfig.actionMessage,
    errorMessage: state.hipChatConfig.error.message,
    fieldErrors: state.hipChatConfig.error.fieldErrors
});

// Mapping redux actions -> react props
const mapDispatchToProps = dispatch => ({
    getConfig: () => dispatch(getConfig()),
    updateConfig: config => dispatch(updateConfig(config)),
    openHipChatConfigTest: () => dispatch(openHipChatConfigTest()),
    closeHipChatConfigTest: () => dispatch(closeHipChatConfigTest()),
    testConfig: (config, destination) => dispatch(testConfig(config, destination)),
    deleteConfig: config => dispatch(deleteConfig(config))
});

export default connect(mapStateToProps, mapDispatchToProps)(HipChatConfiguration);
