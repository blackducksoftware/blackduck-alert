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
        if (this.props.errorMessage !== prevProps.errorMessage && !this.state.showError) {
            this.setState({ showError: true });
        }
        if (this.props.actionMessage !== prevProps.actionMessage && !this.state.showMessage) {
            this.setState({ showMessage: true });
        }
    }

    render() {
        const { errorMessage, actionMessage } = this.props;
        const onErrorClose = () => {
            this.setState({ showError: false });
        };
        const onMessageClose = () => {
            this.setState({ showMessage: false });
        };
        return (
            <div>
                {errorMessage && this.state.showError && <Alert bsPrefix="statusAlert alert" dismissible onClose={onErrorClose} variant="danger"><MessageFormatter message={errorMessage} /></Alert>}

                {actionMessage && this.state.showMessage && <FadeField><Alert bsPrefix="statusAlert alert" dismissible onClose={onMessageClose} variant="success">{actionMessage}</Alert></FadeField>}
            </div>
        );
    }
}

StatusMessage.propTypes = {
    errorMessage: PropTypes.string,
    actionMessage: PropTypes.string
};

StatusMessage.defaultProps = {
    errorMessage: null,
    actionMessage: null
};

export default StatusMessage;
