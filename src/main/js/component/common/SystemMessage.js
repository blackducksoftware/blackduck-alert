import React, {Component} from 'react';
import PropTypes from 'prop-types';
import '../../../css/messages.scss';

class SystemMessage extends Component {

    constructor(props) {
        super(props);
    }

    render() {
        const {createdAt, content, severity} = this.props;
        return (<div className="messageHeader">
            <span className={this.getIcon()} aria-hidden="true" title={severity}/><span className="messageDate">{createdAt}</span>
            <div>{content}</div>
        </div>);
    }

    getIcon() {
        const {severity} = this.props;
        const errorIcon = "fa fa-exclamation-triangle ";
        if (severity == 'ERROR') {
            return `${errorIcon} errorStatus`;
        } else if (severity == 'WARNING') {
            return `${errorIcon} warningStatus`
        } else {
            return "fa fa-check-circle validStatus"
        }
    }
}

SystemMessage.propTypes = {
    severity: PropTypes.string,
    createdAt: PropTypes.string,
    content: PropTypes.string
};

SystemMessage.defaultProps = {
    severity: 'INFO',
    createAt: '',
    content: ''
};

export default SystemMessage;

