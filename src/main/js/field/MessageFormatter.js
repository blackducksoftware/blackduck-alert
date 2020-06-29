import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { NavLink } from 'react-router-dom';

class MessageFormatter extends Component {
    constructor(props) {
        super(props);
    }

    createDetailedMessage(messageBody) {
        const { header, title, message, componentLabel, componentLink } = JSON.parse(messageBody);

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
    }

    createMessage() {
        try {
            const parsedMessaged = JSON.parse(this.props.message);
            const { isDetailed, message } = parsedMessaged;

            if (isDetailed) {
                return this.createDetailedMessage(message);
            }
            return message;
        } catch (e) {
            return this.props.message;
        }
    }

    render() {
        const { id } = this.props;
        const message = this.props.message && this.createMessage();
        return (
            <div id={id}>
                {message}
            </div>
        );
    }
}

MessageFormatter.propTypes = {
    id: PropTypes.string,
    message: PropTypes.string
};

MessageFormatter.defaultProps = {
    id: 'messageFormatterId',
    message: null
};

export default MessageFormatter;
