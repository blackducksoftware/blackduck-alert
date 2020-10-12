import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Alert from 'react-bootstrap/Alert';
import FadeField from 'field/FadeField';
import MessageFormatter from 'field/MessageFormatter';

class StatusMessage extends Component {
    constructor(props) {
        super(props);

        this.state = {
            showError: true,
            showMessage: true
        };
    }

    componentDidUpdate(prevProps) {
        const { showError, showMessage } = this.state;
        const { actionMessage, errorMessage } = this.props;
        if (errorMessage !== prevProps.errorMessage && !showError) {
            this.setState({ showError: true });
        }

        if (actionMessage !== prevProps.actionMessage && !showMessage) {
            this.setState({ showMessage: true });
        }
    }

    render() {
        const { showError, showMessage } = this.state;
        const { id, errorMessage, actionMessage } = this.props;
        const onErrorClose = () => {
            this.setState({ showError: false });
        };
        const onMessageClose = () => {
            this.setState({ showMessage: false });
        };
        const alwaysInFront = {
            // It's over 9000!!!
            overlay: { zIndex: 9001 }
        };
        return (
            <div id={id}>
                {errorMessage && showError
                && (
                    <Alert
                        style={alwaysInFront}
                        bsPrefix="statusAlert alert"
                        dismissible
                        onClose={onErrorClose}
                        variant="danger"
                    >
                        <MessageFormatter message={errorMessage} />
                    </Alert>
                )}

                {actionMessage && !errorMessage && showMessage
                && (
                    <FadeField>
                        <Alert
                            bsPrefix="statusAlert alert"
                            dismissible
                            onClose={onMessageClose}
                            variant="success"
                        >
                            {actionMessage}
                        </Alert>
                    </FadeField>
                )}
            </div>
        );
    }
}

StatusMessage.propTypes = {
    id: PropTypes.string,
    errorMessage: PropTypes.string,
    actionMessage: PropTypes.string
};

StatusMessage.defaultProps = {
    id: 'statusMessageId',
    errorMessage: null,
    actionMessage: null
};

export default StatusMessage;
