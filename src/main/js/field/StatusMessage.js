import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Alert from 'react-bootstrap/Alert';
import FadeField from 'field/FadeField';

class StatusMessage extends Component {
    render() {
        const { errorMessage, actionMessage } = this.props;
        return (
            <div>
                {errorMessage && <Alert bsPrefix="statusAlert alert" dismissible variant="danger">{errorMessage}</Alert>}

                {actionMessage && <FadeField><Alert bsPrefix="statusAlert alert" dismissible variant="success">{actionMessage}</Alert></FadeField>}
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
