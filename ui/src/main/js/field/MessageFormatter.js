import React from 'react';
import PropTypes from 'prop-types';
import { NavLink } from 'react-router-dom';

const MessageFormatter = ({
    id, errorIsDetailed, message
}) => {
    const determineDisplayMessage = () => {
        if (!message) {
            return [false, message];
        }
        try {
            const parsedMessaged = JSON.parse(message);
            if (errorIsDetailed) {
                return [true, parsedMessaged];
            }
            return [false, parsedMessaged];
        } catch (e) {
            return [false, message];
        }
    };

    const [isMessageDetailed, displayMessage] = determineDisplayMessage();
    return (
        <div id={id}>
            {!isMessageDetailed && displayMessage}
            {isMessageDetailed && (
                <div>
                    <h3>{displayMessage.header}</h3>
                    <div>{displayMessage.title}</div>
                    <div>
                        {displayMessage.message}
                        <NavLink to={displayMessage.componentLink}>
                            {displayMessage.componentLabel}
                        </NavLink>
                    </div>
                </div>
            )}
        </div>
    );
};

MessageFormatter.propTypes = {
    id: PropTypes.string,
    errorIsDetailed: PropTypes.bool,
    message: PropTypes.string
};

MessageFormatter.defaultProps = {
    id: 'messageFormatterId',
    errorIsDetailed: false,
    message: null
};

export default MessageFormatter;
