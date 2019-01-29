import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import TextInput from 'field/input/TextInput';
import PasswordInput from 'field/input/PasswordInput';
import ConfigButtons from 'component/common/ConfigButtons';
import { closeHipChatConfigTest, getConfig, openHipChatConfigTest, testConfig, toggleShowHostServer, updateConfig } from 'store/actions/hipChatConfig';
import ChannelTestModal from 'component/common/ChannelTestModal';
import * as FieldModelUtil from 'util/fieldModelUtilities';

const ID_KEY = 'id';
const KEY_API_KEY = 'channel.hipchat.api.key';
const KEY_HOST_SERVER = 'channel.hipchat.host.server';

const fieldNames = [
    ID_KEY,
    KEY_API_KEY,
    KEY_HOST_SERVER
];

class HipChatConfiguration extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            currentHipChatConfig: FieldModelUtil.createEmptyFieldModel(fieldNames, 'GLOBAL', 'channel_hipchat')
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentWillMount() {
        this.props.getConfig();
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.updateStatus === 'FETCHED' || nextProps.updateStatus === 'UPDATED') {
            const newState = FieldModelUtil.checkModelOrCreateEmpty(nextProps.currentHipChatConfig, fieldNames);
            this.setState({
                currentHipChatConfig: newState
            });
        }
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const newState = FieldModelUtil.updateFieldModelSingleValue(this.state.currentHipChatConfig, target.name, value);
        this.setState({
            currentHipChatConfig: newState
        });
    }

    handleSubmit(event) {
        event.preventDefault();
        event.stopPropagation();
        const fieldModel = this.state.currentHipChatConfig;
        this.props.updateConfig(fieldModel);
    }

    render() {
        const fieldModel = this.state.currentHipChatConfig;
        const { errorMessage, actionMessage } = this.props;
        return (
            <div>
                <h1>
                    <span className="fa fa-comments" />
                    HipChat
                </h1>
                {errorMessage && <div className="alert alert-danger">
                    {errorMessage}
                </div>}

                {actionMessage && <div className="alert alert-success">
                    {actionMessage}
                </div>}

                <form className="form-horizontal" onSubmit={this.handleSubmit}>
                    <PasswordInput
                        id={KEY_API_KEY}
                        label="Api Key"
                        name={KEY_API_KEY}
                        value={FieldModelUtil.getFieldModelSingleValue(fieldModel, KEY_API_KEY)}
                        isSet={FieldModelUtil.isFieldModelValueSet(fieldModel, KEY_API_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtil.createFieldModelErrorKey(KEY_API_KEY)}
                        errorValue={this.props.fieldErrors[FieldModelUtil.createFieldModelErrorKey(KEY_API_KEY)]}
                    />
                    <div>
                        <TextInput
                            id={KEY_HOST_SERVER}
                            label="HipChat Host Server Url"
                            name={KEY_HOST_SERVER}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, KEY_HOST_SERVER)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(KEY_HOST_SERVER)}
                            errorValue={this.props.fieldErrors[FieldModelUtil.createFieldModelErrorKey(KEY_HOST_SERVER)]}
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
    updateConfig: PropTypes.func.isRequired
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
    testConfig: (config, destination) => dispatch(testConfig(config, destination))
});

export default connect(mapStateToProps, mapDispatchToProps)(HipChatConfiguration);
