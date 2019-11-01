import React, { Component } from 'react';
import PropTypes from 'prop-types';

class MessageFormatter extends Component {
    constructor(props) {
        super(props);
    }

    createLinks(info) {
        const tokens = info.split(/\s/);
        return tokens.map((token, i) => {
            let hasSpace = i !== (tokens.length - 1);
            let maybeSpace = hasSpace ? ' ' : '';

            if (token.match(/^https\:\//)) {
                return (
                    <a href={token} target="_blank">{token}{maybeSpace}</a>
                );
            } else {
                return token + maybeSpace;
            }
        });
    }

    createDetailedMessage(messageBody) {
        const { header, title, info } = JSON.parse(messageBody);

        const bulletList = info.map(infoItem =>
            (<li>
                {this.createLinks(infoItem)}
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
    message: null
};

export default MessageFormatter;
