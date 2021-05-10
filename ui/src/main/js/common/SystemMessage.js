import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import '../../css/messages.scss';

class SystemMessage extends Component {
    getIcon() {
        const { severity } = this.props;
        const errorIcon = 'exclamation-triangle';
        if (severity === 'ERROR') {
            return `${errorIcon}`;
        } if (severity === 'WARNING') {
            return `${errorIcon}`;
        }
        return 'check-circle';
    }

    getClassName() {
        const { severity } = this.props;
        if (severity === 'ERROR') {
            return 'errorStatus';
        } if (severity === 'WARNING') {
            return 'warningStatus';
        }
        return 'validStatus';
    }

    render() {
        const {
            createdAt, content, severity, id
        } = this.props;
        return (
            <div id={id} className="messageHeader">
                <FontAwesomeIcon
                    icon={this.getIcon()}
                    className={`alert-icon ${this.getClassName()}`}
                    size="lg"
                    title={severity}
                />
                <span className="messageDate">{createdAt}</span>
                <div>{content}</div>
            </div>
        );
    }
}

SystemMessage.propTypes = {
    id: PropTypes.string,
    severity: PropTypes.string,
    createdAt: PropTypes.string,
    content: PropTypes.string
};

SystemMessage.defaultProps = {
    id: 'systemMessageId',
    severity: 'INFO',
    createdAt: '',
    content: ''
};

export default SystemMessage;
