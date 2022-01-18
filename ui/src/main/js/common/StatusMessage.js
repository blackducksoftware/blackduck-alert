import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import Alert from 'react-bootstrap/Alert';
import FadeField from 'common/FadeField';
import MessageFormatter from 'common/MessageFormatter';

const StatusMessage = ({
    id, errorMessage, actionMessage, errorIsDetailed
}) => {
    const [showError, setShowError] = useState(false);
    const [showMessage, setShowMessage] = useState(false);

    const alwaysInFront = {
        // It's over 9000!!!
        overlay: { zIndex: 9001 }
    };
    const onErrorClose = () => {
        setShowError(false);
    };
    const onMessageClose = () => {
        setShowMessage(false);
    };
    useEffect(() => {
        if (errorMessage) {
            setShowError(true);
        }
    }, [errorMessage]);
    useEffect(() => {
        if (actionMessage) {
            setShowMessage(true);
        }
    }, [actionMessage]);

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
                    <MessageFormatter
                        errorIsDetailed={errorIsDetailed}
                        message={errorMessage.message}
                        header={errorMessage.header}
                        title={errorMessage.title}
                        componentLink={errorMessage.componentLink}
                        componentLabel={errorMessage.componentLabel}
                    />
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
};

StatusMessage.propTypes = {
    id: PropTypes.string,
    errorMessage: PropTypes.object,
    errorIsDetailed: PropTypes.bool,
    actionMessage: PropTypes.string
};

StatusMessage.defaultProps = {
    id: 'statusMessageId',
    errorMessage: null,
    errorIsDetailed: false,
    actionMessage: null
};

export default StatusMessage;
