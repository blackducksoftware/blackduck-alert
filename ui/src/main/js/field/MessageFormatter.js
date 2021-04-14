import React from 'react';
import PropTypes from 'prop-types';
import { NavLink } from 'react-router-dom';

const MessageFormatter = (props) => {
    const {
        id, errorIsDetailed
    } = props;
    const createDetailedMessage = (messageBody) => {
        const {
            header, title, message, componentLabel, componentLink
        } = messageBody;

        return (
            <div>
                <h3>{header}</h3>
                <div>{title}</div>
                <div>
                    {message}
                    <NavLink to={componentLink}>
                        {componentLabel}
                    </NavLink>
                </div>
            </div>
        );
    };

    const createMessage = () => {
        const { message } = props;
        try {
            const parsedMessaged = JSON.parse(message);
            if (errorIsDetailed) {
                return createDetailedMessage(parsedMessaged);
            }
            return parsedMessaged;
        } catch (e) {
            return message;
        }
    };
    const { message } = props;
    return (
        <div id={id}>
            {message && createMessage()}
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
