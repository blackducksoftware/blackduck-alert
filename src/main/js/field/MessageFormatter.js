import React, { Component } from 'react';
import PropTypes from 'prop-types';

class MessageFormatter extends Component {
    constructor(props) {
        super(props);

    }

    createDetailedMessage(messageBody) {
        const { header, title, info } = JSON.parse(messageBody);

        const bulletList = info.map(infoItem =>
            (<li>
                {infoItem}
            </li>)
        );

        return (
            <div>
                <h3>{header}</h3>
                <div>{title}</div>
                <ul>
                    {bulletList}
                </ul>
            </div>
        );
    }

    createMessage() {
        const parsedMessaged = JSON.parse(this.props.message);
        const { isDetailed, message } = parsedMessaged;

        if (isDetailed) {
            return this.createDetailedMessage(message);
        }
        return this.props.message;
    }

    render() {
        const message = this.props.message && this.createMessage();
        return (
            <div>
                {message}
            </div>
        );
    }
}

MessageFormatter.propTypes = {
    message: PropTypes.string
};

MessageFormatter.defaultProps = {
    message: {}
};

export default MessageFormatter;
